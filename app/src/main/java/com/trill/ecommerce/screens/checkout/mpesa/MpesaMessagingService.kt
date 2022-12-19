package com.trill.ecommerce.screens.checkout.mpesa

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.trill.ecommerce.screens.checkout.CheckoutFragment

class MpesaMessagingService : FirebaseMessagingService()  {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val myPayload = remoteMessage.data["payload"]
            val gson = Gson()

            val mpesaResponse: Transaction = gson.fromJson(myPayload, Transaction::class.java)

            val topicID = mpesaResponse.body.stkCallback.checkoutRequestID

            if (mpesaResponse.body.stkCallback.resultCode != 0) {
                val cause = mpesaResponse.body.stkCallback.resultDesc
                CheckoutFragment.mpesaListener.sendingFailed(cause)
                Log.d("Messaging Service: ", cause.toString())
                Log.e("Failed: ", cause)

            } else {
                val infoList: List<Transaction.Body.StkCallback.CallbackMetadata.Item> =
                    mpesaResponse.body.stkCallback.callbackMetadata.item

                Log.d("Messaging Service: ", infoList.toString())

                var dateOfTransaction = ""
                var amountTransacted = ""
                var receiptNo = ""
                var phoneNumber = ""

                infoList.forEach { transaction ->
                    if (transaction.name == "MpesaReceiptNumber") {
                        receiptNo = transaction.value
                    }
                    if (transaction.name == "TransactionDate") {
                        dateOfTransaction = transaction.value
                    }
                    if (transaction.name == "PhoneNumber") {
                        phoneNumber = transaction.value
                    }
                    if (transaction.name == "Amount") {
                        amountTransacted = transaction.value
                    }
                }


                Log.d("Amount Transacted: ", amountTransacted)
                Log.d("Receipt No: ", receiptNo)

                CheckoutFragment.mpesaListener.sendingSuccessful(
                    amountTransacted,
                    phoneNumber,
                    extractDate(dateOfTransaction),
                    receiptNo
                )
            }

            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicID)
        }
    }

    private fun extractDate(date: String): String {
        return "${date.subSequence(6, 8)}${date.subSequence(4, 6)} ${
            date.subSequence(0, 4)
        } at ${date.subSequence(8, 10)}:${date.subSequence(10, 12)}:${date.subSequence(12, 14)}"
    }
}