#import "scAudioManager.h"


@implementation scAudioManager{
    NSMutableDictionary *connectedAudioDevices;

}

@synthesize audioPlayer = _audioPlayer;

+ (scAudioManager *) sharedInstance {
    
    static scAudioManager *_sharedInstance=nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

-(id) init{
    self = [super init];

    connectedAudioDevices = [[NSMutableDictionary alloc] init];
    [self scanConnectedDevices];
    
    
    return self;
}

-(void) setSpeakerMode:(BOOL) isActivated {
    
    
    BOOL success;NSError *error;
    
    
    AVAudioSession *session = [AVAudioSession sharedInstance];
    if (isActivated == YES){
        success = [session overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error];

    }else{
        success = [session overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&error];
    }
    if (!success) {
        NSLog(@"AVAudioSession error overrideOutputAudioPort:%@",error);
    }
    
    
}

-(void) muteMicrophone:(BOOL) isMuted{
    
    pj_status_t status;
    
    if (isMuted == YES){
        status =   pjsua_conf_adjust_rx_level (0,0); //microphone
        //        status = pjsua_conf_adjust_tx_level (0,0); //speaker
        
    }else{
        status =   pjsua_conf_adjust_rx_level (0,1);
        //        status = pjsua_conf_adjust_tx_level (0,1);
        
    }
    
    NSLog(@"The microphone status: %d",status);
    
}






-(void) startRingbackTone{
    
    NSString *soundFilePath = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] pathForResource:@"www/sounds/ringbacktone" ofType:@"mp3"]];
    [self playTone:soundFilePath Loop:YES];
    
    
}
-(void) startRingtone{

    NSString *soundFilePath = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] pathForResource:@"www/sounds/ringtone" ofType:@"mp3"]];
    [self playTone:soundFilePath Loop:YES];
    [self vibrate];
}

- (void) vibrate{
    
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    //[_log debug:@"Device has been vibrated." from:NSStringFromSelector(_cmd) className:NSStringFromClass([self class])];
};

-(void) stopRingtone{

    [_audioPlayer stop];
}
-(void) startBusyTone{}
-(void) playDTMF:(NSString *) num{
    
    
    NSString *dtmf;
    if ([num isEqualToString:@"#"]){
        dtmf = @"www/sounds/dtmfhash";
    }else if ([num isEqualToString:@"*"]){
        dtmf = @"www/sounds/dtmfstar";
    }else{
        dtmf = [NSString stringWithFormat:@"www/sounds/dtmf%@",num];
    }
    
    
    NSString *soundFilePath = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] pathForResource:dtmf ofType:@"mp3"]];
    [self playTone:soundFilePath Loop:NO];
    
}
-(void) stopTone{

    [_audioPlayer stop];
}
-(void) playTone:(NSString *) soundFilePath Loop:(BOOL) loop{
    
    
    NSLog(@"Sound File Path: %@",soundFilePath);
    
    [_audioPlayer stop];
    //    Utils *utls = [[Utils alloc] init];
    //    NSLog(@"Files:%@",[utls listFiles:[NSString stringWithFormat:@"%@",[[NSBundle mainBundle] resourcePath]]]);
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:soundFilePath]){
        
        NSURL *soundFileURL = [NSURL fileURLWithPath:soundFilePath];
        
        NSError *error = nil;
        _audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:soundFileURL error:&error];
        
        if (error){
            NSLog(@"Error creating the audio player: %@",error);
        }else{
            if (loop == YES)
                _audioPlayer.numberOfLoops = -1; //Infinite
            
            _audioPlayer.volume=[[AVAudioSession sharedInstance] outputVolume];
            _audioPlayer.delegate = self;
            
            [_audioPlayer prepareToPlay];
            [_audioPlayer play];
        }
        
    }else{
        NSLog(@"No sound will be played. The file doesn't exist.");
    }
}


-(void) scanConnectedDevices{
    
    
    
    [connectedAudioDevices removeAllObjects];
    
    
    NSArray *inputs = [[AVAudioSession sharedInstance] availableInputs];
    NSMutableArray *types=[[NSMutableArray alloc] init];
    for (AVAudioSessionPortDescription *port in inputs)
    {
        connectedAudioDevices[port.portType]=port;
        [types addObject:[NSString stringWithFormat:@"%@:%@",port.portType,port.portName]];
    }
    
    NSLog(@"%lu devices have been found:%@.",(unsigned long)[connectedAudioDevices count],types);
    
//    [_log info:[NSString stringWithFormat:@"%lu devices have been found:%@.",(unsigned long)[connectedAudioDevices count],types] from:NSStringFromSelector(_cmd) className:NSStringFromClass([self class])];
    
    
}


AVAudioRecorder *recorder;
NSString * recorderFilePath;
- (void) startRecording{
    
    
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    NSError *err = nil;
    [audioSession setCategory :AVAudioSessionCategoryPlayAndRecord error:&err];
    if(err){
        NSLog(@"audioSession: %@ %ld %@", [err domain], (long)[err code], [[err userInfo] description]);
        return;
    }
    [audioSession setActive:YES error:&err];
    err = nil;
    if(err){
        NSLog(@"audioSession: %@ %ld %@", [err domain], (long)[err code], [[err userInfo] description]);
        return;
    }
    
    NSMutableDictionary *recordSetting = [[NSMutableDictionary alloc] init];
    
    [recordSetting setValue :[NSNumber numberWithInt:kAudioFormatLinearPCM] forKey:AVFormatIDKey];
    [recordSetting setValue:[NSNumber numberWithFloat:44100.0] forKey:AVSampleRateKey];
    [recordSetting setValue:[NSNumber numberWithInt: 2] forKey:AVNumberOfChannelsKey];
    
    [recordSetting setValue :[NSNumber numberWithInt:16] forKey:AVLinearPCMBitDepthKey];
    [recordSetting setValue :[NSNumber numberWithBool:NO] forKey:AVLinearPCMIsBigEndianKey];
    [recordSetting setValue :[NSNumber numberWithBool:NO] forKey:AVLinearPCMIsFloatKey];
    
    
    
    // Create a new dated file
    NSDate *now = [NSDate dateWithTimeIntervalSinceNow:0];
    NSString *caldate = [now description];
    NSString *dcfolder=[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"];
    recorderFilePath = [NSString stringWithFormat:@"%@/%@.caf", dcfolder, caldate];
    
    NSURL *url = [NSURL fileURLWithPath:recorderFilePath];
    err = nil;
    recorder = [[ AVAudioRecorder alloc] initWithURL:url settings:recordSetting error:&err];
    if(!recorder){
        NSLog(@"recorder: %@ %ld %@", [err domain], (long)[err code], [[err userInfo] description]);
    }
    
    [recorder prepareToRecord];
    recorder.meteringEnabled = YES;
    
    [recorder record];
    
}

-(void) checkAudio{
    

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
       
        [self startRecording];
        
        [self setSpeakerMode:YES];
        NSString *soundFilePath = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] pathForResource:@"www/sounds/ringbacktone" ofType:@"mp3"]];
        [self playTone:soundFilePath Loop:NO];
        
        
        
    });
    
    
}

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player successfully:(BOOL)flag {
    // set finish image
    NSLog(@"Audio did finish.");
    
    
    if (recorder){
        [recorder stop];
        recorder=NULL;
        [self setSpeakerMode:YES];
        [self playTone:recorderFilePath Loop:NO];
        
    }
    
}

@end
