package com.trill.ecommerce.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface CartDAO {

    @Query("SELECT * FROM Cart WHERE uid=:uid")
    fun getCartItems(uid : String): Flowable<List<CartItem>>

    @Query("SELECT SUM(productQuantity) FROM Cart WHERE uid=:uid")
    fun countCartItems(uid : String): Single<Int>

    @Query("SELECT SUM(productQuantity*productPrice) FROM Cart WHERE uid=:uid")
    fun totalPrice(uid : String): Single<Long>

    @Query("SELECT * FROM Cart WHERE productID=:productID AND uid=:uid")
    fun getItemInCart(productID: String, uid : String): Single<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItems: CartItem): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(cart: CartItem): Single<Int>

    @Delete
    fun deleteCart(cart : CartItem) : Single<Int>

    @Query("DELETE FROM Cart WHERE uid=:uid")
    fun cleanCart(uid : String): Single<Int>

    @Query("SELECT * FROM Cart WHERE productID=:productID AND uid=:uid")
    fun getItemWithAllOptionsInCart(uid: String, productID : String): Single<CartItem>
}