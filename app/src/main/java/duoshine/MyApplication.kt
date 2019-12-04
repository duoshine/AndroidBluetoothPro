package duoshine

import android.app.Application
import com.squareup.leakcanary.LeakCanary



/**
 *Created by chen on 2019
 */
class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        // Normal app init code...
    }
}