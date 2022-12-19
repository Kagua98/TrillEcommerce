package com.trill.ecommerce.model

import com.trill.ecommerce.database.CartItem

data class OrderModel(
    var userId: String? = null,
    var userName: String? = null,
    var userPhone: String? = null,
    var shippingAddress: String? = null,
    var notes: String? = null,
    var transactionId: String? = null,
    var totalPayment: Long? = 0,
    var cartItemList: List<CartItem>? = null,
    var isCOD: Boolean = true,
    var orderNumber: String? = null,
    var orderStatus: Int? = 0,
    var date: String? = null,
    var time: String? = null,
    var contactPerson: String? = null,
    var paymentMethod: String? = null,
)