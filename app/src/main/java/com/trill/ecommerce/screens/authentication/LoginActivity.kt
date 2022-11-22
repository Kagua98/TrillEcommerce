package com.trill.ecommerce.screens.authentication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.R
import com.trill.ecommerce.data.AuthViewModel
import com.trill.ecommerce.databinding.ActivityHomeBinding
import com.trill.ecommerce.databinding.ActivityLoginBinding
import com.trill.ecommerce.screens.home.HomeActivity
import com.trill.ecommerce.util.LoadingFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        installSplashScreen()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            navigateToHomeActivity()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(this.supportFragmentManager)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.textSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        initViews()


    }

    private fun initViews(){
        val buttonLogin : View? = findViewById(R.id.button)
        buttonLogin!!.setOnClickListener {
            login()
        }
    }

    private fun navigateToHomeActivity(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun login() {
      //  showLoading(true)
        val textInputLayoutEmail : TextInputLayout = findViewById(R.id.textInputLayoutEmail)
        val textInputLayoutPassword: TextInputLayout = findViewById(R.id.textInputLayoutPassword)

        var textEmail: TextInputEditText = findViewById(R.id.textEmail)
        var textPassword: TextInputEditText = findViewById(R.id.textPassword)

        var email = textEmail.text.toString()
        var password = textPassword.text.toString()


        if (email.isEmpty() && password.isEmpty()){
            textInputLayoutEmail.error = getString(R.string.login_empty_email_field_error)
            textInputLayoutPassword.error = getString(R.string.login_empty_password_field_error)
        }else if (email.isEmpty()){
            textInputLayoutEmail.error = getString(R.string.login_empty_email_field_error)
        }else if (password.isEmpty()){
            textInputLayoutPassword.error = getString(R.string.login_empty_password_field_error)
        }else if (email.isNotEmpty() && password.isNotEmpty()){

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    showLoading(true)
                    if (task.isSuccessful) {
                        showLoading(false)
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "signInWithEmail:success", Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser!!.uid
                        Log.i("Uid", user)
                     //   updateUI(user)
                        navigateToHomeActivity()
                    } else {
                        showLoading(false)
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed. User does not exist",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }

    private fun showLoading(isLoading: Boolean) {
        loadingFragmentHelper.showLoading(isLoading)
    }

    override fun onStop() {
     //   if (listener != null)
     //       firebaseAuth.removeAuthStateListener(listener)
     //   compositeDisposable.clear()

        super.onStop()
    }
}