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
        token: String,
        skuTitle: String,
        skuAmount: Int,
        skuDesc: String,
        userFirstName: String,
        userLastName: String,
        userEmailId: String,
        userMobileNumber: String,
        useraddressLine1: String,
        userAddrStreet: String,
        userAddrLandmark: String,
        userAddrArea: String,
        userAddrCity: String,
        userAddrState: String,
        userAddrPin: String
    ): Response<CreateOrderResponse> {
        val createOrderObj = AppPayloads.createOrderPayload(skuAmount,userEmailId,userFirstName,userLastName,userMobileNumber,useraddressLine1,userAddrStreet,userAddrLandmark,userAddrArea,userAddrCity,userAddrState,userAddrPin, skuTitle,skuDesc)
        val body: RequestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            createOrderObj.toString()
        )
        return ApiCall()!!.creatOrder(url,"Bearer $token", body)
    }

    suspend fun generateToken(
        url: String,
        body: RequestBody
    ): Response<GenerateTokenResponse> {
        return ApiCall()!!.generateToken(url, body)
    }
}