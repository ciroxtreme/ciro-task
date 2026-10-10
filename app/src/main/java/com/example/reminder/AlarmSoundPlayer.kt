package com.example.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.util.AlarmLogger

object AlarmSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false

    @Synchronized
    fun play(context: Context) {
        if (isPlaying) return
        isPlaying = true

        try {
            AlarmLogger.log(context, "🔊 Memulai audio USAGE_ALARM dan getaran alarm...")
            val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            var mediaPlayerStarted = false
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context.applicationContext, alertUri)
                    setAudioAttributes(audioAttributes)
                    isLooping = true
                    prepare()
                    start()
                }
                mediaPlayerStarted = true
            } catch (mediaEx: Exception) {
                AlarmLogger.log(context, "⚠️ MediaPlayer gagal (${mediaEx.message}), mencoba fallback Ringtone...")
                try {
                    ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)?.apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            this.audioAttributes = audioAttributes
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            isLooping = true
                        }
                        play()
                    }
                } catch (ringtoneEx: Exception) {
                    AlarmLogger.log(context, "⚠️ Fallback Ringtone juga gagal: ${ringtoneEx.message}")
                }
            }

            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800, 400), 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 800, 400, 800, 400), 0)
            }
        } catch (e: Exception) {
            AlarmLogger.log(context, "⚠️ Gagal memulai audio/getaran: ${e.message}")
            e.printStackTrace()
        }
    }

    @Synchronized
    fun stop(context: Context? = null) {
        isPlaying = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }

        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ringtone = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            vibrator = null
        }

        context?.let {
            AlarmLogger.log(it, "🔇 Audio & getaran alarm dihentikan.")
        }
    }
}
