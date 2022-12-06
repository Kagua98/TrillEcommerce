package com.trill.ecommerce.screens.menu

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.trill.ecommerce.R
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.CartItem
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.databinding.FragmentItemDetailsBinding
import com.trill.ecommerce.eventbus.CountCartEvent
import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.model.MenuItemsModel
import com.trill.ecommerce.screens.menu.comments.CommentsFragment
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import com.trill.ecommerce.util.ui.NumberButton
import com.trill.ecommerce.viewmodel.ItemDetailsViewModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class ItemDetailsFragment : Fragment() {

    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemDetailsViewModel: ItemDetailsViewModel
    private var imageView: ImageView? = null
    private var counterFab: CounterFab? = null
    private var buttonRating: FloatingActionButton? = null
    private var productName: TextView? = null
    private var productDescription: TextView? = null
    private var numberButton: NumberButton? = null
    private var btnShowComments: TextView? = null
    private var userName: String? = null
    private var userPhone: String? = null
    private var extendedFAB: ExtendedFloatingActionButton? = null


    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: CartDataSource

    override fun onAttach(context: Context) {
        showFAB()
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initView(root)
        getUserName()
        getUserPhone()

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        itemDetailsViewModel = ViewModelProvider(this)[ItemDetailsViewModel::class.java]

        itemDetailsViewModel.getMutableLiveDataMenuItem().observe(viewLifecycleOwner, Observer {
            populateFields(it)
        })

        itemDetailsViewModel.getMutableLiveDataComments().observe(viewLifecycleOwner, Observer {
            submitRatingToFirebase(it)
        })

        navbar(root)


        binding.buttonReviews.setOnClickListener {
            val commentsFragment = CommentsFragment.getInstance()
            commentsFragment.show(requireActivity().supportFragmentManager, "CommentsFragment")
        }

        binding.ratingBarLayout.setOnClickListener {
            showDialogRating(root)
        }



        binding.counterFab.setOnClickListener {
            addItemsToCart(root)
        }

        return root
    }

    private fun addItemsToCart(root: View) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        var numberButton = root.findViewById<NumberButton>(R.id.numberButton)

        val cartItem = CartItem()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        cartItem.uid = uid
        cartItem.userPhone = userPhone

        cartItem.productID = Common.listItemSelected!!.id!!
        cartItem.productName = Common.listItemSelected!!.name!!
        cartItem.productImage = Common.listItemSelected!!.image!!
        cartItem.productPrice = Common.listItemSelected!!.price!!.toLong()
        cartItem.productQuantity = numberButton.number.toInt()

        cartDataSource.getItemWithAllOptionsInCart(
            uid,
            cartItem.productID
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<CartItem> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(cartItemFromDB: CartItem) {
                    if (cartItemFromDB == cartItem) {
                        cartItemFromDB.productQuantity =
                            cartItemFromDB.productQuantity?.plus(cartItem!!.productQuantity!!)

                        cartDataSource.updateCart(cartItemFromDB)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : SingleObserver<Int> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onSuccess(t: Int) {
                                    Toast.makeText(
                                        context,
                                        "Cart Updated Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                }

                                override fun onError(e: Throwable) {
                                    Toast.makeText(
                                        context,
                                        "[Update Cart ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            })
                    } else {
                        //If item is not available in database, just update it
                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Toast.makeText(
                                    context,
                                    "Item Added to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            }, { t: Throwable? ->
                                Toast.makeText(
                                    context,
                                    "[INSERT CART]${t!!.message}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            )
                        )
                    }
                }

                override fun onError(e: Throwable) {
                    if (e.message!!.contains("empty")) {
                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Toast.makeText(
                                    context,
                                    "Item Added to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            }, { t: Throwable? ->
                                Toast.makeText(
                                    context,
                                    "[INSERT CART]${e.message}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            )
                        )
                    } else {
                        Toast.makeText(context, "[CART ERROR]${e.message}!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            })


    }

    private fun initView(root: View) {
        extendedFAB = binding.counterFab
    }



    private fun submitRatingToFirebase(it: CommentsModel?) {
        // showLoading(true)
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REFERENCE)
            .child(Common.listItemSelected!!.id!!).push().setValue(it)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToProduct(it!!.ratingValue)
                } else
                    showLoading(false)

            }
    }

    private fun addRatingToProduct(ratingValue: Float) {
        FirebaseDatabase.getInstance().getReference(Common.MENU_CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!).child("products")
            .child(Common.listItemSelected!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val menuItemsModel = snapshot.getValue(MenuItemsModel::class.java)
                        menuItemsModel!!.key = Common.listItemSelected!!.key!!

                        //Apply Rating
                        val sumRating = menuItemsModel.ratingValue!!.plus(ratingValue)
                        val ratingCount = menuItemsModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        //Update data in variable
                        menuItemsModel.ratingCount = ratingCount
                        menuItemsModel.ratingValue = result

                        snapshot.ref.updateChildren(updateData).addOnCompleteListener { task ->
                            showLoading(false)
                            if (task.isSuccessful) {
                                Common.listItemSelected = menuItemsModel
                                itemDetailsViewModel!!.setMenuItemsModel(menuItemsModel)

                                Snackbar.make(
                                    requireView(),
                                    "Comment saved successfully",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }


                    } else showLoading(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Snackbar.make(requireView(), error.message, Snackbar.LENGTH_LONG).show()
                }

            })

    }

    private fun showDialogRating(root: View) {
        var builder = AlertDialog.Builder(requireContext())

        val itemView =
            LayoutInflater.from(context).inflate(R.layout.item_details_rating_dialog, null)
        builder.setView(itemView)

        builder.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("Save Comment") { dialogInterface, i ->

            //Views from the Custom Dialog
            var editText: TextInputEditText? = itemView.findViewById(R.id.textInputEditText)
            var ratingBar: RatingBar? = itemView.findViewById(R.id.ratingBar)
            var userComment = editText!!.text.toString()
            var ratingValue = ratingBar!!.rating

            var userName: String? = null

            //Read data from firebase
            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            val commentsModel = CommentsModel()
            commentsModel.name = userName
            commentsModel.uid = uid
            commentsModel.comment = userComment
            commentsModel.ratingValue = ratingValue!!

            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentsModel.commentTimeStamp = serverTimeStamp

            itemDetailsViewModel.setCommentsModel(commentsModel)

        }
        val dialog = builder.show()
        dialog.show()
    }

    private fun populateFields(it: MenuItemsModel?) {
        Glide.with(requireContext()).load(it!!.image).into(binding.imageView)
        binding.title.text = it.name
        binding.price.text =  "Ksh${"%, d".format(it!!.price)} (Inclusive VAT)"   //"KSh ${it!!.price.toString()}"
        binding.description.text = it.description

        binding.ratingBar.rating = it.ratingValue!!.toFloat()
    }

    private fun navbar(root: View) {
        val backPressedButton: ImageView = root.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            onBackPressed(root)
        }

    }

    private fun showLoading(isLoading: Boolean) {
        loadingFragmentHelper.showLoading(isLoading)
    }

    private fun getUserName() {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.item_details_rating_dialog, null)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.child(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        //Everything works to plan
                        val dataSnapshot = task.result
                        val name = dataSnapshot.child("name").value.toString()
                        var editText: TextInputEditText? =
                            itemView.findViewById(R.id.textInputEditText)
                        var ratingBar: RatingBar? = itemView.findViewById(R.id.ratingBar)

                        userName = name
                        Log.i(TAG, "Logged in name is $name")
                        //assign value to textview
                    } else {
                        //Set username to customer. Means username is null which shouldn't be the case
                    }
                } else
                    Toast.makeText(
                        context,
                        "An error occurred, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
            }
    }

    private fun getUserPhone() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.child(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        //Everything works to plan
                        val dataSnapshot = task.result
                        val phone = dataSnapshot.child("phone").value.toString()
                        userPhone = phone
                        //        Log.i(TAG, "Phone number is $phone")

                    } else {
                        //Set username to customer. Means username is null which shouldn't be the case
                    }
                } else
                    Toast.makeText(
                        context,
                        "An error occurred, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
            }
    }

    override fun onResume() {
        showFAB()
        super.onResume()
    }

    private fun onBackPressed(view: View) {
        requireActivity().onBackPressed()
    }

    private fun showFAB() {
        val fab: View? = requireActivity().findViewById(R.id.counterFab)
        fab!!.visibility = View.VISIBLE
    }

    override fun onDetach() {
        if (compositeDisposable != null)
            compositeDisposable.clear()
        super.onDetach()
    }
}