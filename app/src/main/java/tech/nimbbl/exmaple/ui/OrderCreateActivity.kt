package tech.nimbbl.exmaple.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.databinding.ActivityOrderCreateBinding
import tech.nimbbl.exmaple.ui.adapter.PaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.SubPaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.HeaderCustomisationSpinAdapter
import tech.nimbbl.exmaple.utils.ApiConstants
import tech.nimbbl.exmaple.utils.AppConstants.EXPERIENCE_WEBVIEW
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.APP_PREFERENCE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.ACCESS_TOKEN
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.ORDER_TOKEN
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SAMPLE_APP_MODE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.exmaple.utils.AppUtilExtensions
import tech.nimbbl.exmaple.utils.UiUtils.showToast
import tech.nimbbl.exmaple.utils.getBankCode
import tech.nimbbl.exmaple.utils.getPaymentFlow
import tech.nimbbl.exmaple.utils.getPaymentModeCode
import tech.nimbbl.exmaple.utils.getProductID
import tech.nimbbl.exmaple.utils.getWalletCode
import tech.nimbbl.webviewsdk.BuildConfig
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK
import tech.nimbbl.webviewsdk.models.NimbblCheckoutOptions
import tech.nimbbl.webviewsdk.models.interfaces.NimbblCheckoutPaymentListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class OrderCreateActivity : AppCompatActivity(), NimbblCheckoutPaymentListener {
    private lateinit var binding: ActivityOrderCreateBinding
    private val enableSampleAppApiLogs: Boolean = BuildConfig.DEBUG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupSafeArea()
        initialisation()
        setListeners()
    }

    private fun setupSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            Log.d("InsetsDebug", "Applied top padding: ${systemBars.top}")
            insets
        }
    }

    private fun initialisation() {
        val adapter = HeaderCustomisationSpinAdapter(
            this, resources.getStringArray(R.array.option_enabled)
        )
        binding.spnTestMerchant.setAdapter(adapter)

        binding.switchcompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val a = HeaderCustomisationSpinAdapter(
                    this, resources.getStringArray(R.array.option_enabled)
                )
                binding.spnTestMerchant.setAdapter(a)
            } else {
                val a = HeaderCustomisationSpinAdapter(
                    this@OrderCreateActivity, resources.getStringArray(R.array.option_disabled)
                )
                binding.spnTestMerchant.setAdapter(a)
            }
        }

        val paymentAdapter = PaymentCustomisationSpinAdapter(
            this, resources.getStringArray(R.array.payment_type)
        )
        binding.spnPaymentMode.adapter = paymentAdapter

        binding.spnPaymentMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                try {
                    when (position) {
                        0, 3 -> {
                            binding.tvSubpaymentTitle.visibility = View.GONE
                            binding.spnSubPaymentMode.visibility = View.GONE
                        }
                        1 -> {
                            binding.tvSubpaymentTitle.visibility = View.VISIBLE
                            binding.spnSubPaymentMode.visibility = View.VISIBLE
                            val subPaymentAdapter = SubPaymentCustomisationSpinAdapter(
                                this@OrderCreateActivity,
                                resources.getStringArray(R.array.sub_payment_type_netbanking)
                            )
                            binding.spnSubPaymentMode.adapter = subPaymentAdapter
                        }
                        2 -> {
                            binding.tvSubpaymentTitle.visibility = View.VISIBLE
                            binding.spnSubPaymentMode.visibility = View.VISIBLE
                            val subPaymentAdapter = SubPaymentCustomisationSpinAdapter(
                                this@OrderCreateActivity,
                                resources.getStringArray(R.array.sub_payment_type_wallet)
                            )
                            binding.spnSubPaymentMode.adapter = subPaymentAdapter
                        }
                        4 -> {
                            binding.tvSubpaymentTitle.visibility = View.VISIBLE
                            binding.spnSubPaymentMode.visibility = View.VISIBLE
                            val subPaymentAdapter = SubPaymentCustomisationSpinAdapter(
                                this@OrderCreateActivity,
                                resources.getStringArray(R.array.sub_payment_type_upi)
                            )
                            binding.spnSubPaymentMode.adapter = subPaymentAdapter
                        }
                    }
                } catch (e: Exception) {
                    Log.e("OrderCreate", "Error in payment mode selection: ${e.message}", e)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spnAppCurrencyFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                try {
                    when (position) {
                        0 -> {
                            Log.d("SAN", "00000")
                            paymentAdapter.setData(resources.getStringArray(R.array.payment_type))
                            binding.spnPaymentMode.isEnabled = true
                        }
                        else -> {
                            Log.d("SAN", "$position$position$position")
                            paymentAdapter.setData(resources.getStringArray(R.array.payment_type_card))
                            binding.spnPaymentMode.isEnabled = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("OrderCreate", "Error in currency format selection: ${e.message}", e)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.checkboxcompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.llUserDetailContainer.visibility = View.VISIBLE
            } else {
                binding.llUserDetailContainer.visibility = View.GONE
            }
        }

        binding.tvSettings.setOnClickListener {
            val intent = Intent(this, NimbblConfigActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private fun setListeners() {
        binding.btnBuyNow.setOnClickListener {
            showLoading(true)

            val skuAmount = Integer.parseInt(binding.txtAmount.text.toString())
            val userFirstName = binding.edtUserFirstName.text.toString().ifEmpty { "" }
            val userEmailId = binding.edtUserEmailId.text.toString().ifEmpty { "" }
            val userMobileNumber = binding.edtUserMobileNumber.text.toString().ifEmpty { "" }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val testMerchant = binding.spnTestMerchant.selectedItem.toString()
                    val preferences: SharedPreferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
                    val configuredBaseUrlRaw = preferences.getString(SHOP_BASE_URL, "").orEmpty().trim()
                    // "Access token" here is the Core API auth token used for /api/v3/create-order Authorization header
                    val accessToken = preferences.getString(ACCESS_TOKEN, "").orEmpty().trim()
                        .ifEmpty { preferences.getString(ORDER_TOKEN, "").orEmpty().trim() }
                    val sdkDebugLoggingEnabled = preferences.getBoolean(
                        tech.nimbbl.exmaple.utils.AppPreferenceKeys.DEBUG_LOGS_ENABLED,
                        false
                    )

                    val formattedShopBaseUrl = resolveShopBaseUrl()

                    // If base URL is an IP, keep it for WebView launch but default API calls to QA3 inside the SDK.
                    val sdkEnvUrl = if (configuredBaseUrlRaw.isNotEmpty() && isIpBasedUrl(configuredBaseUrlRaw)) {
                        AppUtilExtensions.formatUrl(configuredBaseUrlRaw) ?: configuredBaseUrlRaw
                    } else {
                        formattedShopBaseUrl
                    }
                    NimbblCheckoutSDK.getInstance().setDebugLoggingEnabled(sdkDebugLoggingEnabled)
                    NimbblCheckoutSDK.getInstance().setEnvironmentUrl(sdkEnvUrl)

                    // Do NOT use access token for checkout. It's only for authenticating create-order API calls.

                    val productId = getProductID(testMerchant, this@OrderCreateActivity)
                    val paymentMode = getPaymentModeCode(binding.spnPaymentMode.selectedItem.toString(), this@OrderCreateActivity)
                    val subPaymentMode = getBankCode(binding.spnPaymentMode.selectedItem.toString(), this@OrderCreateActivity)

                    val responseJson = withContext(Dispatchers.IO) {
                        if (accessToken.isBlank()) {
                            val shopOrderUrl = resolveShopOrderUrl(formattedShopBaseUrl)
                            createShopOrderRequest(
                                shopOrderUrl = shopOrderUrl,
                                totalAmount = skuAmount,
                                emailId = userEmailId,
                                firstName = userFirstName,
                                mobileNumber = userMobileNumber,
                                productId = productId,
                                paymentMode = paymentMode,
                                subPaymentMode = subPaymentMode
                            )
                        } else {
                            createOrderV3Request(
                                apiBaseUrl = formattedShopBaseUrl,
                                totalAmount = skuAmount,
                                emailId = userEmailId,
                                firstName = userFirstName,
                                mobileNumber = userMobileNumber,
                                productId = productId,
                                paymentMode = paymentMode,
                                subPaymentMode = subPaymentMode,
                                accessToken = accessToken
                            )
                        }
                    }

                    if (responseJson != null && responseJson.optBoolean("success", false)) {
                        val token = responseJson.optString("token", "")
                        if (token.isNotEmpty()) {
                            makePayment(token)
                        } else {
                            Log.e("OrderCreate", "Order token is empty")
                            showToast(this@OrderCreateActivity, resources.getString(R.string.unable_to_create_order))
                            showLoading(false)
                        }
                    } else {
                        try {
                            val errorMessage = responseJson?.optString("error", null)
                            val statusCode = responseJson?.optInt("status", -1)
                            Log.e("OrderCreate", "Error response (status=$statusCode): $errorMessage")

                            val userMessage = when {
                                statusCode == 404 -> getString(R.string.unable_to_create_order) + " (404 Not Found)"
                                !errorMessage.isNullOrEmpty() && errorMessage.trimStart().startsWith("{") -> {
                                    try {
                                        val jsonObj = JSONObject(errorMessage)
                                        if (jsonObj.has("error")) {
                                            val errorObj = jsonObj.getJSONObject("error")
                                            if (errorObj.has("nimbbl_consumer_message")) {
                                                errorObj.getString("nimbbl_consumer_message")
                                            } else null
                                        } else null
                                    } catch (e: JSONException) {
                                        Log.e("OrderCreate", "JSON parsing error: ${e.message}", e)
                                        null
                                    }
                                }
                                else -> null
                            }
                            showToast(
                                this@OrderCreateActivity,
                                userMessage ?: resources.getString(R.string.unable_to_create_order)
                            )
                        } catch (e: Exception) {
                            Log.e("OrderCreate", "Error handling exception: ${e.message}", e)
                            showToast(this@OrderCreateActivity, getString(R.string.unable_to_create_order_error, e.toString()))
                        }
                        showLoading(false)
                    }

                } catch (e: IOException) {
                    Log.e("OrderCreate", "Network error: ${e.message}", e)
                    showToast(this@OrderCreateActivity, resources.getString(R.string.network_error))
                    showLoading(false)
                } catch (e: JSONException) {
                    Log.e("OrderCreate", "JSON parsing error: ${e.message}", e)
                    showToast(this@OrderCreateActivity, resources.getString(R.string.invalid_response_format))
                    showLoading(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast(this@OrderCreateActivity, resources.getString(R.string.unable_to_create_order_error))
                    showLoading(false)
                }
            }
        }
    }

    private fun resolveShopBaseUrl(): String {
        val preferences: SharedPreferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val configuredBaseUrl = preferences.getString(SHOP_BASE_URL, "").orEmpty().trim()
        val finalBaseUrl = when {
            configuredBaseUrl.isEmpty() -> ApiConstants.NIMBBL_TECH_URL
            isIpBasedUrl(configuredBaseUrl) -> ApiConstants.BASE_URL_QA1
            else -> configuredBaseUrl
        }
        val formatted = AppUtilExtensions.formatUrl(finalBaseUrl)
        return if (formatted.isNullOrEmpty()) ApiConstants.NIMBBL_TECH_URL else formatted
    }

    private fun isIpBasedUrl(value: String): Boolean {
        return try {
            val normalized = if (value.startsWith("http://") || value.startsWith("https://")) {
                value
            } else {
                "https://$value"
            }
            val host = URL(normalized).host.orEmpty()
            // Matches IPv4 host like 127.0.0.1
            Regex("""^(\d{1,3}\.){3}\d{1,3}$""").matches(host)
        } catch (_: Exception) {
            false
        }
    }

 
    private fun resolveShopOrderUrl(apiBaseUrl: String): String {
        return try {
            val normalized = AppUtilExtensions.formatUrl(apiBaseUrl).trimEnd('/')
            val url = URL(normalized)
            val host = url.host ?: return ApiConstants.SHOP_ORDER_URL_QA1
            val protocol = url.protocol
            val shopHost = host.replaceFirst(Regex("api"), "sonicshopapi")
            "$protocol://$shopHost/create-shop"
        } catch (e: Exception) {
            Log.e("OrderCreate", "Failed to resolve shop URL: ${e.message}", e)
            ApiConstants.SHOP_ORDER_URL_QA1
        }
    }

    private fun createShopOrderRequest(
        shopOrderUrl: String,
        totalAmount: Int,
        emailId: String,
        firstName: String,
        mobileNumber: String,
        productId: String,
        paymentMode: String,
        subPaymentMode: String?
    ): JSONObject? {
        val url = shopOrderUrl

        // Build payload aligned with Core API SDK OrderResponse structure
        val payload = JSONObject().apply {
            // Order basic details
            put("currency", "INR")
            put("amount", totalAmount.toString())
            put("product_id", productId)
            put("total_amount", totalAmount)
            put("amount_before_tax", totalAmount)
            put("tax", 0)
            put("additional_charges", 0)
            put("grand_total_amount", totalAmount)

            // Order configuration
            put("order_line_items", true)
            put("checkout_experience", "redirect")
            put("payment_mode", paymentMode.ifEmpty { "All" })
            if (!subPaymentMode.isNullOrEmpty()) {
                put("sub_payment_mode", subPaymentMode)
            }

            // User details (aligned with Core API SDK User model)
            put("user", JSONObject().apply {
                put("email", emailId)
                put("name", firstName)
                put("mobile_number", mobileNumber)
            })

            // Optional: Add order line items array if order_line_items is true
            put("order_line_item", JSONArray().apply {
                put(JSONObject().apply {
                    put("title", "Product")
                    put("description", "Product description")
                    put("quantity", 1)
                    put("rate", totalAmount.toDouble())
                    put("total_amount", totalAmount.toDouble())
                    put("amount_before_tax", totalAmount.toDouble())
                    put("tax", 0)
                    put("image_url", "")
                    put("sku_id", productId)
                    put("uom", "unit")
                })
            })
        }

        val requestBody = payload.toString()

        return try {
            val urlObj = URL(url)
            val connection = (urlObj.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doInput = true
                doOutput = true
            }
            logSampleApiRequest(
                tag = "OrderCreate-Shop",
                method = connection.requestMethod,
                url = url,
                headers = mapOf(
                    "Content-Type" to "application/json; charset=utf-8"
                ),
                body = requestBody
            )
            connection.outputStream.use { os: OutputStream ->
                os.write(requestBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            val code = connection.responseCode
            val responseBody = readResponseBody(connection)
            connection.disconnect()
            logSampleApiResponse(tag = "OrderCreate-Shop", code = code, message = connection.responseMessage, body = responseBody)

            if (code in 200..299) {
                val json = if (!responseBody.isNullOrEmpty()) JSONObject(responseBody) else JSONObject()
                json.put("success", true)
                json
            } else {
                JSONObject().apply {
                    put("success", false)
                    put("status", code)
                    put("error", responseBody ?: "")
                }
            }
        } catch (e: Exception) {
            Log.e("OrderCreate", "Network error: ${e.message}", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "Unknown error")
            }
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String? {
        return try {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.let { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { it.readText() }.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Create an order using Nimbbl's Core API v3.
     * Docs: https://docs.nimbbl.tech/api-reference/create-an-order-v-3/
     */
    private fun createOrderV3Request(
        apiBaseUrl: String,
        totalAmount: Int,
        emailId: String,
        firstName: String,
        mobileNumber: String,
        productId: String,
        paymentMode: String,
        subPaymentMode: String?,
        accessToken: String?
    ): JSONObject? {
        val base = (AppUtilExtensions.formatUrl(apiBaseUrl) ?: apiBaseUrl).trimEnd('/') + "/"
        val url = "${base}api/v3/create-order"

        val invoiceId = "inv_${System.currentTimeMillis()}"
        val payload = JSONObject().apply {
            // Align request body with docs/example payload shape.
            val qty = 1
            val amountBeforeTax = totalAmount
            val taxAmount = 0

            put("currency", "INR")
            put("quantity", qty)
            put("amount_before_tax", amountBeforeTax)
            put("tax", taxAmount)
            put("total_amount", totalAmount)
            put("invoice_id", invoiceId)
            // Include user only when mobile number is present (some configs enforce mobile_number if user is sent).
            if (mobileNumber.isNotBlank()) {
                put("user", JSONObject().apply {
                    if (emailId.isNotEmpty()) put("email", emailId)
                    put("first_name", firstName)
                    put("last_name", "")
                    put("country_code", "+91")
                    put("mobile_number", mobileNumber)
                })
            }

    /*        val address = JSONObject().apply {
                put("first_name", firstName)
                put("last_name", "")
                put("address_1", "1080 Beach Mansion")
                put("street", "Magic Beach Drive")
                put("landmark", "Opposite Magic Mountain")
                put("area", "Elyria")
                put("city", "Atlantis")
                put("state", "Castalia")
                put("pincode", "100389")
                put("address_type", "Beach House")
                put("label", "Sunny Home")
            }*/
          //  put("shipping_address", JSONObject(address.toString()))
          //  put("billing_address", JSONObject(address.toString()))

        //    put("ip_address", "unknown")
        //    put("user_agent", System.getProperty("http.agent") ?: "")
        //    put("merchant_shopfront_domain", "https://merchant-shopfront.example.com")
          //  put("offer_enabled", false)
         //   put("convert_to_payment_link_enabled", false)
         //   put("validate_order_line_item", false)

            // Some merchants require order line items for create-order.
            put("order_line_items", JSONArray().apply {
                put(JSONObject().apply {
                    put("sku_id", productId.ifEmpty { "item_$invoiceId" })
                    put("title", "Product")
                    put("description", "Product description")
                    put("rate", totalAmount.toDouble())
                    put("quantity", 1)
                    put("amount_before_tax", totalAmount.toDouble())
                    put("tax", 0)
                    put("total_amount", totalAmount.toDouble())
                    put("image_url", "")
                    put("uom", "unit")
                /*    put("serial_numbers", JSONArray().apply {
                        put("359043372654548")
                        put("359043371395481")
                    })*/
                })
            })

   /*         put("bank_account", JSONObject().apply {
                put("account_number", "10038849992883")
                put("name", firstName.ifEmpty { "Customer" })
                put("ifsc", "ICIC0000011")
            })

            put("custom_attributes", JSONObject().apply {
                put("name", firstName)
                put("place", "Themyscira")
                put("animal", "Jumpa")
                put("thing", "Tiara")
            })*/
        }

        val requestBody = payload.toString()

        return try {
            val headersForLog = mutableMapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Accept" to "application/json",
            )
            if (!accessToken.isNullOrEmpty()) headersForLog["Authorization"] = "Bearer ${maskToken(accessToken)}"

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 30_000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                if (!accessToken.isNullOrEmpty()) {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                }
                doInput = true
                doOutput = true
            }
            logSampleApiRequest(
                tag = "OrderCreate-v3",
                method = connection.requestMethod,
                url = url,
                headers = headersForLog,
                body = requestBody
            )

            connection.outputStream.use { os: OutputStream ->
                os.write(requestBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = connection.responseCode
            val responseBody = readResponseBody(connection)
            connection.disconnect()
            logSampleApiResponse(tag = "OrderCreate-v3", code = code, message = connection.responseMessage, body = responseBody)

            if (code in 200..299) {
                val json = if (!responseBody.isNullOrEmpty()) JSONObject(responseBody) else JSONObject()
                json.put("success", true)
                json
            } else {
                JSONObject().apply {
                    put("success", false)
                    put("status", code)
                    put("error", responseBody ?: "")
                }
            }
        } catch (e: Exception) {
            Log.e("OrderCreate", "create-order v3 error: ${e.message}", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "Unknown error")
            }
        }
    }

    private fun logSampleApiRequest(
        tag: String,
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ) {
        if (!enableSampleAppApiLogs) return
        try {
            Log.d(tag, "=== API REQUEST ===")
            Log.d(tag, "Method: $method")
            Log.d(tag, "URL: $url")
            if (headers.isNotEmpty()) Log.d(tag, "Headers: $headers")
            if (!body.isNullOrEmpty()) Log.d(tag, "Request Body: $body")
            Log.d(tag, "===================")
        } catch (_: Exception) {
            // no-op
        }
    }

    private fun logSampleApiResponse(tag: String, code: Int, message: String?, body: String?) {
        if (!enableSampleAppApiLogs) return
        try {
            Log.d(tag, "=== API RESPONSE ===")
            Log.d(tag, "Response Code: $code")
            Log.d(tag, "Response Message: ${message.orEmpty()}")
            Log.d(tag, "Response Body: ${body.orEmpty()}")
            Log.d(tag, "====================")
        } catch (_: Exception) {
            // no-op
        }
    }

    private fun maskToken(token: String): String {
        val t = token.trim()
        if (t.length <= 12) return "****"
        return t.take(6) + "****" + t.takeLast(4)
    }

    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            try {
                if (isLoading) {
                    binding.btnBuyNow.apply {
                        isEnabled = false
                        text = ""
                    }
                    binding.progressLoader.visibility = View.VISIBLE
                } else {
                    binding.btnBuyNow.apply {
                        isEnabled = true
                        text = getString(R.string.pay_now)
                    }
                    binding.progressLoader.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("OrderCreate", "Error updating loading state: ${e.message}", e)
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("SAN", "resultCode-->" + result.resultCode + "data-->" + result.data)
            initialisation()
        }

    private fun makePayment(orderToken: String) {
        showLoading(false)

        val builder = NimbblCheckoutOptions.Builder()
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val appMode = preferences?.getString(SAMPLE_APP_MODE, EXPERIENCE_WEBVIEW) ?: EXPERIENCE_WEBVIEW
        if (appMode == getString(R.string.value_webview)) {
            val options = builder.setOrderToken(orderToken)
                .setPaymentModeCode(getPaymentModeCode(binding.spnPaymentMode.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                .setBankCode(getBankCode(binding.spnSubPaymentMode.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                .setPaymentFlow(getPaymentFlow(binding.spnSubPaymentMode.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                .setWalletCode(getWalletCode(binding.spnSubPaymentMode.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                .build()
            NimbblCheckoutSDK.getInstance().init(this)
            NimbblCheckoutSDK.getInstance().checkout(options)
        } else {
            Log.w("OrderCreate", "Unsupported payment mode: $appMode")
            showToast(this@OrderCreateActivity, resources.getString(R.string.unsupported_payment_mode))
        }
    }

    override fun onCheckoutResponse(data: MutableMap<String, Any>) {
        showLoading(false)
        val intent = Intent(this, OrderSucessPageAcitivty::class.java)
        try {
            val jsonString = convertMapToJsonString(data)
            intent.putExtra("raw_json_data", jsonString)
        } catch (e: Exception) {
            Log.e("OrderCreate", "Error converting data to JSON: ${e.message}")
        }
        startActivity(intent)
    }

    private fun convertMapToJsonString(data: Map<String, Any>): String {
        return try {
            val jsonObject = JSONObject()
            for ((key, value) in data) {
                when (value) {
                    is String -> jsonObject.put(key, value)
                    is Number -> jsonObject.put(key, value)
                    is Boolean -> jsonObject.put(key, value)
                    is Map<*, *> -> {
                        val nestedJson = JSONObject()
                        @Suppress("UNCHECKED_CAST")
                        for ((nestedKey, nestedValue) in value as Map<String, Any>) {
                            nestedJson.put(nestedKey, nestedValue)
                        }
                        jsonObject.put(key, nestedJson)
                    }
                    else -> jsonObject.put(key, value.toString())
                }
            }
            jsonObject.toString()
        } catch (e: Exception) {
            Log.e("OrderCreate", "Error converting map to JSON: ${e.message}")
            "{}"
        }
    }
}
