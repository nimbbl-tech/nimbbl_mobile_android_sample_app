package tech.nimbbl.exmaple

import android.app.Activity
import android.app.Application
import android.os.Bundle
import tech.nimbbl.exmaple.utils.SystemBars

class NimbblSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    SystemBars.apply(activity)
                }

                override fun onActivityResumed(activity: Activity) {
                    SystemBars.apply(activity)
                }

                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            }
        )
    }
}

