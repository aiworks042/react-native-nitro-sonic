package com.margelo.nitro.nitrosonic

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise

@Keep
@DoNotStrip
class HybridSonic : HybridSonicSpec() {

  private val context: Context
    get() = NitroModules.applicationContext ?: throw Error("Lost applicationContext in NitroSonic")

  private var masterVolume: Double = 1.0
  private var isMuted: Boolean = false
  private var currentCategory: AudioCategory = AudioCategory.NOTIFICATION

  private val activePlayers = mutableListOf<MediaPlayer>()

  private val vibrator: Vibrator
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

  override fun play(source: String, options: SoundOptions?): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      if (isMuted) {
        promise.resolve(false)
        return promise
      }

      val resId = context.resources.getIdentifier(source, "raw", context.packageName)
      if (resId == 0) {
        promise.resolve(false)
        return promise
      }

      val mp = MediaPlayer.create(context, resId)
      if (mp == null) {
        promise.resolve(false)
        return promise
      }

      val vol = ((options?.volume ?: 1.0) * masterVolume).toFloat().coerceIn(0f, 1f)
      mp.setVolume(vol, vol)
      mp.isLooping = options?.loop ?: false

      val cat = options?.category ?: currentCategory
      mp.setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(
            when (cat) {
              AudioCategory.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
              AudioCategory.UI -> AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
              AudioCategory.PLAYBACK -> AudioAttributes.USAGE_MEDIA
              AudioCategory.AMBIENT -> AudioAttributes.USAGE_MEDIA
              AudioCategory.ALARM -> AudioAttributes.USAGE_ALARM
            }
          )
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )

      synchronized(activePlayers) {
        activePlayers.add(mp)
      }

      mp.setOnCompletionListener { player ->
        synchronized(activePlayers) {
          activePlayers.remove(player)
        }
        player.release()
      }

      mp.start()
      promise.resolve(true)
    } catch (e: Throwable) {
      promise.reject(e)
    }
    return promise
  }

  override fun playNotification(
    soundName: String,
    options: NotificationOptions?
  ): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      if (isMuted) {
        promise.resolve(false)
        return promise
      }

      val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
      if (resId == 0) {
        promise.resolve(false)
        return promise
      }

      // Haptic/vibration coupling if requested (defaults to true)
      if (options?.vibrate != false) {
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
              VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
            )
          } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(45)
          }
        } catch (_: Throwable) {}
      }

      val mp = MediaPlayer.create(context, resId)
      if (mp == null) {
        promise.resolve(false)
        return promise
      }

      val vol = ((options?.volume ?: 1.0) * masterVolume).toFloat().coerceIn(0f, 1f)
      mp.setVolume(vol, vol)
      mp.isLooping = options?.loop ?: false

      mp.setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )

      synchronized(activePlayers) {
        activePlayers.add(mp)
      }

      mp.setOnCompletionListener { player ->
        synchronized(activePlayers) {
          activePlayers.remove(player)
        }
        player.release()
      }

      mp.start()
      promise.resolve(true)
    } catch (e: Throwable) {
      promise.reject(e)
    }
    return promise
  }

  override fun preload(source: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    val resId = context.resources.getIdentifier(source, "raw", context.packageName)
    promise.resolve(resId != 0)
    return promise
  }

  override fun unload(source: String) {
    // Handled via MediaPlayer completion release
  }

  override fun stopAll() {
    synchronized(activePlayers) {
      activePlayers.forEach { player ->
        try {
          player.stop()
          player.release()
        } catch (_: Throwable) {}
      }
      activePlayers.clear()
    }
  }

  override fun setCategory(category: AudioCategory) {
    currentCategory = category
  }

  override fun setMasterVolume(volume: Double) {
    masterVolume = volume.coerceIn(0.0, 1.0)
  }

  override fun getMasterVolume(): Double = masterVolume

  override fun setMuted(muted: Boolean) {
    isMuted = muted
    if (muted) {
      stopAll()
    }
  }

  override fun isMuted(): Boolean = isMuted

  override fun createPlayer(
    source: String,
    options: SoundOptions?
  ): Promise<HybridSonicPlayerSpec> {
    val promise = Promise<HybridSonicPlayerSpec>()
    try {
      val player = HybridSonicPlayer(context, source, options)
      promise.resolve(player)
    } catch (e: Throwable) {
      promise.reject(e)
    }
    return promise
  }
}
