package org.lynxz.simplesetting.observer

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import org.lynxz.utils.thread.BizHandler

/**
 * 系统设置发生变化时回调
 * */
typealias OnSettingChanged = (selfChange: Boolean, uri: Uri?) -> Unit

/**
 * 自定义 contentObserver
 * */
class SettingsContentObserver(
    private val onSettingChanged: OnSettingChanged?,
    handler: Handler = BizHandler()
) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onSettingChanged?.invoke(selfChange, null)
    }
}