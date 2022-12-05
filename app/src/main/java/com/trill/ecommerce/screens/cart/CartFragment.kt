package com.trill.ecommerce.screens.cart

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.CartAdapter
import com.trill.ecommerce.callback.ButtonCallBack
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.databinding.FragmentCartBinding
import com.trill.ecommerce.eventbus.CountCartEvent
import com.trill.ecommerce.eventbus.UpdateItemInCart
import com.trill.ecommerce.util.ui.LoadingFragment
import com.trill.ecommerce.util.SwipeHelper
import com.trill.ecommerce.viewmodel.CartViewModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var cartViewModel: CartViewModel

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null

    private var emptyCartView: View? = null
    private var emptyCartButton: MaterialButton? = null
    private var totalAmtTextView: TextView? = null
    private var cartFooter: View? = null
    private var buttonClean: View? = null
    private var recyclerView: RecyclerView? = null
    var adapter: CartAdapter? = null

    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        //Hide the floating action button when the page is loaded
        hideFAB()
        hideBottomNavView()
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase Auth
        auth = Firebase.auth

        initViews(root)


        //Init LoadingFragment
        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        //Init CartViewModel
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        cartViewModel.initCartDataSource(requireContext())

        cartViewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recyclerView!!.visibility = View.GONE
                cartFooter!!.visibility = View.GONE
                emptyCartView!!.visibility = View.VISIBLE
                buttonClean!!.visibility = View.GONE

            } else {
                recyclerView!!.visibility = View.VISIBLE
                cartFooter!!.visibility = View.VISIBLE
                emptyCartView!!.visibility = View.GONE
                buttonClean!!.visibility = View.VISIBLE

                adapter = CartAdapter(requireContext(), it)
                recyclerView!!.adapter = adapter

            }
        })

        //On Close Button Pressed
        onBackButtonPressed(root)

        cleanCart(root)

        checkout(root)

        return root
    }

    private fun checkout(root: View) {
        val button = root.findViewById<MaterialButton>(R.id.cartCheckoutButton)
        button.setOnClickListener {
            // Toast.makeText(requireContext(), "🤣🤣🤣", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(it).navigate(R.id.route_to_checkout)
        }

    }

    private fun cleanCart(root: View) {

        val uid = auth.currentUser!!.uid
        val cleanCartButton: View? = root.findViewById(R.id.cleanCart)
        cleanCartButton!!.setOnClickListener {

            var builder = AlertDialog.Builder(requireContext())

            val itemView =
                LayoutInflater.from(context).inflate(R.layout.cart_dialog, null)
            builder.setView(itemView)

            val dialog = builder.show()
            val buttonCancel = itemView.findViewById<MaterialButton>(R.id.cancelButton)
            buttonCancel.setOnClickListener { dialogInterface ->
                dialog.dismiss()
            }

            val buttonOk = itemView.findViewById<MaterialButton>(R.id.okButton)
            buttonOk.setOnClickListener {

                cartDataSource!!.cleanCart(uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Int> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(t: Int) {
                            Toast.makeText(context, "Cleared Cart Successfully", Toast.LENGTH_LONG)
                                .show()
                            EventBus.getDefault().postSticky(CountCartEvent(true))
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                        }

                    })

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun initViews(root: View) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        emptyCartView = root.findViewById(R.id.emptyCartLayout)
        emptyCartButton = root.findViewById(R.id.backToMenuButton)
        totalAmtTextView = root.findViewById(R.id.textTotalAmount)
        cartFooter = root.findViewById(R.id.cartFooter)
        buttonClean = root.findViewById(R.id.cleanCart)

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager
        //  recyclerView!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val swipe = object : SwipeHelper(requireContext(), recyclerView!!, 200) {
            override fun instantiateDeleteButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<DeleteButton>
            ) {
                buffer.add(DeleteButton(context!!,
                    "Delete",
                    30,
                    0,
                    Color.parseColor("#FF3C30"),
                    object : ButtonCallBack {
                        override fun onClick(pos: Int) {

                            val deleteItem = adapter!!.getItemAtPosition(pos)
                            cartDataSource!!.deleteCart(deleteItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        adapter!!.notifyItemRemoved(pos)
                                        sumCart()//Update the total amount in real time
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                        Toast.makeText(
                                            context,
                                            "Item Deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        }

                    }
                ))
            }

        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        emptyCartButton!!.setOnClickListener {
            requireActivity().onBackPressed()
        }


    }

    private fun sumCart() {
        val uid = auth.currentUser!!.uid
        cartDataSource!!.totalPrice(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Long> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Long) {
                    totalAmtTextView!!.text =  "Ksh${"%, d".format(t)}"  //"Ksh ${t.toString()}"
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }

    private fun onBackButtonPressed(root: View?) {
        val backPressedButton: ImageView = root!!.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recyclerView!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recyclerView!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART]${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        val uid = auth.currentUser!!.uid
        cartDataSource!!.totalPrice(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Long> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Long) {
                    totalAmtTextView!!.text = "KSh${"%, d".format(t)}"
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]${e.message}", Toast.LENGTH_LONG).show()
                }

            })
    }

    override fun onResume() {
        calculateTotalPrice()
        hideFAB()
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().removeAllStickyEvents()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        cartViewModel.onStop()
        compositeDisposable.clear()
        hideFAB()
    }

    private fun hideFAB() {
        val fab: View? = requireActivity().findViewById(R.id.counterFab)
        fab!!.visibility = View.GONE
    }

    private fun showFAB() {
        val fab: View? = requireActivity().findViewById(R.id.counterFab)
        fab!!.visibility = View.VISIBLE
    }

    private fun showBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.VISIBLE
    }

    private fun hideBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.GONE

    }

    override fun onDetach() {
        showBottomNavView()
        showFAB()
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}