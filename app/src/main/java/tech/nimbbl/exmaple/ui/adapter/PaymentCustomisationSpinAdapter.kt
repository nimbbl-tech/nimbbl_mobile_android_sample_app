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


class PaymentCustomisationSpinAdapter(context: Context?, private var itemList: Array<String>) :
    ArrayAdapter<String?>(
        context!!, 0, itemList
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    fun setData(itemList: Array<String>){
        this.itemList = itemList
        notifyDataSetChanged()
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
        if(position<itemList.size) {
            val currentItem = itemList[position]

            // It is used the name to the TextView when the
            // current item is not null.
            if (textViewName != null) {
                textViewName.text = currentItem
                if (ivCircle != null) {
                    when (currentItem) {
                        context.resources.getStringArray(R.array.payment_type)[0] -> {
                            ivCircle.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.grid,
                                context.theme
                            )
                        }

                        context.resources.getStringArray(R.array.payment_type)[1] -> {
                            ivCircle.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.netbanking,
                                context.theme
                            )
                        }

                        context.resources.getStringArray(R.array.payment_type)[2] -> {
                            ivCircle.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.wallet,
                                context.theme
                            )
                        }

                        context.resources.getStringArray(R.array.payment_type)[3] -> {
                            ivCircle.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.card,
                                context.theme
                            )
                        }

                        context.resources.getStringArray(R.array.payment_type)[4] -> {
                            ivCircle.background = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.upi,
                                context.theme
                            )
                        }
                    }
                }
            }
        }
        return convertView!!
    }
}

