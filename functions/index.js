const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);
const express = require('express');
const body_parser = require('body-parser');

const app = express();
app.use(body_parser.json());
app.disable('x-powered-by');

app.post('/CallbackUrl', (request, result) => {
    let response = { "ResultCode": 0, "ResultDesc": "Success" }

    result.status(200).json(response);

    let requestBody = request.body;
    let myPayload = JSON.stringify(requestBody)

    // Logs successful function calls
    console.log(myPayload)

    let topicId =  body.Body.stkCallback.CheckoutRequestID

      const sentPayload = {
          data: {
            myPayload,
            },
            topicId : id
        };

         return admin.messaging().send(sentPayload).catch(error=>{
         
            // Logs Failed function calls    
         console.error(error)
         })
})

exports.api = functions.https.onRequest(app);