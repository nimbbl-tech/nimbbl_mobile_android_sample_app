package tech.nimbbl.exmaple.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import tech.nimbbl.exmaple.R
import tech.nimbbl.exmaple.databinding.ActivityNimbblConfigBinding
import tech.nimbbl.exmaple.network.ApiCall.Companion.BASE_URL
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.APP_PREFERENCE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SAMPLE_APP_MODE
import tech.nimbbl.exmaple.utils.AppPreferenceKeys.SHOP_BASE_URL
import tech.nimbbl.webviewsdk.NimbblCheckoutSDK


class NimbblConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNimbblConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNimbblConfigBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE)
        val baseUrl = preferences.getString(SHOP_BASE_URL, BASE_URL)
        when {
            baseUrl.equals("https://qa4api.nimbbl.tech/") -> {
                binding.spnEnvironments.setSelection(2)
            }
            baseUrl.equals("https://apipp.nimbbl.tech/") -> {
                binding.spnEnvironments.setSelection(1)
            }
            baseUrl.equals("https://api.nimbbl.tech/") -> {
                binding.spnEnvironments.setSelection(0)
            }
        }

        val sampleApp = preferences.getString(SAMPLE_APP_MODE, getString(R.string.value_native))
        if (sampleApp.equals(getString(R.string.value_native))) {
            binding.spnAppExperience.setSelection(0)
        } else {
            binding.spnAppExperience.setSelection(1)
        }



        binding.btnDone.setOnClickListener {
            val editor: SharedPreferences.Editor = preferences.edit()
            when (binding.spnEnvironments.selectedItem.toString()) {
                getString(R.string.value_uat) -> {
                    BASE_URL = "https://uatapi.nimbbl.tech/"
                }
                getString(R.string.value_pp)-> {
                    BASE_URL = "https://apipp.nimbbl.tech/"
                }
                getString(R.string.value_prod) -> {
                    BASE_URL = "https://api.nimbbl.tech/"
                }
            }


            editor.putString(SAMPLE_APP_MODE, binding.spnAppExperience.selectedItem.toString())
            editor.putString(SHOP_BASE_URL, BASE_URL)

            NimbblCheckoutSDK.instance?.setEnvironmentUrl(BASE_URL)

            val isSuccess = editor.commit()

            if (isSuccess) {
                Toast.makeText(this, "Environment selected successfully !", Toast.LENGTH_SHORT)
                    .show()
               onBackPressed()
            }

        }
    }
}