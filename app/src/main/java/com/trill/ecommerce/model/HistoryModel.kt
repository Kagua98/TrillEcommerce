package com.trill.ecommerce.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trill.ecommerce.util.Common

class HistoryModel: ViewModel() {

    val mutableLiveDataOrderList: MutableLiveData<List<OrderModel>> = MutableLiveData()
    private var mutableLiveDataHistoryItem: MutableLiveData<OrderModel>? = null

    fun setMutableLiveDatOrderList(orderList: List<OrderModel>){
        mutableLiveDataOrderList.value = orderList
    }

    fun getMutableLiveDataHistoryItem(): MutableLiveData<OrderModel> {
        if (mutableLiveDataHistoryItem == null)
            mutableLiveDataHistoryItem = MutableLiveData()
        mutableLiveDataHistoryItem!!.value = Common.historyItemSelected

        return mutableLiveDataHistoryItem!!
    }

    fun setHistoryItemsModel(historyItemsModel: OrderModel) {
        if (historyItemsModel != null)
            mutableLiveDataHistoryItem!!.value = historyItemsModel
    }
}