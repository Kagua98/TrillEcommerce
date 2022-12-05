package com.trill.ecommerce.callback

import com.trill.ecommerce.model.OrderModel

interface ILoadOrderCallBackListener {

    fun onLoadOrderSuccess(orderList: List<OrderModel>)
    fun onLoadOrderFailure(message: String)
}