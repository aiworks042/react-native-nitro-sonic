package com.margelo.nitro.nitrosonic

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.core.Promise

@Keep
@DoNotStrip
class HybridSonicPlayer(
  private val context: Context,
  private val source: String,
  options: SoundOptions? = null
) : HybridSoundPlayerSpec() {

  private var mediaPlayer: MediaPlayer? = null

  override var isLooping: Boolean = options?.loop ?: false
    set(value) {
      field = value
      mediaPlayer?.isLooping = value
    }

  override var volume: Double = options?.volume ?: 1.0
    set(value) {
      field = value
      val v = value.toFloat().coerceIn(0f, 1f)
      mediaPlayer?.setVolume(v, v)
    }

  override val duration: Double
    get() = (mediaPlayer?.duration?.toDouble() ?: 0.0).coerceAtLeast(0.0)

  override val currentTime: Double
    get() = (mediaPlayer?.currentPosition?.toDouble() ?: 0.0).coerceAtLeast(0.0)

  override val isPlaying: Boolean
    get() = mediaPlayer?.isPlaying ?: false

  init {
    val cleanName = source.substringBeforeLast(".")
    var resId = context.resources.getIdentifier(cleanName, "raw", context.packageName)
    if (resId == 0) {
      resId = context.resources.getIdentifier(source, "raw", context.packageName)
    }
    if (resId != 0) {
      mediaPlayer = MediaPlayer.create(context, resId)?.apply {
        isLooping = this@HybridSonicPlayer.isLooping
        val v = volume.toFloat().coerceIn(0f, 1f)
        setVolume(v, v)
      }
    }
  }

  override fun play(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val mp = mediaPlayer
      if (mp != null) {
        mp.start()
        promise.resolve(true)
      } else {
        promise.resolve(false)
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
    return promise
  }

  override fun pause() {
    try {
      mediaPlayer?.pause()
    } catch (_: Throwable) {}
  }

  override fun resume() {
    try {
      mediaPlayer?.start()
    } catch (_: Throwable) {}
  }

  override fun stop() {
    try {
      mediaPlayer?.stop()
      mediaPlayer?.prepare()
    } catch (_: Throwable) {}
  }

  override fun seek(positionMs: Double) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mediaPlayer?.seekTo(positionMs.toLong(), MediaPlayer.SEEK_CLOSEST)
      } else {
        @Suppress("DEPRECATION")
        mediaPlayer?.seekTo(positionMs.toInt())
      }
    } catch (_: Throwable) {}
  }

  override fun setSpeed(speed: Double) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val params = mediaPlayer?.playbackParams
        if (params != null) {
          mediaPlayer?.playbackParams = params.setSpeed(speed.toFloat())
        }
      }
    } catch (_: Throwable) {}
  }
}
