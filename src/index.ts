import { NitroModules } from 'react-native-nitro-modules';
import type { Sound as SoundSpec } from './specs/Sound.nitro';

export type {
  AudioCategory,
  NotificationOptions,
  NotificationPriority,
  Sound as SoundType,
  SoundOptions,
} from './specs/Sound.nitro';
export type { SoundPlayer } from './specs/SoundPlayer.nitro';

export const Sound = NitroModules.createHybridObject<SoundSpec>('Sound');
