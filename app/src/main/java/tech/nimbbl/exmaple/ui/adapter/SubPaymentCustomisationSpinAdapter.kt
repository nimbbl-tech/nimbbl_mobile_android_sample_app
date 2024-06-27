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
                ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.upi, context.theme)
                when (currentItem) {

                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[0] -> {
                        ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.grid, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[1] -> {
                        ivCircle.background =  ResourcesCompat.getDrawable(context.resources, R.drawable.hdfc, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[2] -> {
                        ivCircle.background =  ResourcesCompat.getDrawable(context.resources, R.drawable.sbi, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_netbanking)[3] -> {
                        ivCircle.background =  ResourcesCompat.getDrawable(context.resources, R.drawable.kotak, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[0] -> {
                        ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.grid, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[1] -> {
                        ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.freecharge, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[2] -> {
                        ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.jiomoney, context.theme)
                    }
                    context.resources.getStringArray(R.array.sub_payment_type_wallet)[3] -> {
                        ivCircle.background = ResourcesCompat.getDrawable(context.resources, R.drawable.phonepe, context.theme)
                    }
                }
            }
        }
        return convertView!!
    }
}

