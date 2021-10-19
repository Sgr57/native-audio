<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Native Audio</h3>
<p align="center"><strong><code>@capacitor-community/native-audio</code></strong></p>
<p align="center">
  Capacitor community plugin for playing sounds.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2021?style=flat-square" />
  <a href="https://github.com/capacitor-community/native-audio/actions?query=workflow%3A%22Test+and+Build+Plugin%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/native-audio/Test%20and%20Build%20Plugin?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/l/@capacitor-community/native-audio?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/dw/@capacitor-community/native-audio?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/v/@capacitor-community/native-audio?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-6-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

# Capacitor Native Audio Plugin

Capacitor plugin for native audio engine.
Capacitor v3 - ✅ Support!

Click on video to see example 💥

[![YouTube Example](https://img.youtube.com/vi/XpUGlWWtwHs/0.jpg)](https://www.youtube.com/watch?v=XpUGlWWtwHs)


## Maintainers

| Maintainer    | GitHub                                      | Social                              |
| ------------- | ------------------------------------------- | ----------------------------------- |
| Maxim Bazuev  | [bazuka5801](https://github.com/bazuka5801) | [Telegram](https://t.me/bazuka5801) |

Mainteinance Status: Actively Maintained

## Preparation
All audio place in specific platform folder

Andoid: `android/app/src/assets`

iOS: `ios/App/App/sounds`

## Installation

To use npm

```bash
npm install @capacitor-community/native-audio
```

To use yarn

```bash
yarn add @capacitor-community/native-audio
```

Sync native files

```bash
npx cap sync
```

On iOS and Android, no further steps are needed.

## Configuration

No configuration required for this plugin.
<docgen-config>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->



</docgen-config>

## Supported methods

| Name           | Android | iOS | Web |
| :------------- | :------ | :-- | :-- |
| configure      | ✅      | ✅  | ❌  |
| preloadSimple  | ✅      | ✅  | ❌  |
| preloadComplex | ✅      | ✅  | ❌  |
| play           | ✅      | ✅  | ❌  |
| loop           | ✅      | ✅  | ❌  |
| stop           | ✅      | ✅  | ❌  |
| unload         | ✅      | ✅  | ❌  |
| setVolume      | ✅      | ✅  | ❌  |
| getDuration    | ✅      | ✅  | ❌  |
| getCurrentTime | ✅      | ✅  | ❌  |

## Usage

[Example repository](https://github.com/bazuka5801/native-audio-example)

```typescript
import {NativeAudio} from '@capacitor-community/native-audio'


/**
 * This method will load more optimized audio files for background into memory.
 * @param track - title of the track; string, optional, default ''
 *        artist - string, optional, default ''
 *        album - string, optional, default ''
 *        cover - can be a local path (use fullpath 'file:///storage/emulated/...', or only 'my_image.jpg' if my_image.jpg is in the www folder of your app)
 *     	          or a remote url ('http://...', 'https://...', 'ftp://...'), string, optional
 *        hasPrev - show previous button, optional, default: true
 *        hasNext - show next button, optional, default: true
 *        hasClose - show close button, optional, default: false
 *        duration - number, optional, default: 0, iOS ONLY
 *        elapsed - number, optional, default: 0, iOS ONLY
 *        hasSkipForward - boolean, optional, default: false. true value overrides hasNext, iOS ONLY
 *        hasSkipBackward - boolean, optional, default: false. true value overrides hasPrev, iOS ONLY
 *        skipForwardInterval - number, optional. default: 15, iOS ONLY
 *        skipBackwardInterval - number, optional. default: 15, iOS ONLY
 *        hasScrubbing - boolean, optional. default to false. Enable scrubbing from control center progress bar, iOS ONLY
 *        isPlaying - boolean, optional, default : true, ANDROID ONLY
 *        dismissable - boolean,	optional, default : false, ANDROID ONLY
 *        ticker - string, text displayed in the status bar when the notification (and the ticker) are updated
 *        playIcon: 'media_play',A ll icons default to their built-in android equivalents, The supplied drawable name, e.g. 'media_play', is the name of a drawable found under android/res/drawable folders
 *        pauseIcon: 'media_pause', ANDROID ONLY
 *        prevIcon: 'media_prev', ANDROID ONLY
 *        nextIcon: 'media_next', ANDROID ONLY
 *        closeIcon: 'media_close', ANDROID ONLY
 *        notificationIcon: 'notification', ANDROID ONLY
 
 * @returns void
 */
NativeAudio.preload({
    track       : 'Time is Running Out',
    artist      : 'Muse',
    album       : 'Absolution',
    cover       : 'albums/absolution.jpg',
    hasPrev   : false,
    hasNext   : false,
    hasClose  : true,
    duration : 60,
    elapsed : 10,
    hasSkipForward : true,
    hasSkipBackward : true,
    skipForwardInterval : 15,
    skipBackwardInterval : 15,
    hasScrubbing : false,
    isPlaying   : true,
    dismissable : true,
    ticker	  : 'Now playing "Time is Running Out"',
    playIcon: 'media_play',
    pauseIcon: 'media_pause',
    prevIcon: 'media_prev',
    nextIcon: 'media_next',
    closeIcon: 'media_close',
    notificationIcon: 'notification'
});

/**
 * This method will play the loaded audio file if present in the memory.
 * @param assetId - identifier of the asset
 * @param time - (optional) play with seek. example: 6.0 - start playing track from 6 sec
 * @returns void
 */
NativeAudio.play({
    assetId: 'fire',
    // time: 6.0 - seek time
});

/**
 * This method will loop the audio file for playback.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.loop({
  assetId: 'fire',
});


/**
 * This method will stop the audio file if it's currently playing.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.stop({
  assetId: 'fire',
});

/**
 * This method will unload the audio file from the memory.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.unload({
  assetId: 'fire',
});

/**
 * This method will set the new volume for a audio file.
 * @param assetId - identifier of the asset
 *        volume - numerical value of the volume between 0.1 - 1.0
 * @returns void
 */
NativeAudio.setVolume({
  assetId: 'fire',
  volume: 0.4,
});

/**
 * This method will get the current volume for a audio file.
 * @param assetId - identifier of the asset
 * @returns number
 */
NativeAudio.getVolume({
  assetId: 'fire',
});

/**
 * this method will get the duration of an audio file.
 * only works if channels == 1
 */
NativeAudio.getDuration({
  assetId: 'fire'
})
.then(result => {
  console.log(result.duration);
})

/**
 * this method will get the current time of a playing audio file.
 * only works if channels == 1
 */
NativeAudio.getCurrentTime({
  assetId: 'fire'
});
.then(result => {
  console.log(result.currentTime);
})

/**
 * This method will set the new current time for a audio file.
 * @param assetId - identifier of the asset
 *        currentTime - numerical value in seconds
 * @returns void
 */
NativeAudio.setCurrentTime({
  assetId: 'fire',
  currentTime: 123455,
});

// Listen for events and pass them to your handler function

NativeAudio.addListener('controlsNotification', (info: any) => {
    console.log('controlsNotification was fired');
    console.log(info);
    handleControlsEvent(info);
});

function handleControlsEvent(action) {
	console.log("hello from handleControlsEvent")
	const message = action.message;
	console.log("message: " + message)
	switch(message) {
		case 'STATE_NONE':
			// none
			break;
		case 'STATE_STOPPED':
			// stop
			break;
		case 'STATE_PLAYING':
			// playing
			break;
		case 'STATE_PAUSED':
			// paused
			break;
		case 'STATE_BUFFERING':
			// buffering
			break;
		case 'STATE_ERROR':
			// error
			break;
    		case 'STATE_CONNECTING':
			// connecting
			break;
    		case 'OTHER':
			// other
			break;
	}
}

NativeAudio.addListener('currentTime', (currentTime: number) => {
    console.log('currentTime:', currentTime);
});

```

## API

<docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### configure(...)

```typescript
configure(options: ConfigureOptions) => Promise<void>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#configureoptions">ConfigureOptions</a></code> |

--------------------


### preload(...)

```typescript
preload(options: PreloadOptions) => Promise<void>
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#preloadoptions">PreloadOptions</a></code> |

--------------------


### play(...)

```typescript
play(options: { assetId: string; time: number; }) => Promise<void>
```

| Param         | Type                                            |
| ------------- | ----------------------------------------------- |
| **`options`** | <code>{ assetId: string; time: number; }</code> |

--------------------


### resume(...)

```typescript
resume(options: { assetId: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### pause(...)

```typescript
pause(options: { assetId: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### loop(...)

```typescript
loop(options: { assetId: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### stop(...)

```typescript
stop(options: { assetId: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### unload(...)

```typescript
unload(options: { assetId: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### setVolume(...)

```typescript
setVolume(options: { assetId: string; volume: number; }) => Promise<void>
```

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code>{ assetId: string; volume: number; }</code> |

--------------------

### getVolume(...)

```typescript
getVolume(options: { assetId: string; }) => Promise<{volume: number;}>
```

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

--------------------


### getCurrentTime(...)

```typescript
getCurrentTime(options: { assetId: string; }) => Promise<{ currentTime: number; }>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

**Returns:** <code>Promise&lt;{ currentTime: number; }&gt;</code>

--------------------
  
### setCurrentTime(...)

```typescript
setCurrentTime(options: { currentTime: number; }) => Promise<{ currentTime: number; }>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ currentTime: number, value in seconds; }</code> |

**Returns:** <code>Promise&lt;{ currentTime: number; }&gt;</code>

--------------------


### getDuration(...)

```typescript
getDuration(options: { assetId: string; }) => Promise<{ duration: number; }>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ assetId: string; }</code> |

**Returns:** <code>Promise&lt;{ duration: number; }&gt;</code>

--------------------


### Interfaces


#### ConfigureOptions

| Prop       | Type                 |
| ---------- | -------------------- |
| **`fade`** | <code>boolean</code> |


#### PreloadOptions

| Prop                       | Type                 |
| -------------------------- | -------------------- |
| **`track`**                | <code>string, optiona, default: ''</code>  |
| **`artist`**               | <code>string, optional, default ''</code>  |
| **`album`**                | <code>string, optional, default ''</code>  |
| **`cover`**                | <code>can be a local path (use fullpath 'file:///storage/emulated/...', or only 'my_image.jpg' if my_image.jpg is in the www folder of your app) or a remote url ('http://...', 'https://...', 'ftp://...'), string, optional</code>|
| **`hasPrev`**              | <code>show previous button, boolean, optional, default: true</code> |
| **`hasNext`**              | <code>show next button, optional, default: true</code> |
| **`hasClose`**             | <code>show close button, optional, default: false</code> |
| **`duration`**             | <code>number, optional, default: 0, iOS ONLY</code> |
| **`elapsed`**              | <code>number, optional, default: 0, iOS ONLY</code> |
| **`hasSkipForward`**       | <code>boolean, optional, default: false. true value overrides hasNext, iOS ONLY</code> |
| **`hasSkipBackward`**      | <code>boolean, optional, default: false. true value overrides hasPrev, iOS ONLY</code> |
| **`skipForwardInterval`**  | <code>number, optional. default: 15, iOS ONLY</code> |
| **`skipBackwardInterval`** | <code>number, optional. default: 15, iOS ONLY</code> |
| **`hasScrubbing`**         | <code>boolean, optional. default to false. Enable scrubbing from control center progress bar, iOS ONLY</code> |
| **`isPlaying`**            | <code>boolean, optional, default : true, ANDROID ONLY</code> |
| **`dismissable`**          | <code>boolean,	optional, default : false, ANDROID ONLY</code> |
| **`ticker`**               | <code>string, text displayed in the status bar when the notification (and the ticker) are updated</code> |
| **`playIcon`**             | <code>'media_play',A ll icons default to their built-in android equivalents, The supplied drawable name, e.g. 'media_play', is the name of a drawable found under android/res/drawable folders, ANDROID ONLY</code> |
| **`pauseIcon`**            | <code>'media_pause', ANDROID ONLY</code> |
| **`prevIcon`**             | <code>'media_prev', ANDROID ONLY</code> |
| **`nextIcon`**             | <code>'media_next', ANDROID ONLY</code> |
| **`closeIcon`**            | <code>'media_close', ANDROID ONLY</code> |
| **`notificationIcon`**     | <code>'notification', ANDROID ONLY</code> |
  
###Notification
  Possible values:
* STATE_NONE
* STATE_STOPPED
* STATE_PLAYING
* STATE_PAUSED
* STATE_BUFFERING
* STATE_ERROR
* STATE_CONNECTING (?)
* OTHER

</docgen-api>
