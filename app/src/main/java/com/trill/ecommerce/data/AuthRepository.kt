package com.trill.ecommerce.data

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val application: Application) {
    val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val userLoggedInMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String?, password: String?) {
        auth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener { task ->
            if (task.isSuccessful){
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            }else {
                Toast.makeText(application, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun login(email: String?, password: String?) {
        auth.signInWithEmailAndPassword(email!!, password!!).addOnCompleteListener { task ->
            if (task.isSuccessful){
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            }else {
                Toast.makeText(application, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut(){
        auth.signOut()
        userLoggedInMutableLiveData.postValue(true)
    }

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }
}