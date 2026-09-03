import Foundation
import AVFoundation
import NitroModules

public final class HybridSonicPlayer: HybridSoundPlayerSpec_base, HybridSoundPlayerSpec_protocol {
  private var player: AVAudioPlayer?
  private var initialVolume: Double = 1.0
  private var initialLooping: Bool = false

  public var duration: Double {
    guard let player = player else { return 0.0 }
    return player.duration * 1000.0
  }

  public var currentTime: Double {
    guard let player = player else { return 0.0 }
    return player.currentTime * 1000.0
  }

  public var isPlaying: Bool {
    return player?.isPlaying ?? false
  }

  public var isLooping: Bool {
    get {
      return player?.numberOfLoops == -1
    }
    set {
      player?.numberOfLoops = newValue ? -1 : 0
    }
  }

  public var volume: Double {
    get {
      return Double(player?.volume ?? Float(initialVolume))
    }
    set {
      let clamped = Float(min(max(newValue, 0.0), 1.0))
      player?.volume = clamped
      initialVolume = Double(clamped)
    }
  }

  public override init() {
    super.init()
  }

  public init(source: String, options: SoundOptions?) {
    super.init()
    setupPlayer(source: source, options: options)
  }

  private func setupPlayer(source: String, options: SoundOptions?) {
    let url: URL?
    if source.hasPrefix("file://") || source.hasPrefix("http://") || source.hasPrefix("https://") {
      url = URL(string: source)
    } else {
      let fileExtension = (source as NSString).pathExtension
      let fileName = (source as NSString).deletingPathExtension
      if !fileExtension.isEmpty {
        url = Bundle.main.url(forResource: fileName, withExtension: fileExtension)
      } else {
        var found: URL? = nil
        for ext in ["wav", "mp3", "m4a", "aac"] {
          if let u = Bundle.main.url(forResource: source, withExtension: ext) {
            found = u
            break
          }
        }
        url = found
      }
    }

    guard let soundUrl = url else { return }

    do {
      let audioPlayer = try AVAudioPlayer(contentsOf: soundUrl)
      let vol = options?.volume ?? 1.0
      initialVolume = vol
      audioPlayer.volume = Float(min(max(vol, 0.0), 1.0))

      let looping = options?.loop ?? false
      initialLooping = looping
      audioPlayer.numberOfLoops = looping ? -1 : 0

      if let speed = options?.speed {
        audioPlayer.enableRate = true
        audioPlayer.rate = Float(min(max(speed, 0.5), 2.0))
      }

      player = audioPlayer
    } catch {
      // Ignored
    }
  }

  public func play() throws -> Promise<Bool> {
    guard let player = player else {
      return Promise.resolved(withResult: false)
    }
    let started = player.play()
    return Promise.resolved(withResult: started)
  }

  public func pause() throws {
    player?.pause()
  }

  public func resume() throws {
    player?.play()
  }

  public func stop() throws {
    player?.stop()
    player?.currentTime = 0
  }

  public func seek(positionMs: Double) throws {
    player?.currentTime = max(positionMs, 0.0) / 1000.0
  }

  public func setSpeed(speed: Double) throws {
    guard let player = player else { return }
    player.enableRate = true
    player.rate = Float(min(max(speed, 0.5), 2.0))
  }
}
