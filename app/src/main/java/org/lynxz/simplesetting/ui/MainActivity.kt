package org.lynxz.simplesetting.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import org.lynxz.simplesetting.R
import org.lynxz.simplesetting.showToast
import org.lynxz.simplesetting.ui.base.BaseActivity
import org.lynxz.simplesetting.util.OnSoundIndexChanged
import org.lynxz.simplesetting.util.SoundUtil
import org.lynxz.utils.no
import org.lynxz.utils.trans.IPermissionCallback
import org.lynxz.utils.trans.PermissionResultInfo


class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"

    override fun getLayoutRes() = R.layout.activity_main

    @RequiresApi(Build.VERSION_CODES.M)
    override fun afterViewCreated() {
        SoundUtil.init(this, object : OnSoundIndexChanged {
            override fun invoke(streamType: Int, lastIndex: Int, curIndex: Int) {
                updateVolume()
            }
        })

        updateVolume()
        // 音量加减
        btn_volume_increase.setOnClickListener { adjustVolume(true) }
        btn_volume_decrease.setOnClickListener { adjustVolume(false) }
        // 一步到位设定为80%
        btn_volume_fix.setOnClickListener { adjustVolume(false, 0.8) }
        // 跳转音量设置页面
        btn_sound_setting.setOnClickListener { startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }
    }

    /**
     * 调节音量
     * @param increase true-增加音量 false-减小音量
     * @param ratio [0.0,1.0] 有效, 表示直接设定为该比例值
     * */
    private fun adjustVolume(increase: Boolean, ratio: Double = -1.0) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (SoundUtil.isSilentMode()
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && !notificationManager.isNotificationPolicyAccessGranted
        ) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            showToast("请启用本app的 \"勿扰\" 模式权限", Toast.LENGTH_LONG)
            return
        }

        // 退出勿扰/静音模式
        SoundUtil.exitSilentMode()
        requestPermission(
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            object : IPermissionCallback {
                override fun onAllRequestResult(allGranted: Boolean) {
                    super.onAllRequestResult(allGranted)
                    val max = SoundUtil.getMaxVolume()
                    val cur = SoundUtil.getVolume()

                    val delta = if (increase) 1 else -1
                    var tRatio = (cur + delta) * 1.0 / max
                    if (ratio in 0.0..1.0) {
                        tRatio = ratio
                    }
                    SoundUtil.setAllVolume(tRatio)
                }

                override fun onRequestResult(permission: PermissionResultInfo) {
                    permission.granted.no {
                        showToast("需要修改系统音量权限, 请授予后重试")
                    }
                }
            })
    }

    private fun updateVolume() {
        tv_volume.text = "音量: ${SoundUtil.getVolume()} "
    }
}