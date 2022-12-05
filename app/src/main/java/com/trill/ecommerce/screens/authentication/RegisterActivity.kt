package com.trill.ecommerce.screens.authentication

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.model.UserModel
import com.trill.ecommerce.screens.HomeActivity
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import java.io.IOException
import java.util.*


class RegisterActivity : AppCompatActivity() {


    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var userRef: DatabaseReference
    private lateinit var authStateListener: AuthStateListener

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(this.supportFragmentManager)

        init()
    }

    private fun init() {

        val loginButton = findViewById<View>(R.id.textSignIn)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        val button = findViewById<MaterialButton>(R.id.button)
        button.setOnClickListener {
            registerUser()
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    private fun registerUser() {
        //  showLoading(true)

        var textInputLayoutName: TextInputLayout = findViewById(R.id.textInputLayoutName)
        var textInputLayoutEmail: TextInputLayout = findViewById(R.id.textInputLayoutEmail)
        var textInputLayoutPhone: TextInputLayout = findViewById(R.id.textInputLayoutPhone)
        var textInputLayoutContact: TextInputLayout = findViewById(R.id.textInputLayoutContact)
        var textInputLayoutAddress: TextInputLayout = findViewById(R.id.textInputLayoutAddress)
        var textInputLayoutPassword: TextInputLayout = findViewById(R.id.textInputLayoutPassword)

        textInputLayoutAddress.setEndIconOnClickListener {
            checkLocationPermission()
        }

        var textName: TextInputEditText = findViewById(R.id.textName)
        var textEmail: TextInputEditText = findViewById(R.id.textEmail)
        var textPhone: TextInputEditText = findViewById(R.id.textPhone)
        var textContact : TextInputEditText = findViewById(R.id.textContactPerson)
        var textAddress: TextInputEditText = findViewById(R.id.textAddress)
        var textPassword: TextInputEditText = findViewById(R.id.textPassword)

        var name = textName.text.toString()
        var email = textEmail.text.toString()
        var phone = textPhone.text.toString()
        var contactPerson = textContact.text.toString()
        var address = textAddress.text.toString()
        var password = textPassword.text.toString()

        if (name.isEmpty() && email.isEmpty() && phone.isEmpty() && password.isEmpty() && contactPerson.isEmpty() && address.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else if (name.isEmpty()) {
            textInputLayoutName.error = getString(R.string.register_empty_name_field_error)
        } else if (email.isEmpty()) {
            textInputLayoutEmail.error = getString(R.string.register_empty_email_field_error)
        } else if (!isEmailValid(email)) {
            textInputLayoutEmail.error = getString(R.string.register_invalid_email_field_error)
        } else if (phone.isEmpty()) {
            textInputLayoutPhone.error = getString(R.string.register_empty_phone_field_error)
        } else if (phone.length > 10) {
            textInputLayoutPhone.error = getString(R.string.register_invalid_phone_field_error)
        }else if (contactPerson.isEmpty()){
            textInputLayoutContact.error = getString(R.string.register_invalid_contact_person_error)
        }else if (address.isEmpty()){
            textInputLayoutAddress.error = getString(R.string.register_invalid_address_field_error)
        }else if (password.isEmpty()) {
            textInputLayoutPassword.error = getString(R.string.register_empty_password_field_error)
        } else if (password.length < 6) {
            textInputLayoutPassword.error =
                getString(R.string.register_invalid_password_field_error)
        } else {
            firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        showLoading(true)
                        val userModel = UserModel()
                        userModel.name = name
                        userModel.email = email
                        userModel.phone = phone
                        userModel.contact = contactPerson
                        userModel.address = address
                        userModel.uid = firebaseAuth.currentUser!!.uid

                        val user = Firebase.auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(contactPerson).build()
                        user!!.updateProfile(profileUpdates)

                        userRef.child(firebaseAuth.currentUser!!.uid)
                            .setValue(userModel)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    showLoading(false)
                                    intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                }
        }


    }



    private fun showLoading(isLoading: Boolean) {
        loadingFragmentHelper.showLoading(isLoading)
    }

    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permission is already granted
            checkGPS()


        } else {
            //Permission not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun checkGPS() {
        val interval = 5000
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval.toLong())
                .setMinUpdateIntervalMillis(2000)
                .setIntervalMillis(5000)
                .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                //when the gps is on
                val response = task.getResult(ApiException::class.java)

                getUserLocation()

            } catch (e: ApiException) {
                //when the gps is off
                e.printStackTrace()

                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        //Send the request to enable the GPS
                        val resolveApiException = e as ResolvableApiException
                        resolveApiException.startResolutionForResult(this, 200)

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
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result

            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())

                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    var textAddress: TextInputEditText = findViewById(R.id.textAddress)
                    val addressLine = address!![0].getAddressLine(0)
                    textAddress.setText(addressLine)


                } catch (e: IOException) {

                }
            }
        }
    }
}