package com.trill.ecommerce.api

import android.database.Observable
import com.trill.ecommerce.model.BrainTreeToken
import com.trill.ecommerce.model.BrainTreeTransaction
import io.reactivex.Flowable

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ICloudFunctions {

    @GET("token")
    fun getToken(): io.reactivex.Observable<BrainTreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(@Field("amount") amount: Long,
                      @Field("payment_method_nonce") nonce: String) : io.reactivex.Observable<BrainTreeTransaction>

}