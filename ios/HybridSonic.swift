import Foundation
import AVFoundation
import AudioToolbox
#if canImport(UIKit)
import UIKit
#endif
import NitroModules

public final class HybridSonic: HybridSoundSpec_base, HybridSoundSpec_protocol {
  private var masterVolume: Double = 1.0
  private var isMutedState: Bool = false
  private var currentCategory: AudioCategory = .notification
  private var activePlayers = [AVAudioPlayer]()
  private let lock = NSLock()

  public override init() {
    super.init()
  }

  private func resolveURL(for source: String) -> URL? {
    if source.hasPrefix("file://") || source.hasPrefix("http://") || source.hasPrefix("https://") {
      return URL(string: source)
    }

    let fileExtension = (source as NSString).pathExtension
    let fileName = (source as NSString).deletingPathExtension

    if !fileExtension.isEmpty {
      if let url = Bundle.main.url(forResource: fileName, withExtension: fileExtension) {
        return url
      }
    }

    let extensions = ["wav", "mp3", "m4a", "aac", "caf", "aif"]
    for ext in extensions {
      if let url = Bundle.main.url(forResource: source, withExtension: ext) {
        return url
      }
    }

    return nil
  }

  public func play(source: String, options: SoundOptions?) throws -> Promise<Bool> {
    if isMutedState {
      return Promise.resolved(withResult: false)
    }

    guard let url = resolveURL(for: source) else {
      return Promise.resolved(withResult: false)
    }

    do {
      let player = try AVAudioPlayer(contentsOf: url)
      let volume = Float(((options?.volume ?? 1.0) * masterVolume).clamped(to: 0.0...1.0))
      player.volume = volume
      player.numberOfLoops = (options?.loop ?? false) ? -1 : 0

      if let speed = options?.speed {
        player.enableRate = true
        player.rate = Float(speed.clamped(to: 0.5...2.0))
      }

      configureAudioCategory(options?.category ?? currentCategory)

      lock.lock()
      activePlayers.append(player)
      lock.unlock()

      player.play()
      return Promise.resolved(withResult: true)
    } catch {
      return Promise.rejected(withError: error)
    }
  }

  public func playNotification(soundName: String, options: NotificationOptions?) throws -> Promise<Bool> {
    if isMutedState {
      return Promise.resolved(withResult: false)
    }

    if options?.vibrate != false {
      #if canImport(UIKit)
      DispatchQueue.main.async {
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(.success)
      }
      #endif
    }

    guard let url = resolveURL(for: soundName) else {
      return Promise.resolved(withResult: false)
    }

    do {
      let player = try AVAudioPlayer(contentsOf: url)
      let volume = Float(((options?.volume ?? 1.0) * masterVolume).clamped(to: 0.0...1.0))
      player.volume = volume
      player.numberOfLoops = (options?.loop ?? false) ? -1 : 0

      try? AVAudioSession.sharedInstance().setCategory(
        .ambient,
        mode: .default,
        options: [.mixWithOthers, .duckOthers]
      )
      try? AVAudioSession.sharedInstance().setActive(true)

      lock.lock()
      activePlayers.append(player)
      lock.unlock()

      player.play()
      return Promise.resolved(withResult: true)
    } catch {
      return Promise.rejected(withError: error)
    }
  }

  public func preload(source: String) throws -> Promise<Bool> {
    let exists = resolveURL(for: source) != nil
    return Promise.resolved(withResult: exists)
  }

  public func unload(source: String) throws {
    // Handled automatically on player completion
  }

  public func stopAll() throws {
    lock.lock()
    defer { lock.unlock() }
    for player in activePlayers {
      player.stop()
    }
    activePlayers.removeAll()
  }

  public func setCategory(category: AudioCategory) throws {
    currentCategory = category
    configureAudioCategory(category)
  }

  private func configureAudioCategory(_ category: AudioCategory) {
    let session = AVAudioSession.sharedInstance()
    do {
      switch category {
      case .notification:
        try session.setCategory(.ambient, mode: .default, options: [.mixWithOthers, .duckOthers])
      case .ui:
        try session.setCategory(.ambient, mode: .default, options: [.mixWithOthers])
      case .playback:
        try session.setCategory(.playback, mode: .default, options: [])
      case .ambient:
        try session.setCategory(.ambient, mode: .default, options: [.mixWithOthers])
      case .alarm:
        try session.setCategory(.playback, mode: .default, options: [])
      }
      try session.setActive(true)
    } catch {
      // Ignored
    }
  }

  public func setMasterVolume(volume: Double) throws {
    masterVolume = volume.clamped(to: 0.0...1.0)
  }

  public func getMasterVolume() throws -> Double {
    return masterVolume
  }

  public func setMuted(muted: Bool) throws {
    isMutedState = muted
    if muted {
      try? stopAll()
    }
  }

  public func isMuted() throws -> Bool {
    return isMutedState
  }

  public func createPlayer(source: String, options: SoundOptions?) throws -> Promise<any HybridSoundPlayerSpec> {
    let player = HybridSonicPlayer(source: source, options: options)
    return Promise.resolved(withResult: player)
  }
}

private extension Comparable {
  func clamped(to limits: ClosedRange<Self>) -> Self {
    return min(max(self, limits.lowerBound), limits.upperBound)
  }
}
