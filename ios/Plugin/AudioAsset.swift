//
//  AudioAsset.swift
//  Plugin
//
//  Created by priyank on 2020-05-29.
//  Copyright © 2020 Max Lynch. All rights reserved.
//

import AVFoundation

public class AudioAsset: NSObject, AVAudioPlayerDelegate {
    
    var channels: NSMutableArray = NSMutableArray()
    var playIndex: Int = 0
    var assetId: String = ""
    var initialVolume: NSNumber = 1.0
    var fadeDelay: NSNumber = 1.0
    var owner: NativeAudio
    var isRemote: Bool = false
    
    let FADE_STEP: Float = 0.05
    let FADE_DELAY: Float = 0.08
    
    init(owner:NativeAudio, withAssetId assetId:String, withPath path: String!, withChannels channels: NSNumber!, withVolume volume: NSNumber!, withFadeDelay delay: NSNumber!, isRemote: Bool = false) {

        self.owner = owner
        self.assetId = assetId
        self.channels = NSMutableArray.init(capacity: channels as! Int)
        self.isRemote = isRemote
        super.init()
        
        let pathUrl: NSURL! = NSURL.fileURL(withPath: path) as NSURL
        
        if isRemote {
            guard let url = URL(string: path) else {return}
            //                guard let url = URL(string: "https://bcbolt446c5271-a.akamaihd.net/media/v1/pmp4/static/clear/4090876667001/5d672396-3c3c-4d86-b721-09b44f3be7a8/a1db436c-017e-4b19-adfc-42b4b1156fca/main.mp4?akamai_token=exp=1633720378~acl=/media/v1/pmp4/static/clear/4090876667001/5d672396-3c3c-4d86-b721-09b44f3be7a8/a1db436c-017e-4b19-adfc-42b4b1156fca/main.mp4*~hmac=9ab216f794d4ea565db03f367e4bbb8709755f9f4c41380cbb2df62dbe903558") else {return}
            
            let item = AVPlayerItem(url: url)
            
            let player = AVPlayer(playerItem: item)
            
            player.volume = volume.floatValue
            self.channels.addObjects(from: [player as Any])
            
            NotificationCenter.default.addObserver(self, selector: #selector(playerDidFinishPlaying), name: .AVPlayerItemDidPlayToEndTime, object: nil)
            
        } else {
            
            for _ in 0..<channels.intValue {
                do {
                    let player: AVAudioPlayer! = try AVAudioPlayer(contentsOf: pathUrl as URL)
                    
                    if player != nil {
                        player.volume = volume.floatValue
                        player.prepareToPlay()
                        self.channels.addObjects(from: [player as Any])
                        if channels == 1 {
                            player.delegate = self
                        }
                    }
                } catch {
                    
                }
            }
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func getCurrentTime() -> TimeInterval {
        if channels.count != 1 {
            return 0
        }
        return isRemote ? (channels.object(at: playIndex) as? AVPlayer)?.currentTime().seconds ?? 0 : (channels.object(at: playIndex) as? AVAudioPlayer)?.currentTime ?? 0
    }
    
    func getDuration() -> TimeInterval {
        if channels.count != 1 {
            return 0
        }
        return isRemote ? (channels.object(at: playIndex) as? AVPlayer)?.currentItem?.duration.seconds ?? 0 : (channels.object(at: playIndex) as? AVAudioPlayer)?.duration ?? 0
    }

    func play(time: TimeInterval) {
        if isRemote {
            let player: AVPlayer = channels.object(at: playIndex) as! AVPlayer
            player.currentItem?.seek(to: CMTimeMakeWithSeconds(time, preferredTimescale: 60000), completionHandler: nil)
            player.play()
        } else {
            let player: AVAudioPlayer = channels.object(at: playIndex) as! AVAudioPlayer
            player.currentTime = time
            player.numberOfLoops = 0
            player.play()
        }
        playIndex = Int(truncating: NSNumber(value: playIndex + 1))
        playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
    }
    
    func playWithFade(time: TimeInterval) {
        if isRemote {
            let player: AVPlayer = channels.object(at: playIndex) as! AVPlayer
            player.currentItem?.seek(to: CMTimeMakeWithSeconds(time, preferredTimescale: player.currentItem?.currentTime().timescale ?? 60000), completionHandler: nil)
            
            if player.rate != 0 && player.error == nil {
                if player.volume < initialVolume.floatValue {
                    player.volume = player.volume + self.FADE_STEP
                }
            } else {
                player.volume = 0
                player.play()
                playIndex = Int(truncating: NSNumber(value: playIndex + 1))
                playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
                
            }
            
        } else {
            let player: AVAudioPlayer! = channels.object(at: playIndex) as? AVAudioPlayer
            player.currentTime = time
            
            if !player.isPlaying {
                player.numberOfLoops = 0
                player.volume = 0
                player.play()
                playIndex = Int(truncating: NSNumber(value: playIndex + 1))
                playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
            } else {
                if player.volume < initialVolume.floatValue {
                    player.volume = player.volume + self.FADE_STEP
                }
            }
        }
  
    }

    func pause() {
        if isRemote {
            (channels.object(at: playIndex) as! AVPlayer).pause()
        } else {
            let player: AVAudioPlayer = channels.object(at: playIndex) as! AVAudioPlayer
            player.pause()
        }
    }

    func resume() {
        if isRemote {
            let player: AVPlayer = channels.object(at: playIndex) as! AVPlayer
            let timeOffset = CMTimeMakeWithSeconds(CMTimeGetSeconds(player.currentTime()) + 5, preferredTimescale: player.currentTime().timescale);
            player.seek(to: timeOffset)
            player.play()
            
        } else {
            let player: AVAudioPlayer = channels.object(at: playIndex) as! AVAudioPlayer
            
            let timeOffset = player.deviceCurrentTime + 0.01
            player.play(atTime: timeOffset)
        }
    }
    
    func stop() {
        for i in 0..<channels.count {
            if isRemote {
                let player: AVPlayer! = channels.object(at: i) as? AVPlayer
                player.pause()
            } else {
                let player: AVAudioPlayer! = channels.object(at: i) as? AVAudioPlayer
                player.stop()
            }
        }
    }
    
    func stopWithFade() {
        
        if isRemote {
            let player: AVPlayer = channels.object(at: playIndex) as! AVPlayer
 
            if player.rate != 0 && player.error == nil {
                if player.volume < initialVolume.floatValue
                {
                    player.volume = player.volume + self.FADE_STEP
                }
            } else {
                player.seek(to: CMTimeMakeWithSeconds(0, preferredTimescale: player.currentTime().timescale))
                player.volume = 0
                player.play()
                playIndex = Int(truncating: NSNumber(value: playIndex + 1))
                playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
            }
            
            
        } else {
            let player: AVAudioPlayer! = channels.object(at: playIndex) as? AVAudioPlayer
            
            if !player.isPlaying {
                player.currentTime = 0.0
                player.numberOfLoops = 0
                player.volume = 0
                player.play()
                playIndex = Int(truncating: NSNumber(value: playIndex + 1))
                playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
            } else {
                if player.volume < initialVolume.floatValue
                {
                    player.volume = player.volume + self.FADE_STEP
                }
            }
        }
    }
    
    func loop() {
        self.stop()
        if isRemote {
            let player: AVPlayer! = channels.object(at: Int(playIndex)) as? AVPlayer
            player.play()

        } else {
            let player: AVAudioPlayer! = channels.object(at: Int(playIndex)) as? AVAudioPlayer
            player.numberOfLoops = -1
            player.play()
        }
        playIndex = Int(truncating: NSNumber(value: playIndex + 1))
        playIndex = Int(truncating: NSNumber(value: playIndex % channels.count))
    }
    
    func unload() {
        self.stop()
        channels = NSMutableArray()
    }
    
    func setVolume(volume: NSNumber!) {
        for i in 0..<channels.count {
            if isRemote {
                let player: AVPlayer! = channels.object(at: i) as? AVPlayer
                player.volume = volume.floatValue
            } else {
                let player: AVAudioPlayer! = channels.object(at: i) as? AVAudioPlayer
                player.volume = volume.floatValue
            }
        }
    }
    
    public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        NSLog("playerDidFinish")
        self.owner.notifyListeners("complete", data: [
            "assetId": self.assetId
        ])
    }
    
    func playerDecodeError(player: AVAudioPlayer!, error: NSError!) {
        
    }
    
    @objc func playerDidFinishPlaying(note: NSNotification) {
        print("Play Finished")
        self.owner.notifyListeners("complete", data: [
            "assetId": self.assetId
        ])
    }
    
}
