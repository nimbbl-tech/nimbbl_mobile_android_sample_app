package tech.nimbbl.exmaple.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.json.JSONObject
import tech.nimbbl.coreapisdk.interfaces.NimbblCheckoutPaymentListener
import tech.nimbbl.coreapisdk.utils.getAPIRequestBody
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.databinding.ActivityOrderCreateBinding
import tech.nimbbl.exmaple.network.ApiCall.Companion.BASE_URL
import tech.nimbbl.exmaple.repository.CatalogRepository
import tech.nimbbl.exmaple.ui.adapter.PaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.SubPaymentCustomisationSpinAdapter
import tech.nimbbl.exmaple.ui.adapter.headerCustomisationSpinAdapter
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.APP_PREFERENCE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SAMPLE_APP_MODE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.exmaple.utils.getAccessKey
import tech.nimbbl.exmaple.utils.getAccessSecret
import tech.nimbbl.exmaple.utils.getBankCode
import tech.nimbbl.exmaple.utils.getPaymentFlow
import tech.nimbbl.exmaple.utils.getPaymentModeCode
import tech.nimbbl.exmaple.utils.getWalletCode
import tech.nimbbl.webviewsdk.NimbblCheckoutOptions
import tech.nimbbl.webviewsdk.NimbblCheckoutSDK


class OrderCreateActivity : AppCompatActivity(), NimbblCheckoutPaymentListener {
    private lateinit var binding: ActivityOrderCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initialisation()
        setListners()
    }


    private fun initialisation() {
        val preferences: SharedPreferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val appMode = preferences.getString(SAMPLE_APP_MODE, "").toString()
        if (appMode.isEmpty()) {
            val intent = Intent(this, NimbblConfigActivity::class.java)
            resultLauncher.launch(intent)
        } else {

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
            val skuTitle = "paper plane"
            val skuAmount = Integer.parseInt(binding.txtAmount.text.toString())
            val skuDesc = "description"
            val userFirstName = binding.edtUserFirstName.text.toString().ifEmpty { "" }
            val userLastName = ""
            val userEmailId = binding.edtUserEmailId.text.toString().ifEmpty { "" }
            val userMobileNumber = binding.edtUserMobileNumber.text.toString().ifEmpty { "" }
            val useraddressLine1 = "My address"
            val userAddrStreet = "My street"
            val userAddrLandmark = "My landmark"
            val userAddrArea = "My area"
            val userAddrCity = "My city"
            val userAddrState = "My state"
            val userAddrPin = "1234567"

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val preferences: SharedPreferences =
                        getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
                    val shopBaseUrl = preferences.getString(SHOP_BASE_URL, BASE_URL).toString()
                    val testMerchant = binding.spnTestMerchant.selectedItem.toString()

                    NimbblCheckoutSDK.instance?.setEnvironmentUrl(shopBaseUrl)
                    val jsonObject = JSONObject()
                    jsonObject.put("access_key", getAccessKey(shopBaseUrl, testMerchant))
                    jsonObject.put("access_secret", getAccessSecret(shopBaseUrl, testMerchant))
                    val body: RequestBody = getAPIRequestBody(jsonObject)
                    val tokenResponse = CatalogRepository().generateToken(
                        shopBaseUrl + "api/v3/generate-token", body
                    )
                    if (tokenResponse.isSuccessful) {
                        val token = tokenResponse.body()?.token.toString()
                        val response = CatalogRepository().createOrder(
                            shopBaseUrl + "api/v3/create-order",
                            token,
                            skuTitle,
                            skuAmount,
                            skuDesc,
                            userFirstName,
                            userLastName,
                            userEmailId,
                            userMobileNumber,
                            useraddressLine1,
                            userAddrStreet,
                            userAddrLandmark,
                            userAddrArea,
                            userAddrCity,
                            userAddrState,
                            userAddrPin
                        )
                        if (response.isSuccessful) {
                            Log.i("response", response.body()!!.order_id)
                            makePayment(
                                response.body()!!.order_id,
                                "",
                                skuAmount,
                                response.body()!!.token,
                                token,
                                response.body()!!.invoice_id

                            )
                        } else {
                            try {
                                val errorMessage = response.errorBody()?.string()
                                val jsonObj = JSONObject(errorMessage)

                                Toast.makeText(
                                    this@OrderCreateActivity,
                                    jsonObj.getJSONObject("error")
                                        .getString("nimbbl_consumer_message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@OrderCreateActivity,
                                    "Unable to create order,\n$e",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@OrderCreateActivity, "Unable to create order,\n$e", Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("SAN", "resultCode-->" + result.resultCode + "data-->" + result.data)
            initialisation()
        }


    private fun makePayment(
        orderId: String,
        subMerchantId: String,
        skuAmount: Int,
        orerToken: String,
        token: String,
        invoiceId: String
    ) {
        val builder = NimbblCheckoutOptions.Builder()
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val appMode = preferences.getString(SAMPLE_APP_MODE, "")
        if (appMode.equals(getString(R.string.value_webview))) {
            val options =
                builder.setToken(token)
                    .setOrderToken(orerToken)
                    .setOrderId(orderId)
                    .setPaymentModeCode(getPaymentModeCode(binding.spnPaymentMode.selectedItem.toString()))
                    .setBankCode(getBankCode(binding.spnSubPaymentMode.selectedItem.toString()))
                    .setPaymentFlow(getPaymentFlow(binding.spnSubPaymentMode.selectedItem.toString()))
                    .setWalletCode(getWalletCode(binding.spnSubPaymentMode.selectedItem.toString()))
                    .setInvoiceId(invoiceId)
                    .build()
            NimbblCheckoutSDK.instance?.init(this)
            NimbblCheckoutSDK.instance?.checkout(options)
        } else {


        }

    }

    override fun onPaymentFailed(data: String) {
        Toast.makeText(this, "Failed: $data", Toast.LENGTH_LONG).show()
    }


    override fun onPaymentSuccess(data: MutableMap<String, Any>) {
        //val payload: MutableMap<String, Any>? =
        //    p0.get("payload") as MutableMap<String, Any>?
        Toast.makeText(
            this, "OrderId=" + data["order_id"] + ", Status=" + data["status"], Toast.LENGTH_LONG
        ).show()
        val intent = Intent(this, OrderSucessPageAcitivty::class.java)
        intent.putExtra("orderid", data["order_id"].toString())
        intent.putExtra("status", data["status"].toString())
        startActivity(intent)

    }
}