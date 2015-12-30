package li.ruoshi.playground

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by ruoshili on 7/2/15.
 */
public class PlaygroundApp() : Application() {
    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

    }


}