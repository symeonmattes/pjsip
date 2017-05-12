#import "PJSIP.h"
#import "PjsipActions.h"
#import "scAudioManager.h"
#import <Cordova/CDVPlugin.h>
#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>



//#include "pjsua_app.h"
//#include "pjsua_app_config.h"

@implementation PJSIP

PjsipActions *pjactions = NULL;
scAudioManager *scaudio = NULL;


- (void)pluginInitialize{
    
    pjactions = [[PjsipActions alloc] init];
    [pjactions initialise:self.commandDelegate];

    scaudio = [scAudioManager sharedInstance];
    
    
}

- (void) issupported:(CDVInvokedUrlCommand*)command{
  
  CDVPluginResult* pluginResult = nil;

  
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"PJSIP is supported"];


  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  
}
    
- (void) connect:(CDVInvokedUrlCommand*)command{
  
  CDVPluginResult* pluginResult = nil;
  
  NSString* msg = @"";
  
  NSString* user = [command.arguments objectAtIndex:0];
  NSString* pass = [command.arguments objectAtIndex:1];
  NSString* systemIPAddress = [command.arguments objectAtIndex:2];
  NSString* proxyIPAddress = [command.arguments objectAtIndex:3];

  if (!(user != nil && [user length] > 0)) {
      msg = [msg stringByAppendingString:@"No user has been provided."];

  }
  
  if (!(pass != nil && [pass length] > 0)) {
      msg = [msg stringByAppendingString:@"No password has been provided."];
  }
   
  if (!(systemIPAddress != nil && [systemIPAddress length] > 0)) {
      msg = [msg stringByAppendingString:@"No system IP has been provided."];
  }
  
  
  
  if ([msg length]==0) {
      
      [pjactions registerPBX:user Password:pass systemIpAddress:systemIPAddress proxyIpAddress:proxyIPAddress];
      
      
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"All parameters are correct"];
  } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
  }
  

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

  
}
- (void) makecall:(CDVInvokedUrlCommand*)command{
  
  CDVPluginResult* pluginResult = nil;
  NSString* num = [command.arguments objectAtIndex:0];
    

  if (num != nil && [num length] > 0) {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Call to user '%1$@' will be initiated", num]];
  
      
      
      //http://techqa.info/programming/question/40989940/ios10-iphone5s-voip-siphon-pjsip2.5.5-error-opening-sound-device
      NSError *sessionError = nil;
      [[AVAudioSession sharedInstance] setDelegate:self];
      [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:&sessionError];

      [scaudio startRingbackTone];
      [pjactions makeCall:num];
      
//      [self.commandDelegate runInBackground:^{
//
//      }];
      

      

  
  }else{
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No user has been provided"];
  }

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void) endcall:(CDVInvokedUrlCommand*)command{
  CDVPluginResult* pluginResult = nil;

    
    [scaudio stopTone];
    [pjactions rejectCall];
    
  
    
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"The call has been ended."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];  
  
  
}
- (void) disconnect:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult = nil;
    
    [pjactions unregisterPBX];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"The user will be disconnected."];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) isconnected:(CDVInvokedUrlCommand*)command{
  CDVPluginResult* pluginResult = nil;

  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"User is connected."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) activatespeaker:(CDVInvokedUrlCommand*)command{
  
    CDVPluginResult* pluginResult = nil;
    NSString* isActivated = [command.arguments objectAtIndex:0];
    
    [scaudio setSpeakerMode:[isActivated boolValue]];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Speaker has been %@.",[isActivated boolValue]?@"enabled":@"disabled" ]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  
  
}
- (void) dtmfcall:(CDVInvokedUrlCommand*)command{
  CDVPluginResult* pluginResult = nil;
  NSString* dtmfNumber = [command.arguments objectAtIndex:0];

    [scaudio playDTMF:dtmfNumber];
    [pjactions playDTMF:dtmfNumber];
    
    
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Mute has been ."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];  
  
}
- (void) mutemicrophone:(CDVInvokedUrlCommand*)command{
  
    CDVPluginResult* pluginResult = nil;
    NSString* isMuted = [command.arguments objectAtIndex:0];
    
    [scaudio muteMicrophone:[isMuted boolValue]];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Mute has been %@.",[isMuted boolValue]?@"enabled":@"disabled" ]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];  
  
}
- (void) holdcall:(CDVInvokedUrlCommand*)command{
  CDVPluginResult* pluginResult = nil;
  NSString* isOnHold = [command.arguments objectAtIndex:0];

    [pjactions holdCall:[isOnHold boolValue]];
    
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Mute has been ."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];  
  
}
- (void) declinecall:(CDVInvokedUrlCommand*)command{
  
  CDVPluginResult* pluginResult = nil;
    
    [scaudio stopRingtone];
    [pjactions rejectCall];

  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Call has been declined."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];    
  
}
- (void) acceptcall:(CDVInvokedUrlCommand*)command{
  
    CDVPluginResult* pluginResult = nil;

    
    [scaudio stopRingtone];
    [pjactions acceptCall];

    
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Call has been accepted."];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];     
}



@end
