package tech.nimbbl.exmaple.utils

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.widget.Toast
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlPP
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlPROD
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlQA1
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlQA2


/*
Created by Sandeep Yadav on 10/05/24.
Copyright (c) 2024 Bigital Technologies Pvt. Ltd. All rights reserved.
*/


fun Activity.displayToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}


/**
 * Hide some cell phone numbers
 * @param phone
 * @return
 */
fun hidePhoneNum(phone: String): String {
    return if (phone.length > 9) {
        phone.substring(0, 3) + "XXXX" + phone.substring(7)
    } else {
        phone
    }
}

fun drawCircle(backgroundColor: Int, borderColor: Int): GradientDrawable {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.OVAL
    shape.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    shape.setColor(backgroundColor)
    shape.setStroke(10, borderColor)
    return shape
}

fun getProductID(header: String): String {
    when (header) {
        "your brand name and brand logo" -> {
            return "11"
        }

        "your brand logo" -> {
            return "12"
        }

        "your brand name" -> {
            return "13"
        }
    }
    return ""
}

fun getShopUrl(url: String): String {
    when (url) {
        baseUrlPP -> {
            return "https://sonicshopapipp.nimbbl.tech/"
        }

        baseUrlPROD -> {
            return "https://sonicshopapi.nimbbl.tech/"
        }

        baseUrlQA1 -> {
            return "https://qa1sonicshopapi.nimbbl.tech/"
        }
        baseUrlQA2 -> {
            return "https://qa2sonicshopapi.nimbbl.tech/"
        }
    }
    return ""
}

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getPaymentModeCode(paymentMode: String): String {
    when (paymentMode) {
        "all payments modes" -> {
            return ""
        }

        "netbanking" -> {
            return "Netbanking"
        }

        "wallet" -> {
            return "Wallet"
        }

        "card" -> {
            return "card"
        }

        "upi" -> {
            return "UPI"
        }

        else -> return ""
    }

}

fun getBankCode(bankName: String): String {
    return when (bankName) {
        "all banks" -> {
            ""
        }

        "hdfc bank" -> {
            "hdfc"
        }

        "sbi bank" -> {
            "sbi"
        }

        "kotak bank" -> {
            "kotak"
        }

        else -> ""
    }

}

fun getWalletCode(walletName: String): String {
    return when (walletName) {
        "all wallets" -> {
            ""
        }

        "freecharge" -> {
            "freecharge"
        }

        "jio money" -> {
            "jio_money"
        }

        "phonepe" -> {
            "phonepe"
        }

        else -> ""
    }

}


fun getPaymentFlow(upiModeName: String): String {
    return when (upiModeName) {
        "collect + intent" -> {
            "phonepe"
        }

        "collect" -> {
            "collect"
        }

        "intent" -> {
            "intent"
        }

        else -> ""
    }

}



