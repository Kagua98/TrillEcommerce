package com.trill.ecommerce.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(application: Application): AndroidViewModel(application) {

    private val repository: AuthRepository = AuthRepository(application)
    val userData: MutableLiveData<FirebaseUser?> = repository.firebaseUserMutableLiveData
    val loggedStatus: MutableLiveData<Boolean> = repository.userLoggedInMutableLiveData

    fun register(email: String?, password: String?){
        repository.register(email, password)
    }

    fun login(email: String?, password: String?){
        repository.login(email, password)
    }

    fun signOut(){
        repository.signOut()
    }

}