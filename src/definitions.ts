import { PluginListenerHandle } from "@capacitor/core";
export interface NativeAudio {
  configure(options: ConfigureOptions): Promise<void>;
  preload(options: PreloadOptions): Promise<void>;
  play(options: { assetId: string; time: number }): Promise<void>;
  resume(options: { assetId: string }): Promise<void>;
  pause(options: { assetId: string }): Promise<void>;
  loop(options: { assetId: string }): Promise<void>;
  stop(options: { assetId: string }): Promise<void>;
  unload(options: { assetId: string }): Promise<void>;
  setVolume(options: { assetId: string; volume: number }): Promise<void>;
  getVolume(options: { assetId: string}): Promise<{ volume: number }>;
  getCurrentTime(options: { assetId: string; }): Promise<{ time: number }>;
  setCurrentTime(options: { assetId: string; time: number; }): Promise<void>;
  getDuration(options: { assetId: string }): Promise<{ duration: number }>;
  addListener(event: string, callback: (info: { status: any; position: number }) => void): PluginListenerHandle;
}

export interface ConfigureOptions {
  fade?: boolean;
}

export interface PreloadOptions {
  assetPath: string;
  assetId: string;
  volume?: number;
  audioChannelNum?: number;
  isUrl?: boolean;
  trackName?: string; // TODO: to be mandatory
  artist?: string;
  album?: string;
  cover?: string;
  hasPrev?: boolean;
  hasNext?: boolean;
  hasClose?: boolean;
  duration?: number;
  elapsed?: number;
  hasSkipForward?: boolean;
  hasSkipBackward?: boolean;
  skipForwardInterval?: number;
  skipBackwardInterval?: number;
  hasScrubbing?: boolean;
  isPlaying?: boolean;
  dismissible?: boolean;
  ticker?: string;
  playIcon?: string;
  pauseIcon?: string;
  prevIcon?: string;
  nextIcon?: string;
  closeIcon?: string;
  notificationIcon?: string;
}
