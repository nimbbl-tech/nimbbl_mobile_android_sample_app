package tech.nimbbl.exmaple.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.utils.AppConstants.EXTRA_ORDER_ID
import tech.nimbbl.exmaple.utils.AppConstants.EXTRA_STATUS

class OrderSucessPageAcitivty : AppCompatActivity() {
    var order_id: String? = null
    var status: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove enableEdgeToEdge() since we want app bar below status bar

        setContentView(R.layout.activity_order_sucess_page)

        // Set status bar color to black using modern approach (after setContentView)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.setSystemBarsAppearance(
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            @Suppress("DEPRECATION")
            window.statusBarColor = getColor(R.color.black)
        }

        // Setup safe area handling to position content below status bar
        setupSafeArea()

        // Setup back button
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val txt_orderid = findViewById<TextView>(R.id.txt_orderid)
        val txt_status = findViewById<TextView>(R.id.txt_status)
        try {

            order_id = intent.getStringExtra(EXTRA_ORDER_ID)!!
            status = intent.getStringExtra(EXTRA_STATUS)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (order_id != null)
            txt_orderid.setText(getString(R.string.order_id_label, order_id))
        txt_status.setText(getString(R.string.status_label, status))

    }

    private fun setupSafeArea() {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            // Add top padding to position app bar below status bar
            // This ensures the app bar is not hidden behind the status bar
            v.setPadding(
                systemBars.left + displayCutout.left,
                systemBars.top + displayCutout.top, // Add top padding for status bar
                systemBars.right + displayCutout.right,
                systemBars.bottom + displayCutout.bottom
            )
            insets
        }
    }
}