package org.lynxz.simplesetting.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


object CommonUtil {
    /**
     * 启动app
     * @return app是否存在
     * */
    fun launch(context: Context, pkgName: String): Boolean {
        val packageManager = context.packageManager
        val exist = checkPackInfo(context, pkgName)
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
    fun checkPackInfo(context: Context, pkgName: String): Boolean {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = context.packageManager.getPackageInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return packageInfo != null
    }
}