package com.trill.ecommerce.screens.checkout

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.api.ICloudFunctions
import com.trill.ecommerce.api.RetrofitCloudClient
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.CartItem
import com.trill.ecommerce.database.LocalCartDataSource
import com.trill.ecommerce.databinding.FragmentCheckoutBinding
import com.trill.ecommerce.model.OrderModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var totalAmtTextView: TextView? = null
    private var addressEditText: TextInputEditText? = null
    private var paymentOptionAutoCompleteTextView: AutoCompleteTextView? = null
    private var checkoutButton: MaterialButton? = null
    private var selectedPaymentOption: String? = null

    private var buttonDate: MaterialButton? = null
    private var buttonTime: MaterialButton? = null
    private var addressTextInputLayout: TextInputLayout? = null
    private var notesEditText: TextInputEditText? = null

    private var salesPeopleAutoCompleteTextView: AutoCompleteTextView? = null

    private var contactName: String? = null
    private var contactPhone: String? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private val REQUEST_BRAINTREE_CODE: Int = 8888

    private lateinit var root: View

    private lateinit var cloudFunctions: ICloudFunctions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        root = binding.root

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        initViews(root)

        onBackButtonPressed(root)


        return root
    }


    private fun onBackButtonPressed(root: View?) {
        val backPressedButton: ImageView = root!!.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun initViews(root: View) {
        // Initialize Firebase Auth
        auth = Firebase.auth

        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        totalAmtTextView = root.findViewById(R.id.textTotalAmount)
        addressEditText = root.findViewById(R.id.textInputEditTextAddress)

        fetchUserDetails(root)

        paymentOptionAutoCompleteTextView =
            root.findViewById(R.id.paymentOptionAutoCompleteTextView)
        setupPaymentOptionAutoCompleteTextView()

        //  setCheckoutButtonEnabled(false)
        checkoutButton = root.findViewById(R.id.checkoutButton)
        checkoutButton!!.setOnClickListener {

            if (buttonDate!!.text!!.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter delivery date", Toast.LENGTH_SHORT)
                    .show()
            } else if (buttonTime!!.text!!.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter delivery time", Toast.LENGTH_SHORT)
                    .show()
            } else if (addressEditText!!.text!!.toString().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter delivery address",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (paymentOptionAutoCompleteTextView!!.text!!.toString().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select a payment option",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (salesPeopleAutoCompleteTextView!!.text!!.toString().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter your sales agent",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (paymentOptionAutoCompleteTextView!!.text!!.toString() == "M-PESA") {
                    checkoutWithMPesaOption()
                } else if (paymentOptionAutoCompleteTextView!!.text!!.toString() == "Cash") {
                    checkoutWithCODOption(root)
                } else if (paymentOptionAutoCompleteTextView!!.text!!.toString() == "Bank") {
                    checkoutWithBraintreeOption()
                } else
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid payment option",
                        Toast.LENGTH_LONG
                    ).show()
            }
        }


        calculateTotalPrice()

        salesPeopleAutoCompleteTextView = root.findViewById(R.id.salesAgentAutoCompleteTextView)
        setupSalesPeopleAutoCompleteTextView()

        addressTextInputLayout = root.findViewById(R.id.addressTextInputLayout)
        addressTextInputLayout!!.setEndIconOnClickListener {
            checkLocationPermission()
        }

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

        buttonDate = root.findViewById(R.id.dateButton)
        buttonDate!!.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Choose Delivery Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.MaterialDatePickerTheme)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            datePicker.show(childFragmentManager, "datePicker")
            datePicker.addOnPositiveButtonClickListener {
                val simpleDateFormat = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())
                buttonDate!!.text = simpleDateFormat.format(Date(it).time)
            }
        }

        buttonTime = root.findViewById(R.id.timeButton)
        buttonTime!!.setOnClickListener {
            val isSystem24hr = is24HourFormat(requireContext())
            val clockFormat = if (isSystem24hr) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Set Delivery Time")
                .build()
            picker.show(childFragmentManager, "TAG")

            picker.addOnPositiveButtonClickListener {
                val h = picker.hour
                val min = picker.minute
                buttonTime!!.text = "$h:$min"
            }
        }

        notesEditText = root.findViewById(R.id.notesEditText)

    }

    private fun fetchUserDetails(root: View) {
        // Write a message to the database

        val uid = auth.currentUser!!.uid
        val database = Firebase.database
        val reference = database.getReference("Users").child(uid)

        showLoading(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                val map: Map<String, Any> = snapshot.value as Map<String, Any>
                val name = map["name"]
                val phone = map["phone"]
                val email = map["email"]
                val address = map["address"]
                val contact = map["contact"]
                val pin = map["pin"]

                contactName = name.toString()
                contactPhone = phone.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value. Error details -> ${error.toException().message}")
            }

        })
    }

    private fun checkoutWithBraintreeOption() {
        val uid = auth.currentUser!!.uid
        //get Token
        val dropInRequest = DropInRequest().clientToken(Common.currentToken)
        startActivityForResult(dropInRequest.getIntent(context), REQUEST_BRAINTREE_CODE)

    }

    private fun checkoutWithMPesaOption() {
        val uid = auth.currentUser!!.uid
        compositeDisposable.add(
            cartDataSource!!.getCartItems(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cartItemList ->

                    //When we have all the cart items, we will calculate the total price
                    cartDataSource!!.totalPrice(uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Long> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: Long) {
                                val finalPrice = t
                                val order = OrderModel()
                                order.userId = uid
                                order.userName = contactName//Common.currentUser!!.name
                                order.userPhone = contactPhone//Common.currentUser!!.phone

                                order.date = buttonDate!!.text.toString()
                                order.time = buttonTime!!.text.toString()

                                order.shippingAddress = addressEditText!!.text.toString()
                                order.notes = notesEditText!!.text.toString()

                                order.cartItemList = cartItemList
                                order.totalPayment = finalPrice
                                order.transactionId = "Cash on Delivery"
                                order.isCOD = true
                                order.orderStatus = 0

                                //Submit order to Firebase
                                submitMPesaOrderToFirebase(order) {

                                }
                            }

                            override fun onError(e: Throwable) {
                                if (!e!!.message!!.contains("Query returned empty"))
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                                        .show()
                            }

                        })

                }
        )

    }

    //M-Pesa
    private fun checkoutWithMPesaOption(root: View) {
        val uid = auth.currentUser!!.uid
        compositeDisposable.add(
            cartDataSource!!.getCartItems(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cartItemList ->

                    //When we have all the cart items, we will calculate the total price
                    cartDataSource!!.totalPrice(uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Long> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: Long) {
                                val finalPrice = t
                                val order = OrderModel()
                                order.userId = uid
                                order.userName = contactName//Common.currentUser!!.name
                                order.userPhone = contactPhone//Common.currentUser!!.phone

                                order.date = buttonDate!!.text.toString()
                                order.time = buttonTime!!.text.toString()

                                order.shippingAddress = addressEditText!!.text.toString()
                                order.notes = notesEditText!!.text.toString()

                                order.cartItemList = cartItemList
                                order.totalPayment = finalPrice
                                order.transactionId = "MPESA"
                                order.isCOD = false

                                //Submit order to Firebase
                                submitMPesaOrderToFirebase(order) {

                                }
                            }

                            override fun onError(e: Throwable) {
                                if (!e!!.message!!.contains("Query returned empty"))
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                                        .show()
                            }

                        })

                }
        )
    }

    //COD - Cash On Delivery
    private fun checkoutWithCODOption(root: View) {
        val uid = auth.currentUser!!.uid
        compositeDisposable.add(
            cartDataSource!!.getCartItems(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cartItemList ->

                    //When we have all the cart items, we will calculate the total price
                    cartDataSource!!.totalPrice(uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Long> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: Long) {
                                val finalPrice = t
                                val order = OrderModel()
                                order.userId = uid
                                order.userName = contactName//Common.currentUser!!.name
                                order.userPhone = contactPhone//Common.currentUser!!.phone

                                order.date = buttonDate!!.text.toString()
                                order.time = buttonTime!!.text.toString()

                                order.shippingAddress = addressEditText!!.text.toString()
                                order.notes = notesEditText!!.text.toString()

                                order.cartItemList = cartItemList
                                order.totalPayment = finalPrice
                                order.transactionId = "Cash on Delivery"
                                order.isCOD = true

                                //Submit order to Firebase
                                submitCashOrderToFirebase(order) {

                                }
                            }

                            override fun onError(e: Throwable) {
                                if (!e!!.message!!.toString().contains("Query returned empty"))
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                                        .show()
                            }

                        })

                }
        )
    }

    private fun submitMPesaOrderToFirebase(order: OrderModel, function: () -> Unit) {
        val uid = auth.currentUser!!.uid
        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REFERNCE)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener { task ->
                //Clear cart
                if (task.isSuccessful) {
                    cartDataSource!!.cleanCart(uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Int> {
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onSuccess(t: Int) {

                                activity!!.findNavController(id)
                                    .navigate(R.id.route_to_mpesa_checkout)
                            }

                            override fun onError(e: Throwable) {
                                if (!e!!.message!!.contains("Query returned empty"))
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                                        .show()
                            }


                        })
                }
            }
    }

    private fun submitCashOrderToFirebase(order: OrderModel, function: () -> Unit) {
        val uid = auth.currentUser!!.uid
        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REFERNCE)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener { task ->
                //Clear cart
                if (task.isSuccessful) {
                    cartDataSource!!.cleanCart(uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Int> {
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onSuccess(t: Int) {

                                lifecycleScope.launchWhenResumed {
                                    activity!!.findNavController(id)
                                        .navigate(R.id.route_to_order_success)
                                }


                            }

                            override fun onError(e: Throwable) {
                                if (!e.message!!.toString().contains("Query returned empty"))
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                                        .show()
                            }


                        })
                }
            }
    }

    private fun setupSalesPeopleAutoCompleteTextView() {
        var salesPeople = resources.getStringArray(R.array.sales_people)

        var arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.sales_people_dropdown_item, salesPeople)

        salesPeopleAutoCompleteTextView!!.setAdapter(arrayAdapter)

    }

    private fun setupPaymentOptionAutoCompleteTextView() {
        var paymentMethods = resources.getStringArray(R.array.payment_methods)

        var arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.payment_dropdown_item, paymentMethods)

        paymentOptionAutoCompleteTextView!!.setAdapter(arrayAdapter)

        paymentOptionAutoCompleteTextView!!.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var item = p0!!.getItemAtPosition(p2).toString()
                selectedPaymentOption = item
            }

        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permission is already granted
            checkGPS()


        } else {
            //Permission not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun checkGPS() {
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        val interval = 5000
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval.toLong())
                .setMinUpdateIntervalMillis(2000)
                .setIntervalMillis(5000)
                .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                //when the gps is on
                val response = task.getResult(ApiException::class.java)

                getUserLocation()

            } catch (e: ApiException) {
                //when the gps is off
                progressBar.visibility = View.GONE
                e.printStackTrace()

                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        //Send the request to enable the GPS
                        val resolveApiException = e as ResolvableApiException
                        resolveApiException.startResolutionForResult(requireActivity(), 200)

                    } catch (sendIntentException: IntentSender.SendIntentException) {
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        //When the setting is unavailable
                    }
                }
            }


        }
    }

    private fun getUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result

            if (location != null) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    val addressLine = address!![0].getAddressLine(0)

                    progressBar.visibility = View.GONE

                    addressEditText!!.setText(addressLine)

                    val address_location = address!![0].getAddressLine(0)

                } catch (e: IOException) {
                    progressBar.visibility = View.GONE
                }
            }
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
                    totalAmtTextView!!.text =
                        "Ksh ${"%, d".format(t).toString()}"  //"Ksh ${t.toString()}"
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.toString().contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]${e.message}", Toast.LENGTH_LONG).show()
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uid = auth.currentUser!!.uid
        if (requestCode == REQUEST_BRAINTREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result!!.paymentMethodNonce

                //Calculate Total price of cart items
                cartDataSource!!.totalPrice(uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Long> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(totalPrice: Long) {
                            //Get all cart Items
                            compositeDisposable.add(
                                cartDataSource!!.getCartItems(uid)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ cartItems: List<CartItem>? ->

                                        //After we have all cart items, submit payment to Braintree
                                        compositeDisposable.add(
                                            cloudFunctions.submitPayment(totalPrice, nonce!!.nonce)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({ brainTreeTransaction ->
                                                    if (brainTreeTransaction.success) {
                                                        val finalPrice = totalPrice
                                                        val order = OrderModel()
                                                        order.userId = uid
                                                        order.userName =
                                                            contactName//Common.currentUser!!.name
                                                        order.userPhone =
                                                            contactPhone//Common.currentUser!!.phone

                                                        order.shippingAddress =
                                                            addressEditText!!.text.toString()
                                                        order.notes =
                                                            notesEditText!!.text.toString()

                                                        order.cartItemList = cartItems
                                                        order.totalPayment = finalPrice
                                                        order.transactionId =
                                                            brainTreeTransaction.transaction!!.id
                                                        order.isCOD = false

                                                        //Submit order to Firebase
                                                        submitCashOrderToFirebase(order) {}
                                                    }

                                                }, { t: Throwable? ->
                                                    Toast.makeText(
                                                        requireContext(),
                                                        t!!.message.toString(),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                })


                                        )


                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            requireContext(),
                                            t!!.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    })

                            )
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        loadingFragmentHelper.showLoading(isLoading)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}