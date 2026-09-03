# react-native-nitro-sonic 🔊⚡

Blazing-fast, zero-bridge audio and real-time notification sound library for React Native, powered by **Nitro Modules** and **C++ JSI**.

---

## Features

- ⚡ **Zero Bridge Overhead**: Directly invokes native hardware via direct C++ JSI / Nitrogen without serialization lag.
- 🔔 **Real-Time Notification Chimes**: Built-in support for incoming push alerts, message chimes, audio ducking, and coupled haptic vibration.
- 🎛️ **Stateful Sound Player**: Seek, pause, resume, loop, speed control (0.5x to 2.0x), volume, and live playback progress.
- 🔇 **Audio Focus & Categories**: Automatic routing for `'notification'`, `'ui'`, `'playback'`, `'ambient'`, and `'alarm'`.
- 🔌 **Seamless Autolinking**: Zero manual native boilerplate in `MainApplication.kt` or `AppDelegate.mm`.

---

## Installation

```bash
bun add react-native-nitro-sonic
# or
npm install react-native-nitro-sonic
# or
yarn add react-native-nitro-sonic
```

> **Note**: Requires React Native 0.75+ with the **New Architecture** enabled and `react-native-nitro-modules`.

---

## Quick Start

### 1. Play a Sound
```typescript
import { Sound } from 'react-native-nitro-sonic';

// Play a bundled native raw resource or local file
await Sound.play('kandid_notification_sound', { volume: 0.9 });
```

### 2. Play a Real-Time Notification Chime
```typescript
import { Sound } from 'react-native-nitro-sonic';

// Plays chime with automatic background audio ducking and haptic vibration
await Sound.playNotification('kandid_notification_sound', {
  vibrate: true,
  priority: 'high',
});
```

### 3. Preload for 0ms Instant Trigger
```typescript
// Pre-cache sound in native memory so taps trigger instantly
await Sound.preload('kandid_notification_sound');
```

### 4. Stateful Audio Player (Seek, Loop, Speed)
```typescript
import { Sound } from 'react-native-nitro-sonic';

const player = await Sound.createPlayer('ambient_track', {
  loop: true,
  volume: 0.8,
});

await player.play();

// Control playback
player.pause();
player.resume();
player.seek(5000); // seek to 5s
player.setSpeed(1.5); // 1.5x speed
player.stop();
```

---

## API Reference

### `Sound`
| Method | Description |
| :--- | :--- |
| `play(source, options?)` | Plays one-shot sound with zero latency |
| `playNotification(soundName, options?)` | Plays notification chime with ducking & vibration |
| `preload(source)` | Preloads audio into native memory |
| `unload(source)` | Releases preloaded audio resource |
| `stopAll()` | Immediately halts all active audio |
| `setCategory(category)` | Sets `'notification'`, `'ui'`, `'playback'`, `'ambient'`, or `'alarm'` |
| `setMasterVolume(volume)` | Sets global master volume (0.0 to 1.0) |
| `setMuted(muted)` | Toggles global audio output |
| `createPlayer(source, options?)` | Creates dedicated controllable `SoundPlayer` instance |

---

## License

MIT © [aiworks042](https://github.com/aiworks042)
