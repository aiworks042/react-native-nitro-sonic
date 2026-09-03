import { type HybridObject } from 'react-native-nitro-modules';
import type { SoundPlayer } from './SoundPlayer.nitro';
/**
 * System audio category governing hardware routing, ducking, and mute switch behavior.
 */
export type AudioCategory = 'notification' | 'ui' | 'playback' | 'ambient' | 'alarm';
/**
 * Priority level for incoming notification alerts.
 */
export type NotificationPriority = 'high' | 'normal' | 'low';
/**
 * Configuration options for general sound playback.
 */
export interface SoundOptions {
    /**
     * Volume level from 0.0 (silent) to 1.0 (full volume). Defaults to 1.0.
     */
    volume?: number;
    /**
     * Whether to loop the sound repeatedly until stopped. Defaults to false.
     */
    loop?: boolean;
    /**
     * Playback speed multiplier (0.5 to 2.0). Defaults to 1.0.
     */
    speed?: number;
    /**
     * Audio category override for this specific sound.
     */
    category?: AudioCategory;
}
/**
 * Configuration options for real-time notification chimes and alerts.
 */
export interface NotificationOptions {
    /**
     * Volume level from 0.0 to 1.0. Defaults to system notification volume.
     */
    volume?: number;
    /**
     * Whether to trigger synchronized haptic feedback / vibration alongside the chime. Defaults to true.
     */
    vibrate?: boolean;
    /**
     * Delivery priority level for audio focus and ducking. Defaults to 'high'.
     */
    priority?: NotificationPriority;
    /**
     * Whether to loop the alert (e.g. for incoming audio/video call rings). Defaults to false.
     */
    loop?: boolean;
}
/**
 * The primary Sound HybridObject for ultra-fast native audio playback and real-time notifications.
 */
export interface Sound extends HybridObject<{
    ios: 'swift';
    android: 'kotlin';
}> {
    /**
     * Plays a one-shot sound file immediately with zero bridge overhead.
     * Source can be a native resource name (e.g. 'kandid_notification_sound'),
     * an app asset path, or a file URI.
     */
    play(source: string, options?: SoundOptions): Promise<boolean>;
    /**
     * Plays a real-time notification alert chime with automatic audio focus ducking,
     * optional coupled vibration, and notification-channel routing.
     * Perfect for incoming messages, reactions, tags, and candid moments.
     */
    playNotification(soundName: string, options?: NotificationOptions): Promise<boolean>;
    /**
     * Pre-loads an audio file into native memory (or sound pool) so subsequent
     * playback triggers instantly with 0ms latency.
     */
    preload(source: string): Promise<boolean>;
    /**
     * Unloads a preloaded sound from native memory to free up resources.
     */
    unload(source: string): void;
    /**
     * Immediately stops all currently playing sounds and active notification chimes.
     */
    stopAll(): void;
    /**
     * Sets the global audio category and hardware routing behavior.
     */
    setCategory(category: AudioCategory): void;
    /**
     * Sets the master volume level (0.0 to 1.0) across all sounds played by this module.
     */
    setMasterVolume(volume: number): void;
    /**
     * Returns the current master volume level (0.0 to 1.0).
     */
    getMasterVolume(): number;
    /**
     * Sets global mute state without changing volume settings.
     */
    setMuted(muted: boolean): void;
    /**
     * Checks whether global audio output is currently muted.
     */
    isMuted(): boolean;
    /**
     * Creates a dedicated SoundPlayer handle for granular playback control
     * (play, pause, seek, loop, volume, duration).
     */
    createPlayer(source: string, options?: SoundOptions): Promise<SoundPlayer>;
}
