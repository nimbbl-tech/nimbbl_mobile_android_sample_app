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
import org.json.JSONException
import org.json.JSONObject
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.databinding.ActivityOrderCreateBinding
import tech.nimbbl.exmaple.ui.adapter.PaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.SubPaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.headerCustomisationSpinAdapter
import tech.nimbbl.exmaple.utils.AppConstants.EXPERIENCE_WEBVIEW
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.APP_PREFERENCE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SAMPLE_APP_MODE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.exmaple.utils.AppUtilExtensions
import tech.nimbbl.exmaple.utils.Constants.Companion.DEFAULT_ENVIRONMENT
import tech.nimbbl.exmaple.utils.UiUtils.showToast
import tech.nimbbl.exmaple.utils.getBankCode
import tech.nimbbl.exmaple.utils.getPaymentFlow
import tech.nimbbl.exmaple.utils.getPaymentModeCode
import tech.nimbbl.exmaple.utils.getProductID
import tech.nimbbl.exmaple.utils.getWalletCode
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK
import tech.nimbbl.webviewsdk.core.NimbblShopOrderCreation
import tech.nimbbl.webviewsdk.models.NimbblCheckoutOptions
import tech.nimbbl.webviewsdk.models.interfaces.NimbblCheckoutPaymentListener
import java.io.IOException


class OrderCreateActivity : AppCompatActivity(),NimbblCheckoutPaymentListener {
    private lateinit var binding: ActivityOrderCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityOrderCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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

        // Setup safe area handling
        setupSafeArea()
        initialisation()
        setListners()
    }

    private fun setupSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply only system bar insets (status bar, navigation bar)
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            // Optional: log padding values for debugging
            Log.d("InsetsDebug", "Applied top padding: ${systemBars.top}")

            insets
        }
    }


    private fun initialisation() {
        val preferences: SharedPreferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        // Do not open config on first launch; proceed with default UI setup

            // we pass our item list and context to our Adapter.
            // we pass our item list and context to our Adapter.
            val adapter = headerCustomisationSpinAdapter(
                this, resources.getStringArray(R.array.option_enabled)
            )
            binding.spnTestMerchant.setAdapter(adapter)

            binding.switchcompat.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val a = headerCustomisationSpinAdapter(
                        this, resources.getStringArray(R.array.option_enabled)
                    )
                    binding.spnTestMerchant.setAdapter(a)
                } else {
                    val a = headerCustomisationSpinAdapter(
                        this@OrderCreateActivity, resources.getStringArray(R.array.option_disabled)
                    )
                    binding.spnTestMerchant.setAdapter(a)
                }
            }
            val paymentAdapter = PaymentCustomisationSpinAdapter(
                this, resources.getStringArray(R.array.payment_type)
            )
            binding.spnPaymentMode.adapter = paymentAdapter



            binding.spnPaymentMode.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        try {
                            when (position) {
                                0 -> {
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

                                3 -> {
                                    binding.tvSubpaymentTitle.visibility = View.GONE
                                    binding.spnSubPaymentMode.visibility = View.GONE
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

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // sometimes you need nothing here
                    }
                }
            binding.spnAppCurrencyFormat.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
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

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // sometimes you need nothing here
                    }
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

    private fun setListners() {
        binding.btnBuyNow.setOnClickListener {
            // Show loading state
            showLoading(true)

            val skuAmount = Integer.parseInt(binding.txtAmount.text.toString())
            val userFirstName = binding.edtUserFirstName.text.toString().ifEmpty { "" }
            val userEmailId = binding.edtUserEmailId.text.toString().ifEmpty { "" }
            val userMobileNumber = binding.edtUserMobileNumber.text.toString().ifEmpty { "" }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val preferences: SharedPreferences =
                        getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
                    val shopBaseUrl = preferences.getString(SHOP_BASE_URL, DEFAULT_ENVIRONMENT).toString()
                    val testMerchant = binding.spnTestMerchant.selectedItem.toString()

                    // Format the URL to ensure proper formatting
                    val formattedShopBaseUrl = AppUtilExtensions.formatUrl(shopBaseUrl)

                    NimbblCheckoutSDK.getInstance().setEnvironmentUrl(formattedShopBaseUrl)

                    // Use the new order creation functionality from WebView SDK
                    val response = NimbblShopOrderCreation.createShopOrder(
                        formattedShopBaseUrl,
                        skuAmount,
                        userEmailId,
                        userFirstName,
                        userMobileNumber,
                        getProductID(testMerchant, this@OrderCreateActivity),
                        getPaymentModeCode(binding.spnPaymentMode.selectedItem.toString(), this@OrderCreateActivity),
                        getBankCode(binding.spnPaymentMode.selectedItem.toString(), this@OrderCreateActivity)
                    )

                    if (response?.isSuccessful == true) {
                        val orderResponse = response.body()
                        if (orderResponse != null) {
                            Log.i("response", orderResponse.orderId)
                            // Add null check for token
                            if (!orderResponse.token.isNullOrEmpty()) {
                                makePayment(orderResponse.token)
                            } else {
                                Log.e("OrderCreate", "Order token is null or empty")
                                showToast(
                                    this@OrderCreateActivity,
                                    resources.getString(R.string.unable_to_create_order)
                                )
                                showLoading(false)
                            }
                        } else {
                            showToast(
                                this@OrderCreateActivity,
                                resources.getString(R.string.order_created_null_response)
                            )
                            // Hide loading state on error
                            showLoading(false)
                        }
                    } else {
                        try {
                            val errorMessage = response?.errorBody()?.string()
                            Log.e("OrderCreate", "Error response: $errorMessage")

                            if (!errorMessage.isNullOrEmpty()) {
                                try {
                                    val jsonObj = JSONObject(errorMessage)
                                    if (jsonObj.has("error")) {
                                        val errorObj = jsonObj.getJSONObject("error")
                                        if (errorObj.has("nimbbl_consumer_message")) {
                                            showToast(
                                                this@OrderCreateActivity,
                                                errorObj.getString("nimbbl_consumer_message")
                                            )
                                        } else {
                                            showToast(
                                                this@OrderCreateActivity,
                                                resources.getString(R.string.unable_to_create_order)
                                            )
                                        }
                                    } else {
                                        showToast(
                                            this@OrderCreateActivity,
                                            getString(R.string.unable_to_create_order)
                                        )
                                    }
                                } catch (jsonException: JSONException) {
                                    Log.e("OrderCreate", "JSON parsing error: ${jsonException.message}", jsonException)
                                    // If it's not valid JSON, show the raw error message
                                    if (errorMessage.contains("Traceback")) {
                                        showToast(
                                            this@OrderCreateActivity,
                                            resources.getString(R.string.unable_to_create_order)
                                        )
                                    } else {
                                        showToast(
                                            this@OrderCreateActivity,
                                            resources.getString(R.string.unable_to_create_order)
                                        )
                                    }
                                }
                            } else {
                                showToast(
                                    this@OrderCreateActivity,
                                    resources.getString(R.string.unable_to_create_order)
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("OrderCreate", "Error handling exception: ${e.message}", e)
                            showToast(
                                this@OrderCreateActivity,
                                getString(R.string.unable_to_create_order_error, e.toString())
                            )
                        }
                        // Hide loading state on error
                        showLoading(false)
                    }

                } catch (e: IOException) {
                    Log.e("OrderCreate", "Network error: ${e.message}", e)
                    showToast(
                        this@OrderCreateActivity,
                        resources.getString(R.string.network_error)
                    )
                    showLoading(false)
                } catch (e: JSONException) {
                    Log.e("OrderCreate", "JSON parsing error: ${e.message}", e)
                    showToast(
                        this@OrderCreateActivity,
                        resources.getString(R.string.invalid_response_format)
                    )
                    showLoading(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast(
                        this@OrderCreateActivity,
                        resources.getString(R.string.unable_to_create_order_error)
                    )
                    // Hide loading state on exception
                    showLoading(false)
                }
            }

        }
    }

    /**
     * Show or hide loading state on the button
     * @param isLoading true to show loading, false to hide
     */
    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            try {
                if (isLoading) {
                    // Show loading state
                    binding.btnBuyNow.apply {
                        isEnabled = false
                        text = "" // Hide button text when loader is showing
                    }

                    // Show circular progress indicator
                    binding?.progressLoader?.visibility = View.VISIBLE

                } else {
                    // Hide loading state
                    binding?.btnBuyNow?.apply {
                        isEnabled = true
                        text = getString(R.string.pay_now) // Restore original button text
                    }

                    // Hide circular progress indicator
                    binding?.progressLoader?.visibility = View.GONE
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


    private fun makePayment(
        orderToken: String,
    ) {
        // Hide loading state as SDK will handle its own UI
        showLoading(false)

        val builder = NimbblCheckoutOptions.Builder()

        // Add null check for SharedPreferences
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val appMode = preferences?.getString(SAMPLE_APP_MODE, EXPERIENCE_WEBVIEW) ?: EXPERIENCE_WEBVIEW
        if (appMode.equals(getString(R.string.value_webview))) {
            val options =
                builder.setOrderToken(orderToken)
                    .setPaymentModeCode(getPaymentModeCode(binding?.spnPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setBankCode(getBankCode(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setPaymentFlow(getPaymentFlow(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setWalletCode(getWalletCode(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .build()
            NimbblCheckoutSDK.getInstance().init(this)
            NimbblCheckoutSDK.getInstance().checkout(options)
        } else {
            // Handle other payment modes
            Log.w("OrderCreate", "Unsupported payment mode: $appMode")
            showToast(
                this@OrderCreateActivity,
                resources.getString(R.string.unsupported_payment_mode)
            )
        }
    }


    override fun onCheckoutResponse(data: MutableMap<String, Any>) {
        // Ensure loading state is hidden on payment success
        showLoading(false)


        val intent = Intent(this, OrderSucessPageAcitivty::class.java)

        // Pass the raw JSON data if available for complex parsing
        // The data map contains the parsed JSON, so we need to reconstruct it or pass the original
        // For now, we'll pass the data map as a JSON string
        try {
            val jsonString = convertMapToJsonString(data)
            intent.putExtra("raw_json_data", jsonString)
        } catch (e: Exception) {
            Log.e("OrderCreate", "Error converting data to JSON: ${e.message}")
        }

        startActivity(intent)
    }

    /**
     * Convert Map to JSON string for passing to success page
     */
    private fun convertMapToJsonString(data: MutableMap<String, Any>): String {
        return try {
            val jsonObject = JSONObject()
            for ((key, value) in data) {
                when (value) {
                    is String -> jsonObject.put(key, value)
                    is Number -> jsonObject.put(key, value)
                    is Boolean -> jsonObject.put(key, value)
                    is Map<*, *> -> {
                        val nestedJson = JSONObject()
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