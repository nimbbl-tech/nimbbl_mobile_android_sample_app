package tech.nimbbl.exmaple.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Response
import tech.nimbbl.exmaple.model.GenerateTokenResponse
import tech.nimbbl.exmaple.model.createorder.CreateOrderResponse
import tech.nimbbl.exmaple.network.ApiCall
import tech.nimbbl.exmaple.utils.AppPayloads

class CatalogRepository {
    suspend fun createOrder(
        url: String,
        skuAmount: Int,
        userFirstName: String,
        userEmailId: String,
        userMobileNumber: String,
        productID: String,
        paymentMode: String,
        subPaymentMode: String
    ): Response<CreateOrderResponse> {
        val createOrderObj = AppPayloads.createOrderPayload(
            skuAmount,
            userEmailId,
            userFirstName,
            userMobileNumber,
            productID,
            paymentMode,
            subPaymentMode
        )
        val body: RequestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            createOrderObj.toString()
        )
        return ApiCall()!!.createOrder(url, body)
    }

    suspend fun generateToken(
        url: String,
        body: RequestBody
    ): Response<GenerateTokenResponse> {
        return ApiCall()!!.generateToken(url, body)
    }
}