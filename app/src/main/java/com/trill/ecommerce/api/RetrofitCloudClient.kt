package com.trill.ecommerce.api


import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Headers

object RetrofitCloudClient {
    private var instance: Retrofit? = null

    fun getInstance(): Retrofit{
        if (instance == null)
            instance = Retrofit.Builder()
                .baseUrl("https://us-central1-trill-ecommerce.cloudfunctions.net/widget/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        return instance!!
    }
}