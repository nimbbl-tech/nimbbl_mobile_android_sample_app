package tech.nimbbl.exmaple.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
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
import tech.nimbbl.exmaple.utils.AppConstants.EXTRA_ORDER_ID
import tech.nimbbl.exmaple.utils.AppConstants.EXTRA_STATUS
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
import tech.nimbbl.webviewsdk.constants.PaymentConstants
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK
import tech.nimbbl.webviewsdk.core.NimbblOrderCreation
import tech.nimbbl.webviewsdk.models.NimbblCheckoutOptions
import tech.nimbbl.webviewsdk.models.interfaces.NimbblCheckoutPaymentListener
import java.io.IOException


class OrderCreateActivity : AppCompatActivity(), NimbblCheckoutPaymentListener {
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
        val appMode = preferences.getString(SAMPLE_APP_MODE, "").toString()
        if (appMode.isEmpty()) {
            val intent = Intent(this, NimbblConfigActivity::class.java)
            resultLauncher.launch(intent)
        } else {

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
                        parent: AdapterView<*>, view: View, position: Int, id: Long
                    ) {
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
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // sometimes you need nothing here
                    }
                }
            binding.spnAppCurrencyFormat.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View, position: Int, id: Long
                    ) {
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
                    Log.d("OrderCreate", "Using shop base URL: $formattedShopBaseUrl")

                    NimbblCheckoutSDK.instance?.setEnvironmentUrl(formattedShopBaseUrl)

                    // Use the new order creation functionality from WebView SDK
                    val response = NimbblOrderCreation.createOrder(
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
                                showToastSafely(
                                    this@OrderCreateActivity,
                                    R.string.unable_to_create_order
                                )
                                showLoading(false)
                            }
                        } else {
                            showToastSafely(
                                this@OrderCreateActivity,
                                R.string.order_created_null_response
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
                                                                                    showToastSafely(
                                            this@OrderCreateActivity,
                                            errorObj.getString("nimbbl_consumer_message")
                                        )
                                        } else {
                                            showToastSafely(
                                                this@OrderCreateActivity,
                                                R.string.unable_to_create_order
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
                                        showToastSafely(
                                            this@OrderCreateActivity,
                                            R.string.unable_to_create_order
                                        )
                                    } else {
                                        showToastSafely(
                                            this@OrderCreateActivity,
                                            R.string.unable_to_create_order
                                        )
                                    }
                                }
                            } else {
                                showToastSafely(
                                    this@OrderCreateActivity,
                                    R.string.unable_to_create_order
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
                    showToastSafely(
                        this@OrderCreateActivity,
                        R.string.network_error
                    )
                    showLoading(false)
                } catch (e: JSONException) {
                    Log.e("OrderCreate", "JSON parsing error: ${e.message}", e)
                    showToastSafely(
                        this@OrderCreateActivity,
                        R.string.invalid_response_format
                    )
                    showLoading(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToastSafely(
                        this@OrderCreateActivity, 
                        R.string.unable_to_create_order_error, e.toString()
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
                    binding?.btnBuyNow?.apply {
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
        val appMode = preferences?.getString(SAMPLE_APP_MODE, "") ?: ""
        
        if (appMode.equals(getString(R.string.value_webview))) {
            val options =
                builder.setOrderToken(orderToken)
                    .setPaymentModeCode(getPaymentModeCode(binding?.spnPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setBankCode(getBankCode(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setPaymentFlow(getPaymentFlow(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .setWalletCode(getWalletCode(binding?.spnSubPaymentMode?.selectedItem?.toString() ?: "", this@OrderCreateActivity))
                    .build()
            NimbblCheckoutSDK.instance?.init(this)
            NimbblCheckoutSDK.instance?.checkout(options)
        } else {
            // Handle other payment modes
            Log.w("OrderCreate", "Unsupported payment mode: $appMode")
            showToastSafely(
                this@OrderCreateActivity,
                R.string.unsupported_payment_mode
            )
        }
    }
    
    /**
     * Get string resource safely with fallback
     * @param resourceId Resource ID
     * @param formatArgs Format arguments
     * @return String resource or fallback
     */
    private fun getStringSafely(resourceId: Int, vararg formatArgs: Any?): String {
        return try {
            getString(resourceId, *formatArgs)
        } catch (e: Exception) {
            Log.e("OrderCreate", "String resource not found: $resourceId", e)
            when (resourceId) {
                R.string.network_error -> "Network error occurred. Please check your connection and try again."
                R.string.invalid_response_format -> "Invalid response format received from server."
                R.string.unable_to_create_order -> "Unable to create order. Please try again."
                R.string.unable_to_create_order_error -> "Unable to create order: %s"
                R.string.order_created_null_response -> "Order created but received null response."
                R.string.unsupported_payment_mode -> "Unsupported payment mode selected."
                R.string.pay_now -> "Pay Now"
                R.string.value_webview -> "webview"
                R.string.value_cash_free_config -> "cash_free_config"
                R.string.value_razorpay_config -> "razorpay_config"
                R.string.value_paytm_config -> "paytm_config"
                R.string.value_phonepe_config -> "phonepe_config"
                R.string.value_amazonpay_config -> "amazonpay_config"
                R.string.value_mobikwik_config -> "mobikwik_config"
                R.string.value_freecharge_config -> "freecharge_config"
                R.string.value_olamoney_config -> "olamoney_config"
                R.string.value_airtelmoney_config -> "airtelmoney_config"
                R.string.value_jiomoney_config -> "jiomoney_config"
                R.string.value_icicimobile_config -> "icicimobile_config"
                R.string.value_hdfcpay_config -> "hdfcpay_config"
                R.string.value_sbiyono_config -> "sbiyono_config"
                R.string.value_axismobile_config -> "axismobile_config"
                R.string.value_kotakmobile_config -> "kotakmobile_config"
                R.string.value_unionbankmobile_config -> "unionbankmobile_config"
                else -> "Error occurred"
            }
        }
    }
    
    /**
     * Show toast safely with null checks and safe string handling
     * @param context Context
     * @param message Message to show
     */
    private fun showToastSafely(context: Context?, message: String) {
        try {
            context?.let {
                runOnUiThread {
                    try {
                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("OrderCreate", "Error showing toast: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OrderCreate", "Error in showToastSafely: ${e.message}", e)
        }
    }
    
    /**
     * Show toast with safe string handling
     * @param context Context
     * @param resourceId String resource ID
     * @param formatArgs Format arguments
     */
    private fun showToastSafely(context: Context?, resourceId: Int, vararg formatArgs: Any?) {
        val message = getStringSafely(resourceId, *formatArgs)
        showToastSafely(context, message)
    }

    override fun onPaymentFailed(data: String) {
        // Ensure loading state is hidden on payment failure
        showLoading(false)
        
        try {
            // Try to parse as JSON first
            val jsonObject = JSONObject(data)
            val merchantMessage = jsonObject.optString(PaymentConstants.key_nimbbl_merchant_message, data)
            
            // Show the merchant message to the user
            showToast(this, merchantMessage)
            
        } catch (e: Exception) {
            // If parsing fails, treat as legacy error message
            showToast(this, getString(R.string.order_creation_failed, data))
        }
    }


    override fun onPaymentSuccess(data: MutableMap<String, Any>) {
        // Ensure loading state is hidden on payment success
        showLoading(false)
        showToast(
            this, 
            getString(R.string.payment_success_message, 
                data["order_id"].toString(), 
                data["status"].toString()
            )
        )
        val intent = Intent(this, OrderSucessPageAcitivty::class.java)
        intent.putExtra(EXTRA_ORDER_ID, data["order_id"].toString())
        intent.putExtra(EXTRA_STATUS, data["status"].toString())
        startActivity(intent)
    }
}