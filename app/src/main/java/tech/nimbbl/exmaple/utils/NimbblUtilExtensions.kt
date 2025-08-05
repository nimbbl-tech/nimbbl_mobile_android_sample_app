package tech.nimbbl.exmaple.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable

fun drawCircle(backgroundColor: Int, borderColor: Int): GradientDrawable {
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.OVAL
    shape.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    shape.setColor(backgroundColor)
    shape.setStroke(10, borderColor)
    return shape
}

fun getProductID(header: String, context: Context): String {
    when (header) {
        context.getString(tech.nimbbl.exmaple.R.string.brand_name_and_logo) -> {
            return "11"
        }
        context.getString(tech.nimbbl.exmaple.R.string.brand_logo) -> {
            return "12"
        }
        context.getString(tech.nimbbl.exmaple.R.string.brand_name) -> {
            return "13"
        }
    }
    return ""
}

fun getPaymentModeCode(paymentMode: String, context: Context): String {
    when (paymentMode) {
        context.getString(tech.nimbbl.exmaple.R.string.all_payment_modes) -> {
            return ""
        }
        context.getString(tech.nimbbl.exmaple.R.string.netbanking) -> {
            return "Netbanking"
        }
        context.getString(tech.nimbbl.exmaple.R.string.wallet) -> {
            return "Wallet"
        }
        context.getString(tech.nimbbl.exmaple.R.string.card) -> {
            return "card"
        }
        context.getString(tech.nimbbl.exmaple.R.string.upi) -> {
            return "UPI"
        }
        else -> return ""
    }
}

fun getBankCode(bankName: String, context: Context): String {
    return when (bankName) {
        context.getString(tech.nimbbl.exmaple.R.string.all_banks) -> {
            ""
        }
        context.getString(tech.nimbbl.exmaple.R.string.hdfc_bank) -> {
            "hdfc"
        }
        context.getString(tech.nimbbl.exmaple.R.string.sbi_bank) -> {
            "sbi"
        }
        context.getString(tech.nimbbl.exmaple.R.string.kotak_bank) -> {
            "kotak"
        }
        else -> ""
    }
}

fun getWalletCode(walletName: String, context: Context): String {
    return when (walletName) {
        context.getString(tech.nimbbl.exmaple.R.string.all_wallets) -> {
            ""
        }
        context.getString(tech.nimbbl.exmaple.R.string.freecharge) -> {
            "freecharge"
        }
        context.getString(tech.nimbbl.exmaple.R.string.jio_money) -> {
            "jio_money"
        }
        context.getString(tech.nimbbl.exmaple.R.string.phonepe) -> {
            "phonepe"
        }
        else -> ""
    }
}

fun getPaymentFlow(upiModeName: String, context: Context): String {
    return when (upiModeName) {
        context.getString(tech.nimbbl.exmaple.R.string.collect_intent) -> {
            "phonepe"
        }
        context.getString(tech.nimbbl.exmaple.R.string.collect) -> {
            "collect"
        }
        context.getString(tech.nimbbl.exmaple.R.string.intent) -> {
            "intent"
        }
        else -> ""
    }
}



