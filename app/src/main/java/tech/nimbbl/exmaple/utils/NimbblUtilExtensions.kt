package tech.nimbbl.exmaple.utils

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.widget.Toast
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlPP
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlPROD
import tech.nimbbl.exmaple.utils.Constants.Companion.baseUrlUAT


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

fun getAccessKey(url: String, header: String): String {
   // Log.d("SAN","url---->$url/////header--->$header")
    when (url) {
        baseUrlPP -> {
            when (header) {
                "your brand name and brand logo" -> {
                    return "access_key_WKO7dmkKnlwpBvdl"
                   // return "access_key_1MwvMlANM5Lqk7ry"
                }

                "your brand logo" -> {
                    return "access_key_94Y3mmW5YJEaa3dE"
                }

                "your brand name" -> {
                    return "access_key_mVG3568XjBZON0kZ"
                }
            }
        }
        baseUrlUAT -> {
            when (header) {
                "your brand name and brand logo" -> {
                    return "access_key_RqLva5xqjjWxAvQZ"
                }

                "your brand logo" -> {
                    return "access_key_Pm43nNr6RVQZA3GL"
                }

                "your brand name" -> {
                    return "access_key_j8w0yLqDzRmqp3Ba"
                }
            }
        }
        baseUrlPROD -> {
            when (header) {
                "your brand name and brand logo" -> {
                    return ""
                }

                "your brand logo" -> {
                    return ""
                }

                "your brand name" -> {
                    return ""
                }
            }
        }
    }
    return ""
}

fun getAccessSecret(url: String, header: String): String {
    when (url) {
        baseUrlPP -> {
            when (header) {
                "your brand name and brand logo" -> {
                   return "access_secret_ROG3K9DyyPOPL7kq"
                   // return "access_secret_WKO7dnm5dpeRB3dl"
                }

                "your brand logo" -> {
                    return "access_secret_rQv9VROL8RKPD3zg"
                }

                "your brand name" -> {
                    return "access_secret_6EAvqK8jg4Lwr0PD"
                }
            }
        }
        baseUrlUAT -> {
            when (header) {
                "your brand name and brand logo" -> {
                    return "access_secret_aKQvPDKGxemWjv9z"
                }

                "your brand logo" -> {
                    return "access_secret_ArL0OVMBZOnXM3zP"
                }

                "your brand name" -> {
                    return "access_secret_wlvDmqjR9njxB3JQ"
                }
            }
        }
        baseUrlPROD -> {
            when (header) {
                "your brand name and brand logo" -> {
                    return ""
                }

                "your brand logo" -> {
                    return ""
                }

                "your brand name" -> {
                    return ""
                }
            }
        }
    }
    return  ""
}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getPaymentModeCode(paymentMode:String) : String{
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

fun getBankCode(bankName:String) : String{
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

fun getWalletCode(walletName:String) : String{
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


fun getPaymentFlow(upiModeName:String) : String{
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



