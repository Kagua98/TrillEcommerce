package com.trill.ecommerce.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.trill.ecommerce.callback.IHomeCallBackListener
import com.trill.ecommerce.model.HomeAdModel
import com.trill.ecommerce.util.Common

class HomeViewModel : ViewModel(), IHomeCallBackListener {
    override fun onHomeAdLoadSuccess(adsList: List<HomeAdModel>) {
        adsListMutable?.value = adsList
    }

    override fun onHomeAdLoadFailed(message: String) {
        messageError.value = message
    }

    private var adsListMutable: MutableLiveData<List<HomeAdModel>>? = null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val homeAdCallBackListener: IHomeCallBackListener

    init {
        homeAdCallBackListener = this
    }

    fun getHomeAdsList(): MutableLiveData<List<HomeAdModel>> {
        if (adsListMutable == null) {
            adsListMutable = MutableLiveData()
            loadAds()
        }
        return adsListMutable!!
    }

    fun getMessageError(): MutableLiveData<String> {
        return messageError
    }

    private fun loadAds() {
        val tempList = ArrayList<HomeAdModel>()
        val homeAdsReference = FirebaseDatabase.getInstance().getReference(Common.HOME_ADS_REF)
        homeAdsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (itemSnapshot in snapshot!!.children) {
                    val model = itemSnapshot.getValue<HomeAdModel>(HomeAdModel::class.java)
                    model!!.id = itemSnapshot.key
                    tempList.add(model!!)
                }

                homeAdCallBackListener.onHomeAdLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                homeAdCallBackListener.onHomeAdLoadFailed((error.message!!))
            }

        })
    }
}