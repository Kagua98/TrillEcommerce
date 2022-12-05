package com.trill.ecommerce.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryModel: ViewModel() {

    val mutableLiveDataOrderList: MutableLiveData<List<OrderModel>>

    init {
        mutableLiveDataOrderList = MutableLiveData()
    }

    fun setMutableLiveDatOrderList(orderList: List<OrderModel>){
        mutableLiveDataOrderList.value = orderList
    }
}