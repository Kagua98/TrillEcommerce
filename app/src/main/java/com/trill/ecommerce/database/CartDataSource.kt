package com.trill.ecommerce.database

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface CartDataSource {

    fun getCartItems(uid: String): Flowable<List<CartItem>>

    fun countCartItems(uid: String): Single<Int>

    fun totalPrice(uid: String): Single<Long>

    fun getItemInCart(productId: String, uid: String): Single<CartItem>

    fun insertOrReplaceAll(vararg cartItems: CartItem): Completable

    fun updateCart(cart: CartItem): Single<Int>

    fun deleteCart(cart: CartItem): Single<Int>

    fun cleanCart(uid: String): Single<Int>

    fun getItemWithAllOptionsInCart(uid: String, productId : String): Single<CartItem>
}