//
//  scAudioManager.h
//  infinity4u
//
//  Created by Navarino on 10/5/16.
//
//

#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>
#import "Utils.h"
#import <Foundation/Foundation.h>


#import <pjlib.h>
#import <pjsua.h>
#import <pj/log.h>


@interface scAudioManager : NSObject <AVAudioPlayerDelegate>


@property (strong, nonatomic) AVAudioPlayer *audioPlayer;

+ (scAudioManager *) sharedInstance;

typedef enum AudioDevice:NSUInteger{
    NONE = 0,
    SPEAKER = 1,
    AUTOSELECT = 2
} AudioDevice;

-(void) setSpeakerMode:(BOOL) isActivated;
-(void) muteMicrophone:(BOOL) isMuted;
-(void) startRingbackTone;
-(void) startRingtone;
-(void) stopRingtone;
-(void) startBusyTone;
-(void) playDTMF:(NSString *) num;
-(void) stopTone;


@end
