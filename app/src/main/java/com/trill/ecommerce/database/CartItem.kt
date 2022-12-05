package com.trill.ecommerce.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart")
class CartItem {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "productID")
    var productID: String = ""

    @ColumnInfo(name = "productName")
    var productName: String? = null

    @ColumnInfo(name = "productImage")
    var productImage: String? = null

    @ColumnInfo(name = "productPrice")
    var productPrice: Long? = 0

    @ColumnInfo(name = "productQuantity")
    var productQuantity: Int? = 0

    @ColumnInfo(name = "userPhone")
    var userPhone: String? = ""

    @NonNull
    @ColumnInfo(name = "uid")
    var uid: String? = ""

    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if (other !is CartItem)
            return false
        val cartItem = other as CartItem?
        return cartItem!!.productID == this.productID

    }
}