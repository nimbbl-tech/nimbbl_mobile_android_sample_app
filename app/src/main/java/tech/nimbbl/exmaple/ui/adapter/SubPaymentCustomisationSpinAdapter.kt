package tech.nimbbl.exmaple.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import tech.nimbbl.exmaple.R


class SubPaymentCustomisationSpinAdapter(context: Context?, private val itemList: Array<String>) :
    ArrayAdapter<String?>(
        context!!, 0, itemList
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    private fun initView(
        position: Int, convertView: View?,
        parent: ViewGroup
    ): View {
        // It is used to set our custom view.
        var convertView = convertView
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.payment_spinner_adapter, parent, false)
        }
        val textViewName = convertView?.findViewById<TextView>(R.id.text_view)
        val ivCircle = convertView?.findViewById<ImageView>(R.id.image_view)
        val currentItem = itemList[position]

        // It is used the name to the TextView when the
        // current item is not null.
        if (textViewName != null) {
            textViewName.text = currentItem
            if (ivCircle != null) {
                // Sub-payment icons include colored bank/wallet logos; tint only monochrome icons
                // (e.g. grid/upi) so they stay visible in dark mode.
                ivCircle.background = null
                var resId = R.drawable.upi
                when (currentItem) {

                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[0] -> {
                        resId = R.drawable.grid
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[1] -> {
                        resId = R.drawable.hdfc
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[2] -> {
                        resId = R.drawable.sbi
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[3] -> {
                        resId = R.drawable.kotak
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[0] -> {
                        resId = R.drawable.grid
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[1] -> {
                        resId = R.drawable.freecharge
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[2] -> {
                        resId = R.drawable.jiomoney
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[3] -> {
                        resId = R.drawable.phonepe
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_emi)[0] -> {
                        resId = R.drawable.grid
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_emi)[1] -> {
                        resId = R.drawable.credit_card_emi
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_emi)[2] -> {
                        resId = R.drawable.credit_card_emi
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_emi)[3] -> {
                        resId = R.drawable.cardless_emi
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_upi_intent_apps)[0] -> {
                        resId = R.drawable.gpay
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_upi_intent_apps)[1] -> {
                        resId = R.drawable.phonepe
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_upi_intent_apps)[2] -> {
                        resId = R.drawable.paytm
                    }
                }

                ivCircle.setImageDrawable(ResourcesCompat.getDrawable(context.resources, resId, context.theme))
                if (resId == R.drawable.grid || resId == R.drawable.upi) {
                    ivCircle.setColorFilter(textViewName.currentTextColor)
                } else {
                    ivCircle.clearColorFilter()
                }
            }
        }
        return convertView!!
    }
}

