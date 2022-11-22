package com.trill.ecommerce.screens.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.trill.ecommerce.screens.home.HomeActivity
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.LoadingFragment


class RegisterActivity : AppCompatActivity() {


    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var userRef : DatabaseReference
    private lateinit var authStateListener: AuthStateListener

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
        }

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        val button = findViewById<MaterialButton>(R.id.button)
        button.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
      //  showLoading(true)

        var textInputLayoutName: TextInputLayout = findViewById(R.id.textInputLayoutName)
        var textInputLayoutEmail: TextInputLayout = findViewById(R.id.textInputLayoutEmail)
        var textInputLayoutPhone: TextInputLayout = findViewById(R.id.textInputLayoutPhone)
        var textInputLayoutPassword: TextInputLayout = findViewById(R.id.textInputLayoutPassword)

        var textName: TextInputEditText = findViewById(R.id.textName)
        var textEmail: TextInputEditText = findViewById(R.id.textEmail)
        var textPhone: TextInputEditText = findViewById(R.id.textPhone)
        var textPassword: TextInputEditText = findViewById(R.id.textPassword)

        var name = textName.text.toString()
        var email = textEmail.text.toString()
        var phone = textPhone.text.toString()
        var password = textPassword.text.toString()

        if (name.isEmpty() && email.isEmpty() && phone.isEmpty() && password.isEmpty()){
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else if(name.isEmpty()){
            textInputLayoutName.error = getString(R.string.register_empty_name_field_error)
        }else if (email.isEmpty()){
            textInputLayoutEmail.error = getString(R.string.register_empty_email_field_error)
        }else if (!isEmailValid(email)){
            textInputLayoutEmail.error = getString(R.string.register_invalid_email_field_error)
        } else if (phone.isEmpty()){
            textInputLayoutPhone.error = getString(R.string.register_empty_phone_field_error)
        }else if(phone.length > 10){
            textInputLayoutPhone.error = getString(R.string.register_invalid_phone_field_error)
        }else if (password.isEmpty()){
            textInputLayoutPassword.error = getString(R.string.register_empty_password_field_error)
        }else if (password.length < 6){
            textInputLayoutPassword.error = getString(R.string.register_invalid_password_field_error)
        }
        else {
            firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task->
                    if (task.isSuccessful){
                        showLoading(true)
                        val userModel  = UserModel()
                        userModel.name = name
                        userModel.email = email
                        userModel.phone = phone
                        userModel.uid = firebaseAuth.currentUser!!.uid

                        val user = Firebase.auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build()
                        user!!.updateProfile(profileUpdates)

                        userRef.child(firebaseAuth!!.currentUser!!.uid)
                            .setValue(userModel)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){

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
}