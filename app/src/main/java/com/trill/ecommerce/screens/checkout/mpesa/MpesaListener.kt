package com.trill.ecommerce.screens.checkout.mpesa

interface MpesaListener {
    fun sendingSuccessful(transactionAmount: String, phoneNumber: String, transactionDate: String, MPesaReceiptNo: String)

    fun sendingFailed(cause: String)
}