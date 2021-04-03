package org.lynxz.simplesetting.util

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import org.lynxz.simplesetting.observer.OnSettingChanged
import org.lynxz.simplesetting.observer.SettingsContentObserver
import org.lynxz.utils.log.LoggerUtil

typealias OnSoundIndexChanged = (streamType: Int, lastIndex: Int, curIndex: Int) -> Unit

/**
 * 声音管理, 用于调节音量, 获取当前音量等
 * 调节音量需要注册权限:
 *     <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
 *
 * 勿扰模式下调节音量需要启用相关权限设置(参考:https://blog.csdn.net/baidu_27196493/article/details/106082018):
 *     <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />
 *
 * <pre>
 *     // 跳转到勿扰权限页面
 *     val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
 *     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted) {
 *          startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
 *          showToast("请先允许\"勿扰\" 权限")
 *          return
 *     }
 * </pre>
 * */
object SoundUtil {
    private const val TAG = "SoundUtil"
    lateinit var audioManager: AudioManager
    private lateinit var applicationContext: Context
    private var settingContentObserver: ContentObserver? = null

    // 默认类型
    private const val DEFAULT_STREAM = AudioManager.STREAM_RING

    // 各铃声当前音量
    private val volumeIndexMap = mutableMapOf<Int, Int>()

    // 支持的铃声类型
    private val streamTypes = listOf(
        AudioManager.STREAM_MUSIC, /* 媒体音量 3 */
        AudioManager.STREAM_RING, /* 来电铃声音量 2 */
        AudioManager.STREAM_ALARM, /* 闹钟音量 4 */
        AudioManager.STREAM_VOICE_CALL /* 通话音量 0 */
    )

    /**
     * 初始化 audioManager
     * */
    fun init(context: Context, onSoundChanged: OnSoundIndexChanged? = null) = this.apply {
        applicationContext = context.applicationContext
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 记录各音量初始值
        streamTypes.forEach {
            volumeIndexMap[it] = getVolume(it)
            LoggerUtil.d(TAG, "初始音量 type=$it(0:通话 2:铃声 3:媒体 4:闹钟), ${volumeIndexMap[it]}")
        }

        // 注册音量变化回调
        settingContentObserver = SettingsContentObserver(object : OnSettingChanged {
            override fun invoke(selfChange: Boolean, uri: Uri?) {
                streamTypes.forEach { steamType ->
                    val curIndex = getVolume(steamType)
                    val lastIndex = volumeIndexMap[steamType] ?: 0
                    if (curIndex != lastIndex) {
                        LoggerUtil.d(
                            TAG,
                            "onSoundChanged $steamType(0:通话 2:铃声 3:媒体 4:闹钟), $lastIndex -> $curIndex"
                        )
                        volumeIndexMap[steamType] = curIndex
                        onSoundChanged?.invoke(steamType, lastIndex, curIndex)
                    }
                }
            }
        })

        applicationContext.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            settingContentObserver!!
        )
    }

//    /**
//     * 音量发生变化时回调监听器
//     * */
//    fun setOnSoundVolumeChange(action: OnSettingChanged?) = this.apply {
//        onSoundVolumeChange = action
//    }

    fun uninit() {
        settingContentObserver?.let {
            applicationContext.contentResolver.unregisterContentObserver(it)
        }

    }

    // 是否处于静音/勿扰模式中
    fun isSilentMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

    // 是否处于振动模式
    fun isVibrateMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE

    // 是否处于正常模式
    fun isNormalMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    // 退出勿扰/静音模式
    fun exitSilentMode() = this.apply { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL }

    /**
     * 获取当前音量
     * @param streamType 类型
     *              AudioManager.STREAM_MUSIC: 媒体音量
     *              AudioManager.STREAM_ALARM: 闹钟音量
     *              AudioManager.STREAM_RING: 铃声音量
     */
    fun getVolume(streamType: Int = DEFAULT_STREAM) =
        audioManager.getStreamVolume(streamType)

    /**
     * 获取指定声音的最大音量
     * */
    fun getMaxVolume(streamType: Int = DEFAULT_STREAM) =
        audioManager.getStreamMaxVolume(streamType)


    /**
     * 设置音量
     * @param index 音量大小
     * */
    fun setVolume(
        index: Int,
        flags: Int = AudioManager.FLAG_PLAY_SOUND,
        streamType: Int = DEFAULT_STREAM
    ) = this.apply {
        val max = audioManager.getStreamMaxVolume(streamType)
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(streamType)
        } else {
            0
        }

        val tIndex = when {
            index < min -> min
            index > max -> max
            else -> index
        }

        LoggerUtil.d(TAG, "setVolume $streamType: $tIndex")
        audioManager.setStreamVolume(streamType, tIndex, flags)
    }


    /**
     * 设置音量百分比
     * @param indexRatio 音量大小百分比 [0,1]
     * */
    fun setVolume(
        indexRatio: Double,
        flags: Int = AudioManager.FLAG_PLAY_SOUND,
        streamType: Int = DEFAULT_STREAM
    ) = this.apply {
        val max = audioManager.getStreamMaxVolume(streamType)
        val ratio = when {
            indexRatio > 1 -> 1.0
            indexRatio < 0 -> 0.0
            else -> indexRatio
        }
        var index: Int = (max * ratio).toInt()
        if (index in 1 downTo 0) {
            index = 1
        }
        setVolume(index, flags, streamType)
    }

    /**
     * 统一设置所有支持的音量值
     * @param indexRatio 音量大小百分比 [0,1]
     * */
    fun setAllVolume(indexRatio: Double) = this.apply {
        streamTypes.forEach { setVolume(indexRatio, streamType = it) }
    }

    /**
     * 统一设置所有支持的音量值
     * @param index 音量大小
     * */
    fun setAllVolume(index: Int) = this.apply {
        streamTypes.forEach { setVolume(index, streamType = it) }
    }
}