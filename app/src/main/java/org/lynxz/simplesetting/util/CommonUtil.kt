package org.lynxz.simplesetting.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build


object CommonUtil {
    /**
     * 启动app
     * @return app是否存在
     * */
    fun launch(context: Context, pkgName: String): Boolean {
        val packageManager = context.packageManager
        val exist = isAppInstalled(context, pkgName)
        if (exist) {
            val intent = packageManager.getLaunchIntentForPackage(pkgName)
            context.startActivity(intent)
        }
        return exist
    }

    /**
     * 检查包是否存在
     *
     * @param pkgName app包名
     */
    fun isAppInstalled(context: Context, pkgName: String): Boolean {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU --> android 13 --> api 33
        if (Build.VERSION.SDK_INT >= 33) { // TIRAMISU --> android 13 --> api 33
            try {
                context.packageManager.getApplicationInfo(pkgName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        } else {
            val packageManager = context.packageManager
            try {
                packageManager.getPackageInfo(pkgName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }
    }
}