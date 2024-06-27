package tech.nimbbl.exmaple.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.utils.drawCircle


class headerCustomisationSpinAdapter(context: Context?, private val itemList: Array<String>) :
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
                LayoutInflater.from(context).inflate(R.layout.custom_spinner_adapter, parent, false)
        }
        val textViewName = convertView?.findViewById<TextView>(R.id.text_view)
        val ivCircle = convertView?.findViewById<ImageView>(R.id.image_view)
        val currentItem = itemList[position]

        // It is used the name to the TextView when the
        // current item is not null.
        if (textViewName != null) {
            textViewName.text = currentItem
            if (ivCircle != null) {
                if (currentItem == context.resources.getStringArray(R.array.option_enabled)[0]) {
                    ivCircle.background = drawCircle(
                        context.resources.getColor(R.color.dark_blue),
                        context.resources.getColor(R.color.dark_blue)
                    )
                } else if (currentItem == context.resources.getStringArray(R.array.option_enabled)[1]) {
                    ivCircle.background = drawCircle(
                        context.resources.getColor(R.color.light_blue),
                        context.resources.getColor(R.color.light_blue)
                    )
                } else if (currentItem == context.resources.getStringArray(R.array.option_disabled)[0]) {
                    ivCircle.background = drawCircle(
                        context.resources.getColor(R.color.red),
                        context.resources.getColor(R.color.red)
                    )
                }
            }
        }
        return convertView!!
    }
}

