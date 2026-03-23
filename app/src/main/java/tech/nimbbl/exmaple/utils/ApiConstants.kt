package tech.nimbbl.exmaple.utils

/*
Created by Sandeep Yadav on 26/02/26.
Copyright (c) 2026 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

/**
 * API URL constants for the sample app
 * These are used for environment selection and shop order creation in the sample app only
 */
object ApiConstants {
    // Production URL
    const val NIMBBL_TECH_URL = "https://api.nimbbl.tech/"

    // Pre-Production URL
    const val BASE_URL_PRE_PROD = "https://apipp.nimbbl.tech/"

    // QA1 URL (Default QA Environment)
    const val BASE_URL_QA1 = "https://qa1api.qa.nimbbl.tech/"

    // Shop Order URLs - Used by sample app for shop order creation
    const val SHOP_ORDER_URL_QA1 = "https://qa1sonicshopapi.qa.nimbbl.tech/create-shop"
    const val SHOP_ORDER_URL_PROD = "https://sonicshopapi.nimbbl.tech/create-shop"
    const val SHOP_ORDER_URL_PRE_PROD = "https://sonicshopapipp.nimbbl.tech/create-shop"
}

