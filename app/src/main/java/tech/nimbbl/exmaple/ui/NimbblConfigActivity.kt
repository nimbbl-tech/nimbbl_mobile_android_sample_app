package tech.nimbbl.exmaple.ui

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.databinding.ActivityNimbblConfigBinding
import tech.nimbbl.exmaple.utils.AppConstants.ENVIRONMENT_PRE_PROD
import tech.nimbbl.exmaple.utils.AppConstants.ENVIRONMENT_PROD
import tech.nimbbl.exmaple.utils.AppConstants.ENVIRONMENT_QA
import tech.nimbbl.exmaple.utils.AppConstants.EXPERIENCE_NATIVE
import tech.nimbbl.exmaple.utils.AppConstants.EXPERIENCE_WEBVIEW
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.APP_PREFERENCE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.QA_ENVIRONMENT_URL
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SAMPLE_APP_MODE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.ACCESS_TOKEN
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.DEBUG_LOGS_ENABLED
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.DEBUG_MENU_UNLOCKED
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.ORDER_TOKEN
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.exmaple.utils.AppUtilExtensions
import tech.nimbbl.exmaple.utils.ApiConstants
import tech.nimbbl.exmaple.utils.UiUtils.showToast
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK

class NimbblConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNimbblConfigBinding
    private val environments = arrayOf(ENVIRONMENT_PROD, ENVIRONMENT_PRE_PROD, ENVIRONMENT_QA)
    private val experiences = arrayOf(EXPERIENCE_NATIVE, EXPERIENCE_WEBVIEW)
    private var selectedEnvironment: String = ENVIRONMENT_PROD
    private var selectedExperience: String = EXPERIENCE_WEBVIEW
    private var qaUrl: String = ApiConstants.BASE_URL_QA3
    private var accessToken: String = ""
    private var debugMenuUnlocked: Boolean = false
    private var sdkDebugLoggingEnabled: Boolean = false
    private var debugTapCount: Int = 0
    private var lastDebugTapMs: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNimbblConfigBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
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
        
        loadSavedPreferences()
        setupUI()
        setupClickListeners()
    }

    private fun loadSavedPreferences() {
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        
        // Load environment
        val savedBaseUrl = preferences.getString(SHOP_BASE_URL, ApiConstants.NIMBBL_TECH_URL)
        selectedEnvironment = when (savedBaseUrl) {
            ApiConstants.NIMBBL_TECH_URL -> ENVIRONMENT_PROD
            ApiConstants.BASE_URL_PRE_PROD -> ENVIRONMENT_PRE_PROD
            else -> ENVIRONMENT_QA
        }
        
        // Load QA URL if it's a QA environment
        if (selectedEnvironment == ENVIRONMENT_QA) {
            qaUrl = preferences.getString(QA_ENVIRONMENT_URL, ApiConstants.BASE_URL_QA3) ?: ApiConstants.BASE_URL_QA3
        }
        
        // Load experience
        val savedExperience = preferences.getString(SAMPLE_APP_MODE, EXPERIENCE_WEBVIEW)
        selectedExperience = if (savedExperience == EXPERIENCE_NATIVE) EXPERIENCE_NATIVE else EXPERIENCE_WEBVIEW

        // Load optional access token (fallback to older "order_token" key)
        accessToken = preferences.getString(ACCESS_TOKEN, "").orEmpty()
            .ifEmpty { preferences.getString(ORDER_TOKEN, "").orEmpty() }

        debugMenuUnlocked = preferences.getBoolean(DEBUG_MENU_UNLOCKED, false)
        sdkDebugLoggingEnabled = preferences.getBoolean(DEBUG_LOGS_ENABLED, false)
    }

    private fun setupUI() {
        // Set initial values
        binding.tvEnvironmentSelection.text = selectedEnvironment
        binding.tvExperienceSelection.text = selectedExperience
        
        // Setup QA URL field
        binding.etQaUrl.setText(qaUrl)
        binding.etQaUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed - only afterTextChanged is used
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed - only afterTextChanged is used
            }
            override fun afterTextChanged(s: Editable?) {
                val newUrl = s?.toString() ?: ""
                if (newUrl.isNotEmpty()) {
                    qaUrl = newUrl
                } else {
                    qaUrl = ApiConstants.BASE_URL_QA3
                    binding.etQaUrl.setText(qaUrl)
                }
            }
        })
        
        // Show/hide QA URL field based on environment
        updateQaUrlVisibility()

        binding.llDebugSection.visibility = if (debugMenuUnlocked) View.VISIBLE else View.GONE

        // Setup optional token field
        binding.etAccessToken.setText(accessToken)
        binding.tvClearAccessToken.setOnClickListener {
            binding.etAccessToken.setText("")
        }
        binding.tvClearAccessToken.visibility =
            if (accessToken.isBlank()) View.GONE else View.VISIBLE
        binding.etAccessToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tvClearAccessToken.visibility =
                    if (s.isNullOrBlank()) View.GONE else View.VISIBLE
            }
        })

        binding.swSdkDebugLogs.isChecked = sdkDebugLoggingEnabled
        // Apply immediately so it works even without tapping "Done"
        NimbblCheckoutSDK.getInstance().setDebugLoggingEnabled(sdkDebugLoggingEnabled)
        binding.swSdkDebugLogs.setOnCheckedChangeListener { _, isChecked ->
            sdkDebugLoggingEnabled = isChecked
            NimbblCheckoutSDK.getInstance().setDebugLoggingEnabled(isChecked)
        }

        binding.llDebugLogs.setOnClickListener {
            startActivity(
                android.content.Intent(this, DebugLogsActivity::class.java)
                    .putExtra(DebugLogsActivity.EXTRA_START_PAUSED, true)
            )
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Unlock debug section: tap app bar 7 times
        val unlockClickTarget = binding.headerView
        unlockClickTarget.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastDebugTapMs > 2000) {
                debugTapCount = 0
            }
            lastDebugTapMs = now
            debugTapCount += 1
            if (debugTapCount >= 7 && !debugMenuUnlocked) {
                debugMenuUnlocked = true
                binding.llDebugSection.visibility = View.VISIBLE
                getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
                    .edit()
                    .putBoolean(DEBUG_MENU_UNLOCKED, true)
                    .apply()
                showToast(this, "Debug options unlocked")
            }
        }
        
        // Environment button
        binding.btnEnvironment.setOnClickListener {
            showEnvironmentDialog()
        }
        
        // Experience button
        binding.btnExperience.setOnClickListener {
            showExperienceDialog()
        }
        
        // Done button
        binding.btnDone.setOnClickListener {
            savePreferences()
        }
    }

    private fun showEnvironmentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_environment))
        
        val items = environments
        builder.setItems(items) { _, which ->
            selectedEnvironment = items[which]
            binding.tvEnvironmentSelection.text = selectedEnvironment
            
            // Update QA URL visibility
            updateQaUrlVisibility()
            
            // Save QA URL if switching to QA
            if (selectedEnvironment == ENVIRONMENT_QA) {
                if (qaUrl.isEmpty()) {
                    qaUrl = ApiConstants.BASE_URL_QA3
                }
                binding.etQaUrl.setText(qaUrl)
            }
        }
        
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun showExperienceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_experience))
        
        val items = experiences
        builder.setItems(items) { _, which ->
            selectedExperience = items[which]
            binding.tvExperienceSelection.text = selectedExperience
        }
        
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun updateQaUrlVisibility() {
        if (selectedEnvironment == ENVIRONMENT_QA) {
            binding.etQaUrl.visibility = View.VISIBLE
        } else {
            binding.etQaUrl.visibility = View.GONE
        }
    }

    private fun savePreferences() {
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        
        // Determine the base URL based on environment
        val baseUrl = when (selectedEnvironment) {
            ENVIRONMENT_PROD -> ApiConstants.NIMBBL_TECH_URL
            ENVIRONMENT_PRE_PROD -> ApiConstants.BASE_URL_PRE_PROD
            ENVIRONMENT_QA -> AppUtilExtensions.formatUrl(qaUrl)
            else -> ApiConstants.NIMBBL_TECH_URL
        }
        
        // Save preferences
        editor.putString(SHOP_BASE_URL, baseUrl)
        editor.putString(QA_ENVIRONMENT_URL, qaUrl)
        if (debugMenuUnlocked) {
            editor.putString(ACCESS_TOKEN, binding.etAccessToken.text?.toString().orEmpty().trim())
            editor.putBoolean(DEBUG_LOGS_ENABLED, binding.swSdkDebugLogs.isChecked)
        }
        editor.putString(SAMPLE_APP_MODE, 
            if (selectedExperience == EXPERIENCE_NATIVE) EXPERIENCE_NATIVE 
            else EXPERIENCE_WEBVIEW
        )
        
        // Update SDK environment
        NimbblCheckoutSDK.getInstance().setEnvironmentUrl(baseUrl)
        NimbblCheckoutSDK.getInstance().setDebugLoggingEnabled(binding.swSdkDebugLogs.isChecked)
        
        val isSuccess = editor.commit()
        
        if (isSuccess) {
            showToast(this, getString(R.string.settings_saved_success))
            finish()
        }
    }
}