import { type HybridObject } from 'react-native-nitro-modules';
/**
 * Stateful audio player instance for continuous or controllable sound playback.
 * Useful for voice notes, long audio previews, ambient loops, and seekable sounds.
 */
export interface SoundPlayer extends HybridObject<{
    ios: 'swift';
    android: 'kotlin';
}> {
    /**
     * Starts or resumes playback.
     */
    play(): Promise<boolean>;
    /**
     * Pauses the playback, maintaining the current playback position.
     */
    pause(): void;
    /**
     * Resumes playback from the current position.
     */
    resume(): void;
    /**
     * Stops playback and resets position to 0.
     */
    stop(): void;
    /**
     * Seeks to a specific timestamp in milliseconds.
     */
    seek(positionMs: number): void;
    /**
     * Total duration of the audio in milliseconds. Returns 0 if unknown or stream.
     */
    readonly duration: number;
    /**
     * Current playback position in milliseconds.
     */
    readonly currentTime: number;
    /**
     * Whether the player is actively outputting sound.
     */
    readonly isPlaying: boolean;
    /**
     * Whether the sound loops continuously when reaching the end.
     */
    isLooping: boolean;
    /**
     * Playback volume level from 0.0 (silent) to 1.0 (full).
     */
    volume: number;
    /**
     * Sets playback speed multiplier (e.g. 1.0 = normal, 1.5 = 1.5x, 2.0 = 2x).
     */
    setSpeed(speed: number): void;
}
