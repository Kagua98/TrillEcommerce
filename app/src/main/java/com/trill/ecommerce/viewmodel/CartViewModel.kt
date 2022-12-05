package com.trill.ecommerce.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trill.ecommerce.database.CartDataSource
import com.trill.ecommerce.database.CartDatabase
import com.trill.ecommerce.database.CartItem
import com.trill.ecommerce.database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable
    private var cartDataSource: CartDataSource? = null
    private var mutableLiveDataCartItem: MutableLiveData<List<CartItem>>? = null
    private lateinit var auth: FirebaseAuth

    init {
        compositeDisposable = CompositeDisposable()
        auth = Firebase.auth
    }

    fun initCartDataSource(context: Context) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems() {
        val uid = auth.currentUser!!.uid
        compositeDisposable.addAll(cartDataSource!!.getCartItems(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cartItems ->
                mutableLiveDataCartItem!!.value = cartItems
            }, { t: Throwable? -> mutableLiveDataCartItem!!.value = null })
        )
    }

    fun onStop() {
        compositeDisposable.clear()
    }
}