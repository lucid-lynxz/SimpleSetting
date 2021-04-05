package org.lynxz.simplesetting.util

import android.media.AudioManager

/**
 * 声音数据
 * */
data class SoundInfoBean(
    val streamType: Int, // 音量类型, 参考 AudioManager.STREAM_MUSIC  等类型
    val maxVolume: Int, // 最大可用值
    val minVolume: Int, // 最小值
    var index: Int // 当前值
) {

    /**
     * 当前音量百分比(double类型), 如: 0.8
     * */
    fun getRatio() = (index * 1.0) / 1.coerceAtLeast(maxVolume)

    /**
     * 当前电量百分比形式, 如: 80%
     * */
    fun getPercent() = "${(getRatio() * 100).toInt()}%"

    /**
     * 获取当前声音类型
     * */
    fun getSoundName() = Companion.getSoundName(index)

    override fun toString(): String {
        return "类型:${getSoundName()},当前音量:$index,${getPercent()},取值范围:[$minVolume,$maxVolume]"
    }

    companion object {
        /**
         * 获取声音类型
         * */
        fun getSoundName(streamType: Int) = when (streamType) {
            AudioManager.STREAM_ALARM -> "闹钟"
            AudioManager.STREAM_RING -> "来电铃声"
            AudioManager.STREAM_MUSIC -> "媒体"
            AudioManager.STREAM_VOICE_CALL -> "通话"
            else -> "未知类型"
        }
    }
}