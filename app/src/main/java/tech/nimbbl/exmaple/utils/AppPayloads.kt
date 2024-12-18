package tech.nimbbl.exmaple.utils

import org.json.JSONObject
import tech.nimbbl.coreapisdk.models.Item
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_completePayment
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_getBinData
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_initiateOrder
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_initiatePayment
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_paymentEnquiry
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_paymentModes
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_resendOtp
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_resolveUser
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_validateCard
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_verifyUser
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_OrderID
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_action
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_app_package_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_cvv
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_expiry
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_holder_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_number
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_flow
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_mobileNumber
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_nimbblPayload
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_otp
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_packageName
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_payment_mode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_payment_type
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_sub_payment_mode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_token
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_transaction_id
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_upi_id
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_payment_mode_upi
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_upi_flow_mode_collect

/*
Created by Sandeep Yadav on 08/05/24.
Copyright (c) 2024 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

object AppPayloads {

    fun createOrderPayload(
        totalAmount: Int,
        emailId: String,
        firstname: String,
        mobileNumber: String,
        productId: String,
        paymentMode: String,
        subPaymentMode: String,
    ): JSONObject {
        val createOrderPayload = JSONObject()
        try {
            createOrderPayload.put("currency", "INR")
            createOrderPayload.put("amount", totalAmount.toString())
            createOrderPayload.put("product_id", productId)
            createOrderPayload.put("orderLineItems", true)
            createOrderPayload.put("checkout_experience", "redirect")
            createOrderPayload.put("payment_mode", paymentMode.ifEmpty { "All" })
            createOrderPayload.put("subPaymentMode", subPaymentMode)

            val userJsonObj = JSONObject()
            userJsonObj.put("email", emailId)
            userJsonObj.put("name", firstname)
            userJsonObj.put("mobile_number", mobileNumber)
            if(mobileNumber.isNotEmpty()) {
                createOrderPayload.put("user", userJsonObj)
            }




        } catch (e: Exception) {
            e.printStackTrace()
        }
        return createOrderPayload
    }

    fun initResourcePayload(
        token: String,
        packageName: String
    ): JSONObject {
        val inputInItPayload = JSONObject()
        try {
            inputInItPayload.put(key_token, token)
            val nimbblJsonPayload = JSONObject()
            nimbblJsonPayload.put(key_packageName, packageName)
            inputInItPayload.put(key_nimbblPayload, nimbblJsonPayload)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inputInItPayload
    }

    fun initialiseOrderPayload(
        token: String,
        orderID: String
    ): JSONObject {
        val initiatePayload = JSONObject()
        try {
            initiatePayload.put(key_action, action_initiateOrder)
            initiatePayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            initiatePayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return initiatePayload
    }

    fun paymentModesPayload(
        token: String,
        orderID: String
    ): JSONObject {
        val initiatePayload = JSONObject()
        try {
            initiatePayload.put(key_action, action_paymentModes)
            initiatePayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            initiatePayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return initiatePayload
    }

    fun initiatePaymentPayload(
        payment_mode: String,
        item: Item,
        token: String,
        orderID: String
    ): JSONObject {
        val initiatePaymentPayload = JSONObject()
        try {
            initiatePaymentPayload.put(key_action, action_initiatePayment)
            initiatePaymentPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, payment_mode)
            nimbblPayloadObj.put(key_sub_payment_mode, item.sub_payment_name)
            initiatePaymentPayload.put(key_nimbblPayload, nimbblPayloadObj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return initiatePaymentPayload
    }

    fun resolveUserPayload(
        token: String,
        orderID: String
    ): JSONObject {
        val resolveUserPayload = JSONObject()
        try {
            resolveUserPayload.put(key_action, action_resolveUser)
            resolveUserPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            resolveUserPayload.put(key_nimbblPayload, nimbblPayloadObj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resolveUserPayload
    }

    fun verifyUserPayload(
        token: String,
        orderID: String,
        mobileNo: String,
        otp: String
    ): JSONObject {
        val resolveUserPayload = JSONObject()
        try {
            resolveUserPayload.put(key_action, action_verifyUser)
            resolveUserPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_mobileNumber, mobileNo)
            nimbblPayloadObj.put(key_otp, otp)
            resolveUserPayload.put(key_nimbblPayload, nimbblPayloadObj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resolveUserPayload
    }

    fun getBinDataCardDetailPayload(
        cardNumber: String,
        cardExpiry: String,
        cardCvv: String,
        cardHolderName: String,
        token: String,
        orderID: String
    ): JSONObject {
        val cardDetailPayload = JSONObject()
        try {
            cardDetailPayload.put(key_action, action_getBinData)
            cardDetailPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_card_number, cardNumber)
            nimbblPayloadObj.put(key_card_cvv, cardCvv)
            nimbblPayloadObj.put(key_card_expiry, cardExpiry)
            nimbblPayloadObj.put(key_card_holder_name, cardHolderName)
            cardDetailPayload.put(key_nimbblPayload, nimbblPayloadObj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cardDetailPayload
    }

    fun validateCardDetailPayload(
        token: String,
        orderID: String,
        cardNumber: String,
        cardExpiry: String,
        cardCvv: String,
        cardHolderName: String
    ): JSONObject {
        val cardDetailPayload = JSONObject()
        try {
            cardDetailPayload.put(key_action, action_validateCard)
            cardDetailPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_card_number, cardNumber)
            nimbblPayloadObj.put(key_card_cvv, cardCvv)
            nimbblPayloadObj.put(key_card_expiry, cardExpiry)
            nimbblPayloadObj.put(key_card_holder_name, cardHolderName)
            cardDetailPayload.put(key_nimbblPayload, nimbblPayloadObj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cardDetailPayload
    }

    fun initiateCardPaymentPayload(
        cardNumber: String,
        cardExpiry: String,
        cardCvv: String,
        cardHolderName: String,
        payment_mode: String,
        sub_payment_mode: String,
        token: String,
        orderID: String
    ): JSONObject {
        val initiatePaymentPayload = JSONObject()
        try {
            initiatePaymentPayload.put(key_action, action_initiatePayment)
            initiatePaymentPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, payment_mode)
            nimbblPayloadObj.put(key_sub_payment_mode, sub_payment_mode)
            nimbblPayloadObj.put(key_card_number, cardNumber)
            nimbblPayloadObj.put(key_card_cvv, cardCvv)
            nimbblPayloadObj.put(key_card_expiry, cardExpiry)
            nimbblPayloadObj.put(key_card_holder_name, cardHolderName)
            initiatePaymentPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return initiatePaymentPayload
    }

    fun binDataRequestPayload(
        cardNo: String,
        token: String,
        orderID: String
    ): JSONObject {
        val cardBinDataPayload = JSONObject()
        try {
            cardBinDataPayload.put(key_action, action_getBinData)
            cardBinDataPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_card_number, cardNo)
            cardBinDataPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cardBinDataPayload
    }

    fun getResendOtpRequestPayload(
        token: String,
        orderID: String,
        paymentMode: String,
        transactionId: String
    ): JSONObject {
        val resendOtpPayload = JSONObject()
        try {
            resendOtpPayload.put(key_action, action_resendOtp)
            resendOtpPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, paymentMode)
            nimbblPayloadObj.put(key_transaction_id, transactionId)
            resendOtpPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resendOtpPayload
    }

    fun getTransactionEnquiryRequestPayload(
        token: String,
        orderID: String,
        paymentMode: String,
        transactionId: String
    ): JSONObject {
        val transactionEnqPayload = JSONObject()
        try {
            transactionEnqPayload.put(key_action, action_paymentEnquiry)
            transactionEnqPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, paymentMode)
            nimbblPayloadObj.put(key_transaction_id, transactionId)
            transactionEnqPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return transactionEnqPayload
    }

    fun getCompletePaymentRequestPayload(
        token: String,
        orderID: String,
        paymentMode: String,
        paymentType:String,
        transactionId: String,
        otp: String = "",
        upiId: String = "",
        flow: String = ""
    ): JSONObject {
        val completePaymentPayload = JSONObject()
        try {
            completePaymentPayload.put(key_action, action_completePayment)
            completePaymentPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, paymentMode)
            nimbblPayloadObj.put(key_payment_type, paymentType)
            nimbblPayloadObj.put(key_transaction_id, transactionId)
            nimbblPayloadObj.put(key_otp,otp)
            nimbblPayloadObj.put(key_upi_id,upiId)
            nimbblPayloadObj.put(key_flow,flow)
            completePaymentPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return completePaymentPayload
    }

    fun getUpiIntentCompletePaymentRequestPayload(
        token: String,
        orderID: String,
        paymentMode: String,
        subPaymentMode: String,
        flow: String,
        appPackageName: String
    ): JSONObject {
        val upiIntentCompletePaymentPayload = JSONObject()
        try {
            upiIntentCompletePaymentPayload.put(key_action, action_completePayment)
            upiIntentCompletePaymentPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, paymentMode)
            nimbblPayloadObj.put(key_sub_payment_mode, subPaymentMode)
            nimbblPayloadObj.put(key_app_package_name, appPackageName)
            nimbblPayloadObj.put(key_flow,flow)
            nimbblPayloadObj.put(key_payment_type, "")
            nimbblPayloadObj.put(key_transaction_id, "")
            nimbblPayloadObj.put(key_otp,"")
            nimbblPayloadObj.put(key_upi_id,"")
            upiIntentCompletePaymentPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return upiIntentCompletePaymentPayload
    }

    fun upiInitiatePaymentRequestPayload(
        vpaId: String,
        token: String,
        orderID: String
    ): JSONObject {
        val upiReqPayload = JSONObject()
        try {
            upiReqPayload.put(key_action, action_initiatePayment)
            upiReqPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, value_payment_mode_upi)
            nimbblPayloadObj.put(key_flow,value_upi_flow_mode_collect)
            nimbblPayloadObj.put(key_upi_id,vpaId)
            upiReqPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return upiReqPayload


    }

    fun getUpiCompletePaymentRequestPayload(
        token: String,
        orderID: String,
        vpaId: String
    ): JSONObject {
        val upiReqPayload = JSONObject()
        try {
            upiReqPayload.put(key_action, action_completePayment)
            upiReqPayload.put(key_token, token)
            val nimbblPayloadObj = JSONObject()
            nimbblPayloadObj.put(key_OrderID, orderID)
            nimbblPayloadObj.put(key_payment_mode, value_payment_mode_upi)
            nimbblPayloadObj.put(key_flow,value_upi_flow_mode_collect)
            nimbblPayloadObj.put(key_upi_id,vpaId)
            upiReqPayload.put(key_nimbblPayload, nimbblPayloadObj)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return upiReqPayload
    }


}