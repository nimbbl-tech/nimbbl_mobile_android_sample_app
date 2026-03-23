package tech.nimbbl.exmaple.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import tech.nimbbl.exmaple.R

class OrderSucessPageAcitivty : AppCompatActivity() {
    var rawJsonData: String? = null

    // Parsed data from complex JSON
    private var parsedPaymentData: MutableMap<String, Any> = mutableMapOf()

    // Constants
    companion object {
        private const val ENCRYPTED_RESPONSE_MESSAGE = "Encrypted response received. Please handle decryption on your server."
    }

    // Data class for detailed status information
    private data class DetailedStatusInfo(
        val reason: String = "",
        val cancellationReason: String = "",
        val attempts: String = "",
        val referrerPlatform: String = "",
        val referrerPlatformVersion: String = "",
        val deviceName: String = "",
        val deviceOsName: String = "",
        val deviceIpAddress: String = "",
        val shippingCity: String = "",
        val shippingState: String = "",
        val shippingCountry: String = "",
        val shippingPincode: String = ""
    )

    // UI Elements
    private lateinit var statusIcon: ImageView
    private lateinit var statusTitle: TextView
    private lateinit var statusMessage: TextView
    private lateinit var txtOrderId: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtAmount: TextView
    private lateinit var txtInvoiceId: TextView
    private lateinit var txtTransactionId: TextView
    private lateinit var txtOrderDate: TextView
    private lateinit var txtDetailedStatus: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_sucess_page)

        // Setup toolbar
        setupToolbar()

        // Setup safe area handling
        setupSafeArea()

        // Initialize UI elements
        initializeUIElements()

        // Parse and display payment data
        parseAndDisplayPaymentData()

        // Setup action buttons
        setupActionButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeUIElements() {
        // Initialize all UI elements
        val statusIcon = findViewById<ImageView>(R.id.status_icon)
        val statusTitle = findViewById<TextView>(R.id.status_title)
        val statusMessage = findViewById<TextView>(R.id.status_message)
        val txtOrderId = findViewById<TextView>(R.id.txt_orderid)
        val txtStatus = findViewById<TextView>(R.id.txt_status)
        val txtAmount = findViewById<TextView>(R.id.txt_amount)
        val txtInvoiceId = findViewById<TextView>(R.id.txt_invoice_id)
        val txtTransactionId = findViewById<TextView>(R.id.txt_transaction_id)
        val txtOrderDate = findViewById<TextView>(R.id.txt_order_date)
        val txtDetailedStatus = findViewById<TextView>(R.id.txt_detailed_status)

        // Store references for later use
        this.statusIcon = statusIcon
        this.statusTitle = statusTitle
        this.statusMessage = statusMessage
        this.txtOrderId = txtOrderId
        this.txtStatus = txtStatus
        this.txtAmount = txtAmount
        this.txtInvoiceId = txtInvoiceId
        this.txtTransactionId = txtTransactionId
        this.txtOrderDate = txtOrderDate
        this.txtDetailedStatus = txtDetailedStatus
    }

    private fun parseAndDisplayPaymentData() {
        try {
            // Try to get raw JSON data if available
            rawJsonData = intent.getStringExtra("raw_json_data")

            // Parse complex JSON if available
            if (!rawJsonData.isNullOrEmpty()) {
                Log.d("SAN", rawJsonData.toString())
                try {
                    parsedPaymentData = parseComplexPaymentResponse(rawJsonData!!)
                    Log.d("OrderSuccess", "Parsed payment data: $parsedPaymentData")
                } catch (e: Exception) {
                    Log.e("OrderSuccess", "Error parsing payment response: ${e.message}")
                    Log.e("OrderSuccess", "Raw JSON data: ${rawJsonData!!.take(200)}...")
                    // Initialize with empty map as fallback
                    parsedPaymentData = mutableMapOf()
                }
            }

        } catch (e: Exception) {
            Log.e("OrderSuccess", "Error getting intent data: ${e.message}", e)
        }

        // Display order information
        displayOrderInformation()
    }

    private fun setupActionButtons() {
        val btnPrimaryAction = findViewById<MaterialButton>(R.id.btn_primary_action)
        val btnSecondaryAction = findViewById<MaterialButton>(R.id.btn_secondary_action)

        btnPrimaryAction.setOnClickListener {
            // Handle primary action (Try Again)
            handlePrimaryAction()
        }

        btnSecondaryAction.setOnClickListener {
            // Handle secondary action (Back to Home)
            handleSecondaryAction()
        }
    }

    private fun handlePrimaryAction() {
        // All actions currently lead to finishing the activity
        // Future enhancements can add specific handling per status if needed
        finish()
    }

    private fun handleSecondaryAction() {
        // Go back to home/main screen
        finish()
    }


    /**
     * Parse payment response - simplified version
     */
    private fun parseComplexPaymentResponse(jsonString: String): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()

        if (jsonString.isBlank()) {
            Log.w("OrderSuccess", "Empty JSON string provided")
            return result
        }

        try {
            val paymentData = JSONObject(jsonString)

            // Check for encrypted response
            if (paymentData.has("encrypted_response")) {
                result["is_encrypted"] = true
                result["encrypted_response"] = safeGetString(paymentData, "encrypted_response", "")
                result["status"] = "encrypted"
                result["message"] = ENCRYPTED_RESPONSE_MESSAGE
                return result
            }

            // Extract basic payment information
            result["status"] = safeGetString(paymentData, "status", "unknown")
            result["message"] = safeGetString(paymentData, "message", "")
            result["order_id"] = safeGetString(paymentData, "order_id", "")
            result["nimbbl_order_id"] = safeGetString(paymentData, "nimbbl_order_id", "")
            result["transaction_id"] = safeGetString(paymentData, "transaction_id", "")
            result["nimbbl_transaction_id"] = safeGetString(paymentData, "nimbbl_transaction_id", "")
            result["signature"] = safeGetString(paymentData, "signature", "")
            result["nimbbl_signature"] = safeGetString(paymentData, "nimbbl_signature", "")
            result["reason"] = safeGetString(paymentData, "reason", "")
            result["sub_merchant_id"] = safeGetString(paymentData, "sub_merchant_id", "")
            result["is_callback"] = safeGetBoolean(paymentData, "is_callback", false)

            // Extract order details
            val order = parseNestedObject(paymentData, "order")
            if (order != null) {
                result["invoice_id"] = safeGetString(order, "invoice_id", "")
                result["total_amount"] = safeGetDouble(order, "total_amount", 0.0)
                result["currency"] = safeGetString(order, "currency", "INR")
                result["order_date"] = safeGetString(order, "order_date", "")
                result["cancellation_reason"] = safeGetString(order, "cancellation_reason", "")
                result["grand_total"] = safeGetDouble(order, "grand_total", 0.0)
                result["amount_before_tax"] = safeGetDouble(order, "amount_before_tax", 0.0)
                result["tax"] = safeGetDouble(order, "tax", 0.0)
                result["convenience_fee"] = safeGetDouble(order, "convenience_fee", 0.0)
                result["offer_discount"] = safeGetDouble(order, "offer_discount", 0.0)
                result["attempts"] = safeGetInt(order, "attempts", 0)
                result["referrer_platform"] = safeGetString(order, "referrer_platform", "")
                result["referrer_platform_version"] = safeGetString(order, "referrer_platform_version", "")
                result["shopfront_domain"] = safeGetString(order, "shopfront_domain", "")
                result["device_user_agent"] = safeGetString(order, "device_user_agent", "")

                // Extract shipping address
                val shippingAddress = parseNestedObject(order, "shipping_address")
                if (shippingAddress != null) {
                    result["shipping_address_1"] = safeGetString(shippingAddress, "address_1", "")
                    result["shipping_street"] = safeGetString(shippingAddress, "street", "")
                    result["shipping_area"] = safeGetString(shippingAddress, "area", "")
                    result["shipping_city"] = safeGetString(shippingAddress, "city", "")
                    result["shipping_state"] = safeGetString(shippingAddress, "state", "")
                    result["shipping_pincode"] = safeGetString(shippingAddress, "pincode", "")
                    result["shipping_country"] = safeGetString(shippingAddress, "country", "")
                    result["shipping_landmark"] = safeGetString(shippingAddress, "landmark", "")
                    result["shipping_address_type"] = safeGetString(shippingAddress, "address_type", "")
                }

                // Extract device info
                val device = parseNestedObject(order, "device")
                if (device != null) {
                    result["device_browser_name"] = safeGetString(device, "browser_name", "")
                    result["device_name"] = safeGetString(device, "device_name", "")
                    result["device_os_name"] = safeGetString(device, "os_name", "")
                    result["device_ip_address"] = safeGetString(device, "ip_address", "")
                }
            }

        } catch (e: Exception) {
            Log.e("OrderSuccess", "Error parsing payment response: ${e.message}")
        }

        return result
    }

    /**
     * Parse nested object that could be JSONObject or JSON string
     */
    private fun parseNestedObject(parent: JSONObject, key: String): JSONObject? {
        // Try to get as JSONObject first
        var nested = safeGetJSONObject(parent, key)

        // If null, try to get as string and parse it as JSON
        if (nested == null) {
            val nestedString = safeGetString(parent, key, "")
            if (nestedString.isNotEmpty()) {
                try {
                    nested = JSONObject(nestedString)
                } catch (e: Exception) {
                    Log.e("OrderSuccess", "Error parsing nested object '$key': ${e.message}")
                }
            }
        }

        return nested
    }

    /**
     * Safe JSON parsing helper functions
     */
    private fun safeGetString(jsonObject: JSONObject, key: String, defaultValue: String = ""): String {
        return try {
            jsonObject.optString(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting string for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetDouble(jsonObject: JSONObject, key: String, defaultValue: Double): Double {
        return try {
            jsonObject.optDouble(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting double for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetInt(jsonObject: JSONObject, key: String, defaultValue: Int): Int {
        return try {
            jsonObject.optInt(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting int for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetBoolean(jsonObject: JSONObject, key: String, defaultValue: Boolean): Boolean {
        return try {
            jsonObject.optBoolean(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting boolean for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetJSONObject(jsonObject: JSONObject, key: String): JSONObject? {
        return try {
            // Check if key exists and is not null
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                jsonObject.optJSONObject(key)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting JSONObject for key '$key': ${e.message}")
            null
        }
    }


    /**
     * Display order information with enhanced details from parsed JSON
     */
    private fun displayOrderInformation() {
        // Check if this is an encrypted response
        val isEncrypted = parsedPaymentData["is_encrypted"] as? Boolean ?: false
        if (isEncrypted) {
            displayEncryptedResponse()
            return
        }

        // Use parsed data if available, otherwise fallback to intent data
        val displayOrderId = parsedPaymentData["order_id"]?.toString() ?: "Unknown"
        val displayStatus = parsedPaymentData["status"]?.toString() ?: "Unknown"
        val message = parsedPaymentData["message"]?.toString() ?: ""
        val reason = parsedPaymentData["reason"]?.toString() ?: ""
        val totalAmount = parsedPaymentData["total_amount"]?.toString() ?: ""
        val currency = parsedPaymentData["currency"]?.toString() ?: "INR"
        val invoiceId = parsedPaymentData["invoice_id"]?.toString() ?: ""
        val transactionId = parsedPaymentData["transaction_id"]?.toString() ?: parsedPaymentData["nimbbl_transaction_id"]?.toString() ?: ""
        val orderDate = parsedPaymentData["order_date"]?.toString() ?: ""
        val cancellationReason = parsedPaymentData["cancellation_reason"]?.toString() ?: ""
        val attempts = parsedPaymentData["attempts"]?.toString() ?: ""
        val referrerPlatform = parsedPaymentData["referrer_platform"]?.toString() ?: ""
        val referrerPlatformVersion = parsedPaymentData["referrer_platform_version"]?.toString() ?: ""
        val deviceIpAddress = parsedPaymentData["device_ip_address"]?.toString() ?: ""
        val deviceName = parsedPaymentData["device_name"]?.toString() ?: ""
        val deviceOsName = parsedPaymentData["device_os_name"]?.toString() ?: ""
        val shippingCity = parsedPaymentData["shipping_city"]?.toString() ?: ""
        val shippingState = parsedPaymentData["shipping_state"]?.toString() ?: ""
        val shippingCountry = parsedPaymentData["shipping_country"]?.toString() ?: ""
        val shippingPincode = parsedPaymentData["shipping_pincode"]?.toString() ?: ""

        // Update status icon and colors based on payment status
        updateStatusUI(displayStatus)

        // Update status title and message
        updateStatusTitleAndMessage(displayStatus, message)

        // Update order details
        txtOrderId.text = displayOrderId
        txtStatus.text = displayStatus
        updateStatusTextColor(displayStatus)

        // Show/hide amount field based on data availability
        val amountLayout = findViewById<LinearLayout>(R.id.amount_layout)
        if (totalAmount.isNotEmpty()) {
            txtAmount.text = getString(R.string.amount_format, currency, totalAmount)
            amountLayout.visibility = View.VISIBLE
        } else {
            amountLayout.visibility = View.GONE
        }

        // Show/hide invoice ID field based on data availability
        val invoiceLayout = findViewById<LinearLayout>(R.id.invoice_layout)
        if (invoiceId.isNotEmpty()) {
            txtInvoiceId.text = invoiceId
            invoiceLayout.visibility = View.VISIBLE
        } else {
            invoiceLayout.visibility = View.GONE
        }

        // Show/hide transaction ID field based on data availability
        val transactionIdLayout = findViewById<LinearLayout>(R.id.transaction_id_layout)
        if (transactionId.isNotEmpty()) {
            txtTransactionId.text = transactionId
            transactionIdLayout.visibility = View.VISIBLE
        } else {
            transactionIdLayout.visibility = View.GONE
        }

        // Show/hide order date field based on data availability
        val orderDateLayout = findViewById<LinearLayout>(R.id.order_date_layout)
        val formattedDate = formatOrderDate(orderDate)
        if (formattedDate != "N/A" && orderDate.isNotEmpty()) {
            txtOrderDate.text = formattedDate
            orderDateLayout.visibility = View.VISIBLE
        } else {
            orderDateLayout.visibility = View.GONE
        }

        // Update detailed status information
        updateDetailedStatus(
            DetailedStatusInfo(
                reason = reason,
                cancellationReason = cancellationReason,
                attempts = attempts,
                referrerPlatform = referrerPlatform,
                referrerPlatformVersion = referrerPlatformVersion,
                deviceName = deviceName,
                deviceOsName = deviceOsName,
                deviceIpAddress = deviceIpAddress,
                shippingCity = shippingCity,
                shippingState = shippingState,
                shippingCountry = shippingCountry,
                shippingPincode = shippingPincode
            )
        )

        // Update action buttons based on status
        updateActionButtons(displayStatus)

        // Log additional parsed information for debugging
        Log.d("OrderSuccess", "Displaying order info:")
        Log.d("OrderSuccess", "  Order ID: $displayOrderId")
        Log.d("OrderSuccess", "  Transaction ID: $transactionId")
        Log.d("OrderSuccess", "  Status: $displayStatus")
        Log.d("OrderSuccess", "  Message: $message")
        Log.d("OrderSuccess", "  Reason: $reason")
        Log.d("OrderSuccess", "  Amount: $currency $totalAmount")
    }

    private fun updateStatusUI(status: String) {
        val statusLower = status.lowercase()
        when (statusLower) {
            "success", "completed" -> {
                statusIcon.setImageResource(R.drawable.ic_payment_success)
                statusIcon.setColorFilter(getColor(R.color.success_green))
            }
            "failed", "error", "cancelled" -> {
                statusIcon.setImageResource(R.drawable.ic_payment_failed)
                statusIcon.setColorFilter(getColor(R.color.red))
            }
            "encrypted" -> {
                statusIcon.setImageResource(R.drawable.ic_payment_failed) // Use warning icon
                statusIcon.setColorFilter(getColor(R.color.warning_orange))
            }
            else -> {
                statusIcon.setImageResource(R.drawable.ic_payment_success)
                statusIcon.setColorFilter(getColor(R.color.light_grey))
            }
        }
    }

    private fun updateStatusTitleAndMessage(status: String, message: String) {
        val statusLower = status.lowercase()
        when (statusLower) {
            "success", "completed" -> {
                statusTitle.text = getString(R.string.payment_successful)
                statusTitle.setTextColor(getColor(R.color.success_green))
                statusMessage.text = message.ifEmpty { getString(R.string.payment_success_message) }
            }
            "failed", "error" -> {
                statusTitle.text = getString(R.string.payment_failed)
                statusTitle.setTextColor(getColor(R.color.red))
                statusMessage.text = message.ifEmpty { getString(R.string.payment_failed_message) }
            }
            "cancelled" -> {
                statusTitle.text = getString(R.string.payment_cancelled)
                statusTitle.setTextColor(getColor(R.color.light_grey))
                statusMessage.text = message.ifEmpty { getString(R.string.payment_cancelled_message) }
            }
            "encrypted" -> {
                statusTitle.text = getString(R.string.encrypted_response)
                statusTitle.setTextColor(getColor(R.color.warning_orange))
                statusMessage.text = message.ifEmpty { ENCRYPTED_RESPONSE_MESSAGE }
            }
            else -> {
                statusTitle.text = getString(R.string.payment_status)
                statusTitle.setTextColor(getColor(R.color.black))
                statusMessage.text = message.ifEmpty { getString(R.string.payment_status_message, status) }
            }
        }
    }

    private fun updateStatusTextColor(status: String) {
        val statusLower = status.lowercase()
        when (statusLower) {
            "success", "completed" -> {
                txtStatus.setTextColor(getColor(R.color.success_green))
            }
            "failed", "error" -> {
                txtStatus.setTextColor(getColor(R.color.red))
            }
            "cancelled" -> {
                txtStatus.setTextColor(getColor(R.color.light_grey))
            }
            "encrypted" -> {
                txtStatus.setTextColor(getColor(R.color.warning_orange))
            }
            else -> {
                txtStatus.setTextColor(getColor(R.color.black))
            }
        }
    }

    private fun updateDetailedStatus(info: DetailedStatusInfo) {
        val detailedInfo = buildString {
            if (info.reason.isNotEmpty()) {
                append("Reason: ${info.reason}\n\n")
            }
            if (info.cancellationReason.isNotEmpty()) {
                append("Cancellation Reason: ${info.cancellationReason}\n\n")
            }
            if (info.attempts.isNotEmpty()) {
                append("Attempts: ${info.attempts}\n\n")
            }
            if (info.referrerPlatform.isNotEmpty()) {
                append("Platform: ${info.referrerPlatform} ${info.referrerPlatformVersion}\n\n")
            }
            if (info.deviceName.isNotEmpty()) {
                append("Device: ${info.deviceName} (${info.deviceOsName})\n\n")
            }
            if (info.deviceIpAddress.isNotEmpty()) {
                append("IP Address: ${info.deviceIpAddress}\n\n")
            }
            if (info.shippingCity.isNotEmpty()) {
                append("Shipping Address: ${info.shippingCity}, ${info.shippingState}, ${info.shippingCountry} - ${info.shippingPincode}")
            }
        }

        // Show/hide the entire Additional Details card based on content
        val additionalDetailsCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.additional_details_card)
        if (detailedInfo.isNotEmpty()) {
            txtDetailedStatus.text = detailedInfo
            additionalDetailsCard.visibility = View.VISIBLE
        } else {
            additionalDetailsCard.visibility = View.GONE
        }
    }

    /**
     * Display encrypted response message
     */
    private fun displayEncryptedResponse() {
        // Update status icon and colors for encrypted response
        updateStatusUI("encrypted")

        // Update status title and message
        updateStatusTitleAndMessage("encrypted", ENCRYPTED_RESPONSE_MESSAGE)

        // Hide order details since we can't parse them
        val orderDetailsCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.order_details_card)
        orderDetailsCard.visibility = View.GONE

        // Hide additional details
        val additionalDetailsCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.additional_details_card)
        additionalDetailsCard.visibility = View.GONE

        // Update action buttons for encrypted response
        updateActionButtons("encrypted")

        // Log the encrypted response for debugging
        val encryptedResponse = parsedPaymentData["encrypted_response"]?.toString() ?: ""
        Log.d("OrderSuccess", "Encrypted response received: ${encryptedResponse.take(100)}...")
    }

    private fun updateActionButtons(status: String) {
        val btnPrimaryAction = findViewById<MaterialButton>(R.id.btn_primary_action)
        val statusLower = status.lowercase()

        when (statusLower) {
            "success", "completed" -> {
                btnPrimaryAction.text = getString(R.string.btn_continue)
                btnPrimaryAction.setBackgroundColor(getColor(R.color.success_green))
            }
            "failed", "error" -> {
                btnPrimaryAction.text = getString(R.string.btn_try_again)
                btnPrimaryAction.setBackgroundColor(getColor(R.color.red))
            }
            "cancelled" -> {
                btnPrimaryAction.text = getString(R.string.btn_retry_payment)
                btnPrimaryAction.setBackgroundColor(getColor(R.color.light_grey))
            }
            "encrypted" -> {
                btnPrimaryAction.text = getString(R.string.btn_continue)
                btnPrimaryAction.setBackgroundColor(getColor(R.color.warning_orange))
            }
            else -> {
                btnPrimaryAction.text = getString(R.string.btn_continue)
                btnPrimaryAction.setBackgroundColor(getColor(R.color.colorAccent))
            }
        }
    }

    private fun formatOrderDate(orderDate: String): String {
        return if (orderDate.isNotEmpty()) {
            try {
                // Simple date formatting - you can enhance this with proper date parsing
                orderDate.take(19) // Show only date and time part
            } catch (_: Exception) {
                orderDate
            }
        } else {
            "N/A"
        }
    }

    private fun setupSafeArea() {
        val rootView = findViewById<View>(android.R.id.content)
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