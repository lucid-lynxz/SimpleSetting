package org.lynxz.simplesetting.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import org.lynxz.simplesetting.R
import org.lynxz.simplesetting.databinding.ActivityMainBinding
import org.lynxz.simplesetting.showToast
import org.lynxz.simplesetting.ui.base.BaseBindingActivity
import org.lynxz.simplesetting.util.CommonUtil
import org.lynxz.simplesetting.util.OnSoundIndexChanged
import org.lynxz.simplesetting.util.SoundInfoBean
import org.lynxz.simplesetting.util.SoundUtil


class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener {
    private val TAG = "MainActivity"

    override fun getLayoutRes() = R.layout.activity_main

    @RequiresApi(Build.VERSION_CODES.M)
    override fun afterViewCreated() {
        dataBinding.clickAction = this
        SoundUtil.init(this, object : OnSoundIndexChanged {
            override fun invoke(soundInfo: SoundInfoBean, lastIndex: Int) {
                updateVolume()
            }
        })

        updateVolume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_volume_decrease -> adjustVolume(false) // 减小音量
            R.id.btn_volume_increase -> adjustVolume(true) // 增加音量
            R.id.btn_volume_fix -> adjustVolume(false, 0.8) // 设置音量为80%
            R.id.btn_sound_setting -> startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) // 音量设置页面
            R.id.btn_mi_call -> launch("小米通话", "com.xiaomi.mitime") // 启动小米通话app
            R.id.btn_forward_sms -> launch("短信转发", "org.lynxz.forwardsms") // 启动短信转发app
        }
    }


    private fun launch(appName: String, pkgName: String) {
        val exist = CommonUtil.launch(this, pkgName)
        if (!exist) {
            showToast("未安装该app:$appName")
        }
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

        // 退出勿扰/静音模式,并根据铃声音量递增/递减 10%
        SoundUtil.exitSilentMode()
            .getSoundInfo(AudioManager.STREAM_RING)?.apply {
                val flag = if (increase) 1 else -1
                val deltaIndex = 1.coerceAtLeast((maxVolume * 0.1).toInt())
                val tIndex = maxVolume.coerceAtMost(index + flag * deltaIndex)
                val tRatio = if (ratio in 0.0..1.0) {
                    ratio
                } else {
                    tIndex * 1.0 / maxVolume
                }
                SoundUtil.setAllVolume(tRatio)
                updateVolume()
            }
    }

    private fun updateVolume() {
        dataBinding.tvVolume.text = "音量:\n${SoundUtil.getSoundInfo()?.getPercent()} "
    }
}