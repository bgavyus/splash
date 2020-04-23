package io.github.bgavyus.splash.common

import android.content.res.Configuration
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import io.github.bgavyus.splash.App
import java.util.*

fun getDefaultString(resourceId: Int): String {
    val config = Configuration().apply { setLocale(Locale.ROOT) }
    return App.shared.createConfigurationContext(config).getString(resourceId)
}

fun showMessage(resourceId: Int) {
    Thread {
        Looper.prepare()
        Toast.makeText(App.shared, resourceId, Toast.LENGTH_LONG).run {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
        Looper.loop()
    }.start()
}
