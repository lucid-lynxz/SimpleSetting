package org.lynxz.simplesetting

import android.app.Application
import org.lynxz.utils.ScreenUtil

class SettingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScreenUtil.enableFontScaleChange(false)
        ScreenUtil.init(this)
    }
}