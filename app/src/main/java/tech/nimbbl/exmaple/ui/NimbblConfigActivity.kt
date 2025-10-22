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
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.exmaple.utils.AppUtilExtensions
import tech.nimbbl.exmaple.utils.Constants.Companion.BASE_URL_PRE_PROD
import tech.nimbbl.exmaple.utils.Constants.Companion.BASE_URL_PROD
import tech.nimbbl.exmaple.utils.Constants.Companion.DEFAULT_ENVIRONMENT
import tech.nimbbl.exmaple.utils.Constants.Companion.DEFAULT_QA_URL
import tech.nimbbl.exmaple.utils.UiUtils.showToast
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK

class NimbblConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNimbblConfigBinding
    private val environments = arrayOf(ENVIRONMENT_PROD, ENVIRONMENT_PRE_PROD, ENVIRONMENT_QA)
    private val experiences = arrayOf(EXPERIENCE_NATIVE, EXPERIENCE_WEBVIEW)
    private var selectedEnvironment: String = ENVIRONMENT_PROD
    private var selectedExperience: String = EXPERIENCE_WEBVIEW
    private var qaUrl: String = DEFAULT_QA_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNimbblConfigBinding.inflate(layoutInflater)
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
        val savedBaseUrl = preferences.getString(SHOP_BASE_URL, DEFAULT_ENVIRONMENT)
        selectedEnvironment = when (savedBaseUrl) {
            BASE_URL_PROD -> ENVIRONMENT_PROD
            BASE_URL_PRE_PROD -> ENVIRONMENT_PRE_PROD
            else -> ENVIRONMENT_QA
        }
        
        // Load QA URL if it's a QA environment
        if (selectedEnvironment == ENVIRONMENT_QA) {
            qaUrl = preferences.getString(QA_ENVIRONMENT_URL, DEFAULT_QA_URL) ?: DEFAULT_QA_URL
        }
        
        // Load experience
        val savedExperience = preferences.getString(SAMPLE_APP_MODE, EXPERIENCE_WEBVIEW)
        selectedExperience = if (savedExperience == EXPERIENCE_NATIVE) EXPERIENCE_NATIVE else EXPERIENCE_WEBVIEW
    }

    private fun setupUI() {
        // Set initial values
        binding.tvEnvironmentSelection.text = selectedEnvironment
        binding.tvExperienceSelection.text = selectedExperience
        
        // Setup QA URL field
        binding.etQaUrl.setText(qaUrl)
        binding.etQaUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newUrl = s?.toString() ?: ""
                if (newUrl.isNotEmpty()) {
                    qaUrl = newUrl
                } else {
                    qaUrl = DEFAULT_QA_URL
                    binding.etQaUrl.setText(qaUrl)
                }
            }
        })
        
        // Show/hide QA URL field based on environment
        updateQaUrlVisibility()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
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
                    qaUrl = DEFAULT_QA_URL
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
            ENVIRONMENT_PROD -> BASE_URL_PROD
            ENVIRONMENT_PRE_PROD -> BASE_URL_PRE_PROD
            ENVIRONMENT_QA -> AppUtilExtensions.formatUrl(qaUrl)
            else -> BASE_URL_PROD
        }
        
        // Save preferences
        editor.putString(SHOP_BASE_URL, baseUrl)
        editor.putString(QA_ENVIRONMENT_URL, qaUrl)
        editor.putString(SAMPLE_APP_MODE, 
            if (selectedExperience == EXPERIENCE_NATIVE) EXPERIENCE_NATIVE 
            else EXPERIENCE_WEBVIEW
        )
        
        // Update SDK environment
        NimbblCheckoutSDK.getInstance().setEnvironmentUrl(baseUrl)
        
        val isSuccess = editor.commit()
        
        if (isSuccess) {
            showToast(this, getString(R.string.settings_saved_success))
            finish()
        }
    }
}