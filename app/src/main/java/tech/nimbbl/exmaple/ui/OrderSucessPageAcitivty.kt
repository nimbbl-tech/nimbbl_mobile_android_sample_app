package tech.nimbbl.exmaple.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import tech.nimbbl.exmaple.R

class OrderSucessPageAcitivty : AppCompatActivity() {
      var order_id :String? = null
    var status :String? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_sucess_page)
        val txt_orderid = findViewById<TextView>(R.id.txt_orderid)
        val txt_status = findViewById<TextView>(R.id.txt_status)
        try {

            order_id = intent.getStringExtra("orderid")!!
            status = intent.getStringExtra("status")!!
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (order_id!=null)
            txt_orderid.setText("Order id:- $order_id")
        txt_status.setText("Status:- $status")

    }
}