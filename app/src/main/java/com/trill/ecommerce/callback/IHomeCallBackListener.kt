package com.trill.ecommerce.callback

import com.trill.ecommerce.model.HomeAdModel

interface IHomeCallBackListener {
    fun onHomeAdLoadSuccess(adsList: List<HomeAdModel>)
    fun onHomeAdLoadFailed(message: String)

}