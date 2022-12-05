package com.trill.ecommerce.database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class LocalCartDataSource(private val cartDAO: CartDAO) : CartDataSource {
    override fun getCartItems(uid: String): Flowable<List<CartItem>> {
        return cartDAO.getCartItems(uid)
    }

    override fun countCartItems(uid: String): Single<Int> {
        return cartDAO.countCartItems(uid)
    }

    override fun totalPrice(uid: String): Single<Long> {
        return cartDAO.totalPrice(uid)
    }

    override fun getItemInCart(productId: String, uid: String): Single<CartItem> {
        return cartDAO.getItemInCart(productId, uid)
    }

    override fun insertOrReplaceAll(vararg cartItems: CartItem): Completable {
        return cartDAO.insertOrReplaceAll(*cartItems)
    }

    override fun updateCart(cart: CartItem): Single<Int> {
        return cartDAO.updateCart(cart)
    }

    override fun deleteCart(cart: CartItem): Single<Int> {
        return cartDAO.deleteCart(cart)
    }

    override fun cleanCart(uid: String): Single<Int> {
        return cartDAO.cleanCart(uid)
    }

    override fun getItemWithAllOptionsInCart(uid: String, productId: String): Single<CartItem> {
        return cartDAO.getItemWithAllOptionsInCart(uid,  productId)
    }
}