package org.lynxz.simplesetting.util

import android.content.Context
import android.media.AudioManager

/**
 * 声音管理, 用于调节音量, 获取当前音量等
 * */
object SoundUtil {
    lateinit var audioManager: AudioManager

    /**
     * 初始化 audioManager
     * */
    fun init(context: Context) {
        audioManager =
            context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // 是否处于静音模式中
    fun isMuteMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

    // 是否处于振动模式
    fun isVibrateMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE

    // 是否处于正常模式
    fun isNormalMode() = audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    /**
     * 获取当前音量
     * @param streamType 类型
     *              AudioManager.STREAM_MUSIC: 媒体音量
     *              AudioManager.STREAM_ALARM: 闹钟音量
     *              AudioManager.STREAM_RING: 铃声音量
     */
    fun getVolume(streamType: Int = AudioManager.STREAM_RING) =
        audioManager.getStreamVolume(streamType)


    /**
     * 设置音量
     * @param index 音量大小
     * */
    fun setVolume(
        index: Int,
        flags: Int = AudioManager.FLAG_PLAY_SOUND,
        streamType: Int = AudioManager.STREAM_RING
    ) = audioManager.setStreamVolume(streamType, index, flags)


}