package com.trill.ecommerce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.api.ICloudFunctions
import com.trill.ecommerce.api.RetrofitCloudClient
import com.trill.ecommerce.screens.authentication.LoginActivity
import com.trill.ecommerce.screens.HomeActivity
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.util.ui.LoadingFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class TrillApplication : AppCompatActivity() {

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper
    private lateinit var auth: FirebaseAuth

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cloudFunctions: ICloudFunctions

    public override fun onStart() {
        super.onStart()
        installSplashScreen()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
        //    generateToken()
            navigateToHomeActivity()
        } else {
            navigateToLoginActivity()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        init()
    }

    private fun init() {
        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(this.supportFragmentManager)
        auth = Firebase.auth
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
    }

    private fun generateToken() {
        compositeDisposable.add(cloudFunctions!!.getToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ brainTreeToken ->

                Common.currentToken = brainTreeToken.token

                Log.i("Token", brainTreeToken.token)

            }, { throwable ->
                Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()}))
    }

    private fun navigateToHomeActivity() {

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }
}