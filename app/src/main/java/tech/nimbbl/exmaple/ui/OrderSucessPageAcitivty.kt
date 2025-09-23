package tech.nimbbl.exmaple.ui

import android.annotation.SuppressLint
import android.content.Intent
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
import org.json.JSONException
import org.json.JSONObject
import tech.nimbbl.exmaple.R

class OrderSucessPageAcitivty : AppCompatActivity() {
    var rawJsonData: String? = null

    // Parsed data from complex JSON
    private var parsedPaymentData: MutableMap<String, Any> = mutableMapOf()

    // UI Elements
    private lateinit var statusIcon: ImageView
    private lateinit var statusTitle: TextView
    private lateinit var statusMessage: TextView
    private lateinit var txtOrderId: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtAmount: TextView
    private lateinit var txtInvoiceId: TextView
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
        // Handle primary action based on payment status
        val status = parsedPaymentData["status"]?.toString()?.lowercase()
        when (status) {
            "failed" -> {
                // Go back to payment screen to try again
                finish()
            }
            "success" -> {
                // Continue or go back to home
                finish()
            }
            "cancelled" -> {
                // Retry payment
                finish()
            }
            else -> {
                // Default action
                finish()
            }
        }
    }

    private fun handleSecondaryAction() {
        // Go back to home/main screen
        finish()
    }


    /**
     * Parse payment response - now handles complete JSON response from SDK
     */
    private fun parseComplexPaymentResponse(jsonString: String): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()

        // Validate input
        if (jsonString.isBlank()) {
            Log.w("OrderSuccess", "parseComplexPaymentResponse: Empty or blank JSON string provided")
            return result
        }

        try {
            val jsonObject = JSONObject(jsonString)

            // Debug: Log the actual JSON structure
            Log.d("OrderSuccess", "JSON keys: ${jsonObject.keys().asSequence().toList()}")
            Log.d("OrderSuccess", "JSON structure: ${jsonObject.toString(2)}")

            // SDK now returns only payload content, so use it directly
            val paymentData = jsonObject
            
            // Check if this is an encrypted response
            if (paymentData.has("encrypted_response")) {
                Log.d("OrderSuccess", "Detected encrypted response")
                result["is_encrypted"] = true
                result["encrypted_response"] = safeGetString(paymentData, "encrypted_response", "")
                result["status"] = "encrypted"
                result["message"] = "Encrypted response received. Please handle decryption on your server."
                return result
            }

            // Extract key payment information with safe parsing from the correct data source
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

            // Debug: Print all extracted payment information
            Log.d("OrderSuccess", "=== PAYMENT INFORMATION ===")
            Log.d("OrderSuccess", "status: ${result["status"]}")
            Log.d("OrderSuccess", "message: ${result["message"]}")
            Log.d("OrderSuccess", "order_id: ${result["order_id"]}")
            Log.d("OrderSuccess", "nimbbl_order_id: ${result["nimbbl_order_id"]}")
            Log.d("OrderSuccess", "transaction_id: ${result["transaction_id"]}")
            Log.d("OrderSuccess", "nimbbl_transaction_id: ${result["nimbbl_transaction_id"]}")
            Log.d("OrderSuccess", "signature: ${result["signature"]}")
            Log.d("OrderSuccess", "nimbbl_signature: ${result["nimbbl_signature"]}")
            Log.d("OrderSuccess", "reason: ${result["reason"]}")
            Log.d("OrderSuccess", "sub_merchant_id: ${result["sub_merchant_id"]}")
            Log.d("OrderSuccess", "is_callback: ${result["is_callback"]}")

            // Extract order details if available
            Log.d("OrderSuccess", "=== CHECKING ORDER FIELD ===")
            Log.d("OrderSuccess", "Has order field: ${paymentData.has("order")}")
            
            // Try to get order as JSONObject first, then as string if that fails
            var order: JSONObject? = safeGetJSONObject(paymentData, "order")
            Log.d("OrderSuccess", "Order JSONObject is null: ${order == null}")
            
            // If order is null, try to get it as a string and parse it
            if (order == null) {
                val orderString = safeGetString(paymentData, "order", "")
                Log.d("OrderSuccess", "Order as string: ${orderString.take(100)}...")
                if (orderString.isNotEmpty()) {
                    try {
                        // Check if it's a Map-like string format
                        if (orderString.startsWith("{") && orderString.contains("=")) {
                            Log.d("OrderSuccess", "Detected Map-like string format, converting to JSON")
                            order = parseMapLikeStringToJSON(orderString)
                        } else {
                            // Try to parse as regular JSON
                            order = JSONObject(orderString)
                        }
                        Log.d("OrderSuccess", "Successfully parsed order from string")
                    } catch (e: Exception) {
                        Log.e("OrderSuccess", "Error parsing order string: ${e.message}")
                        Log.e("OrderSuccess", "Order string: ${orderString.take(200)}...")
                    }
                }
            }
            
            if (order != null) {
                Log.d("OrderSuccess", "=== ORDER DETAILS ===")
                Log.d("OrderSuccess", "Order object keys: ${order.keys().asSequence().toList()}")
                
                // Extract order details with safe parsing
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

                // Debug: Print all order details
                Log.d("OrderSuccess", "invoice_id: ${result["invoice_id"]}")
                Log.d("OrderSuccess", "total_amount: ${result["total_amount"]}")
                Log.d("OrderSuccess", "currency: ${result["currency"]}")
                Log.d("OrderSuccess", "order_date: ${result["order_date"]}")
                Log.d("OrderSuccess", "cancellation_reason: ${result["cancellation_reason"]}")
                Log.d("OrderSuccess", "grand_total: ${result["grand_total"]}")
                Log.d("OrderSuccess", "amount_before_tax: ${result["amount_before_tax"]}")
                Log.d("OrderSuccess", "tax: ${result["tax"]}")
                Log.d("OrderSuccess", "convenience_fee: ${result["convenience_fee"]}")
                Log.d("OrderSuccess", "offer_discount: ${result["offer_discount"]}")
                Log.d("OrderSuccess", "attempts: ${result["attempts"]}")
                Log.d("OrderSuccess", "referrer_platform: ${result["referrer_platform"]}")
                Log.d("OrderSuccess", "referrer_platform_version: ${result["referrer_platform_version"]}")
                Log.d("OrderSuccess", "shopfront_domain: ${result["shopfront_domain"]}")
                Log.d("OrderSuccess", "device_user_agent: ${result["device_user_agent"]}")

                // Extract shipping address if available
                Log.d("OrderSuccess", "=== CHECKING SHIPPING ADDRESS FIELD ===")
                var shippingAddress: JSONObject? = safeGetJSONObject(order, "shipping_address")
                Log.d("OrderSuccess", "Shipping address JSONObject is null: ${shippingAddress == null}")
                
                // If shipping address is null, try to get it as a string and parse it
                if (shippingAddress == null) {
                    val shippingAddressString = safeGetString(order, "shipping_address", "")
                    if (shippingAddressString.isNotEmpty()) {
                        try {
                            if (shippingAddressString.startsWith("{") && shippingAddressString.contains("=")) {
                                Log.d("OrderSuccess", "Detected Map-like string format for shipping address")
                                shippingAddress = parseMapLikeStringToJSON(shippingAddressString)
                            } else {
                                shippingAddress = JSONObject(shippingAddressString)
                            }
                        } catch (e: Exception) {
                            Log.e("OrderSuccess", "Error parsing shipping address string: ${e.message}")
                        }
                    }
                }
                
                if (shippingAddress != null) {
                    Log.d("OrderSuccess", "=== SHIPPING ADDRESS ===")
                    Log.d("OrderSuccess", "Shipping address keys: ${shippingAddress.keys().asSequence().toList()}")

                    // Extract shipping address details with safe parsing
                    result["shipping_address_1"] = safeGetString(shippingAddress, "address_1", "")
                    result["shipping_street"] = safeGetString(shippingAddress, "street", "")
                    result["shipping_area"] = safeGetString(shippingAddress, "area", "")
                    result["shipping_city"] = safeGetString(shippingAddress, "city", "")
                    result["shipping_state"] = safeGetString(shippingAddress, "state", "")
                    result["shipping_pincode"] = safeGetString(shippingAddress, "pincode", "")
                    result["shipping_country"] = safeGetString(shippingAddress, "country", "")
                    result["shipping_landmark"] = safeGetString(shippingAddress, "landmark", "")
                    result["shipping_address_type"] = safeGetString(shippingAddress, "address_type", "")

                    // Debug: Print all shipping address details
                    Log.d("OrderSuccess", "shipping_address_1: ${result["shipping_address_1"]}")
                    Log.d("OrderSuccess", "shipping_street: ${result["shipping_street"]}")
                    Log.d("OrderSuccess", "shipping_area: ${result["shipping_area"]}")
                    Log.d("OrderSuccess", "shipping_city: ${result["shipping_city"]}")
                    Log.d("OrderSuccess", "shipping_state: ${result["shipping_state"]}")
                    Log.d("OrderSuccess", "shipping_pincode: ${result["shipping_pincode"]}")
                    Log.d("OrderSuccess", "shipping_country: ${result["shipping_country"]}")
                    Log.d("OrderSuccess", "shipping_landmark: ${result["shipping_landmark"]}")
                    Log.d("OrderSuccess", "shipping_address_type: ${result["shipping_address_type"]}")
                } else {
                    Log.d("OrderSuccess", "No shipping address found")
                }

                // Extract device info if available
                Log.d("OrderSuccess", "=== CHECKING DEVICE FIELD ===")
                var device: JSONObject? = safeGetJSONObject(order, "device")
                Log.d("OrderSuccess", "Device JSONObject is null: ${device == null}")
                
                // If device is null, try to get it as a string and parse it
                if (device == null) {
                    val deviceString = safeGetString(order, "device", "")
                    if (deviceString.isNotEmpty()) {
                        try {
                            if (deviceString.startsWith("{") && deviceString.contains("=")) {
                                Log.d("OrderSuccess", "Detected Map-like string format for device")
                                device = parseMapLikeStringToJSON(deviceString)
                            } else {
                                device = JSONObject(deviceString)
                            }
                        } catch (e: Exception) {
                            Log.e("OrderSuccess", "Error parsing device string: ${e.message}")
                        }
                    }
                }
                
                if (device != null) {
                    Log.d("OrderSuccess", "=== DEVICE INFORMATION ===")
                    Log.d("OrderSuccess", "Device keys: ${device.keys().asSequence().toList()}")

                    // Extract device information with safe parsing
                    result["device_browser_name"] = safeGetString(device, "browser_name", "")
                    result["device_name"] = safeGetString(device, "device_name", "")
                    result["device_os_name"] = safeGetString(device, "os_name", "")
                    result["device_ip_address"] = safeGetString(device, "ip_address", "")

                    // Debug: Print all device information
                    Log.d("OrderSuccess", "device_browser_name: ${result["device_browser_name"]}")
                    Log.d("OrderSuccess", "device_name: ${result["device_name"]}")
                    Log.d("OrderSuccess", "device_os_name: ${result["device_os_name"]}")
                    Log.d("OrderSuccess", "device_ip_address: ${result["device_ip_address"]}")
                } else {
                    Log.d("OrderSuccess", "No device information found")
                }
            } else {
                Log.d("OrderSuccess", "No order object found")
            }

            Log.d(
                "OrderSuccess",
                "Parsed payment response: status=${result["status"]}, order_id=${result["order_id"]}"
            )

            // Debug: Print final summary of all extracted values
            Log.d("OrderSuccess", "=== FINAL EXTRACTED VALUES SUMMARY ===")
            for ((key, value) in result) {
                Log.d("OrderSuccess", "$key: $value")
            }

        } catch (e: JSONException) {
            Log.e("OrderSuccess", "parseComplexPaymentResponse: JSON parsing error: ${e.message}")
            Log.e("OrderSuccess", "parseComplexPaymentResponse: JSON string: ${jsonString.take(200)}...")
        } catch (e: Exception) {
            Log.e("OrderSuccess", "parseComplexPaymentResponse: Unexpected error: ${e.message}")
            Log.e("OrderSuccess", "parseComplexPaymentResponse: JSON string: ${jsonString.take(200)}...")
        }

        return result
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

    private fun safeGetDouble(jsonObject: JSONObject, key: String, defaultValue: Double = 0.0): Double {
        return try {
            jsonObject.optDouble(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting double for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetInt(jsonObject: JSONObject, key: String, defaultValue: Int = 0): Int {
        return try {
            jsonObject.optInt(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting int for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetBoolean(jsonObject: JSONObject, key: String, defaultValue: Boolean = false): Boolean {
        return try {
            jsonObject.optBoolean(key, defaultValue)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting boolean for key '$key': ${e.message}")
            defaultValue
        }
    }

    private fun safeGetJSONObject(jsonObject: JSONObject, key: String): JSONObject? {
        return try {
            jsonObject.optJSONObject(key)
        } catch (e: Exception) {
            Log.w("OrderSuccess", "Error getting JSONObject for key '$key': ${e.message}")
            null
        }
    }

    /**
     * Parse Map-like string format to JSONObject
     * Example: {key1=value1, key2=value2, nested={nested_key=nested_value}}
     */
    private fun parseMapLikeStringToJSON(mapString: String): JSONObject? {
        return try {
            Log.d("OrderSuccess", "Parsing Map-like string: ${mapString.take(100)}...")
            
            // Remove outer braces
            val content = mapString.trim().removePrefix("{").removeSuffix("}")
            val jsonObject = JSONObject()
            
            // Split by comma, but be careful with nested objects
            val keyValuePairs = mutableListOf<String>()
            var currentPair = ""
            var braceCount = 0
            
            for (char in content) {
                when (char) {
                    ',' -> {
                        if (braceCount == 0) {
                            keyValuePairs.add(currentPair.trim())
                            currentPair = ""
                        } else {
                            currentPair += char
                        }
                    }
                    '{' -> {
                        braceCount++
                        currentPair += char
                    }
                    '}' -> {
                        braceCount--
                        currentPair += char
                    }
                    else -> currentPair += char
                }
            }
            
            // Add the last pair
            if (currentPair.isNotEmpty()) {
                keyValuePairs.add(currentPair.trim())
            }
            
            // Parse each key-value pair
            for (pair in keyValuePairs) {
                val equalIndex = pair.indexOf('=')
                if (equalIndex > 0) {
                    val key = pair.substring(0, equalIndex).trim()
                    val value = pair.substring(equalIndex + 1).trim()
                    
                    // Handle different value types
                    when {
                        value == "null" -> jsonObject.put(key, JSONObject.NULL)
                        value.startsWith("{") && value.endsWith("}") -> {
                            // Nested object
                            val nestedJson = parseMapLikeStringToJSON(value)
                            if (nestedJson != null) {
                                jsonObject.put(key, nestedJson)
                            } else {
                                jsonObject.put(key, value)
                            }
                        }
                        value.startsWith("\"") && value.endsWith("\"") -> {
                            // String value (remove quotes)
                            jsonObject.put(key, value.substring(1, value.length - 1))
                        }
                        value.matches(Regex("^-?\\d+\\.\\d+$")) -> {
                            // Double value
                            jsonObject.put(key, value.toDouble())
                        }
                        value.matches(Regex("^-?\\d+$")) -> {
                            // Integer value
                            jsonObject.put(key, value.toInt())
                        }
                        value == "true" -> jsonObject.put(key, true)
                        value == "false" -> jsonObject.put(key, false)
                        else -> {
                            // Default to string
                            jsonObject.put(key, value)
                        }
                    }
                }
            }
            
            Log.d("OrderSuccess", "Successfully parsed Map-like string to JSON")
            jsonObject
        } catch (e: Exception) {
            Log.e("OrderSuccess", "Error parsing Map-like string: ${e.message}")
            Log.e("OrderSuccess", "Map string: ${mapString.take(200)}...")
            null
        }
    }

    /**
     * Simple JSON parsing fallback
     */
    private fun parseSimpleJson(jsonString: String): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()
        try {
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject[key]
                result[key] = value ?: ""
            }
        } catch (e: JSONException) {
            Log.e("OrderSuccess", "parseSimpleJson: JSON parsing error: ${e.message}")
        }
        return result
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
        val grandTotal = parsedPaymentData["grand_total"]?.toString() ?: ""
        val currency = parsedPaymentData["currency"]?.toString() ?: "INR"
        val invoiceId = parsedPaymentData["invoice_id"]?.toString() ?: ""
        val subMerchantId = parsedPaymentData["sub_merchant_id"]?.toString() ?: ""
        val orderDate = parsedPaymentData["order_date"]?.toString() ?: ""
        val cancellationReason = parsedPaymentData["cancellation_reason"]?.toString() ?: ""
        val attempts = parsedPaymentData["attempts"]?.toString() ?: ""
        val referrerPlatform = parsedPaymentData["referrer_platform"]?.toString() ?: ""
        val referrerPlatformVersion = parsedPaymentData["referrer_platform_version"]?.toString() ?: ""
        val deviceUserAgent = parsedPaymentData["device_user_agent"]?.toString() ?: ""
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
            txtAmount.text = "$currency $totalAmount"
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
        updateDetailedStatus(reason, cancellationReason, attempts, referrerPlatform, 
            referrerPlatformVersion, deviceName, deviceOsName, deviceIpAddress, 
            shippingCity, shippingState, shippingCountry, shippingPincode)

        // Update action buttons based on status
        updateActionButtons(displayStatus)

        // Log additional parsed information for debugging
        Log.d("OrderSuccess", "Displaying order info:")
        Log.d("OrderSuccess", "  Order ID: $displayOrderId")
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
                statusTitle.text = "Payment Successful"
                statusTitle.setTextColor(getColor(R.color.success_green))
                statusMessage.text = message.ifEmpty { "Your payment has been processed successfully" }
            }
            "failed", "error" -> {
                statusTitle.text = "Payment Failed"
                statusTitle.setTextColor(getColor(R.color.red))
                statusMessage.text = message.ifEmpty { "Your payment could not be processed" }
            }
            "cancelled" -> {
                statusTitle.text = "Payment Cancelled"
                statusTitle.setTextColor(getColor(R.color.light_grey))
                statusMessage.text = message.ifEmpty { "Payment was cancelled" }
            }
            "encrypted" -> {
                statusTitle.text = "Encrypted Response"
                statusTitle.setTextColor(getColor(R.color.warning_orange))
                statusMessage.text = message.ifEmpty { "Encrypted response received. Please handle decryption on your server." }
            }
            else -> {
                statusTitle.text = "Payment Status"
                statusTitle.setTextColor(getColor(R.color.black))
                statusMessage.text = message.ifEmpty { "Payment status: $status" }
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

    private fun updateDetailedStatus(reason: String, cancellationReason: String, attempts: String,
                                   referrerPlatform: String, referrerPlatformVersion: String,
                                   deviceName: String, deviceOsName: String, deviceIpAddress: String,
                                   shippingCity: String, shippingState: String, shippingCountry: String,
                                   shippingPincode: String) {
        val detailedInfo = buildString {
            if (reason.isNotEmpty()) {
                append("Reason: $reason\n\n")
            }
            if (cancellationReason.isNotEmpty()) {
                append("Cancellation Reason: $cancellationReason\n\n")
            }
            if (attempts.isNotEmpty()) {
                append("Attempts: $attempts\n\n")
            }
            if (referrerPlatform.isNotEmpty()) {
                append("Platform: $referrerPlatform $referrerPlatformVersion\n\n")
            }
            if (deviceName.isNotEmpty()) {
                append("Device: $deviceName ($deviceOsName)\n\n")
            }
            if (deviceIpAddress.isNotEmpty()) {
                append("IP Address: $deviceIpAddress\n\n")
            }
            if (shippingCity.isNotEmpty()) {
                append("Shipping Address: $shippingCity, $shippingState, $shippingCountry - $shippingPincode")
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
        updateStatusTitleAndMessage("encrypted", "Encrypted response received. Please handle decryption on your server.")
        
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
                btnPrimaryAction.text = "Continue"
                btnPrimaryAction.setBackgroundColor(getColor(R.color.success_green))
            }
            "failed", "error" -> {
                btnPrimaryAction.text = "Try Again"
                btnPrimaryAction.setBackgroundColor(getColor(R.color.red))
            }
            "cancelled" -> {
                btnPrimaryAction.text = "Retry Payment"
                btnPrimaryAction.setBackgroundColor(getColor(R.color.light_grey))
            }
            "encrypted" -> {
                btnPrimaryAction.text = "Continue"
                btnPrimaryAction.setBackgroundColor(getColor(R.color.warning_orange))
            }
            else -> {
                btnPrimaryAction.text = "Continue"
                btnPrimaryAction.setBackgroundColor(getColor(R.color.colorAccent))
            }
        }
    }

    private fun formatOrderDate(orderDate: String): String {
        return if (orderDate.isNotEmpty()) {
            try {
                // Simple date formatting - you can enhance this with proper date parsing
                orderDate.substring(0, 19) // Show only date and time part
        } catch (e: Exception) {
                orderDate
            }
        } else {
            "N/A"
        }
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