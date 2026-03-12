package tech.nimbbl.exmaple

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.exmaple.ui.OrderCreateActivity
import tech.nimbbl.webviewsdk.core.NimbblCheckoutSDK
import tech.nimbbl.webviewsdk.models.NimbblCheckoutOptions

@RunWith(AndroidJUnit4::class)
class NimbblCheckoutSDKTest {

    @Test
    fun checkout_doesNotCrash_withMinimalOptions() {
        val scenario = ActivityScenario.launch(OrderCreateActivity::class.java)
        scenario.onActivity { activity ->
            // Initialize SDK with activity context
            NimbblCheckoutSDK.getInstance().init(activity)

            // Build minimal options resembling real flow
            val options = NimbblCheckoutOptions.Builder()
                .setOrderToken("dummy_test_token")
                .build()

            var invoked = false
            try {
                NimbblCheckoutSDK.getInstance().checkout(options)
                invoked = true
            } catch (t: Throwable) {
                // If this throws synchronously, the SDK invocation path is broken
                throw AssertionError("checkout() threw unexpectedly: ${t.message}", t)
            }
            assertTrue("checkout() should be invoked without immediate crash", invoked)
        }
    }
}


