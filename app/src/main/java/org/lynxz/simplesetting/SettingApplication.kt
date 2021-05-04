package org.lynxz.simplesetting

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import org.lynxz.utils.ScreenUtil

class SettingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScreenUtil.init(this)
    }
}