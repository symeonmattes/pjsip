#import <Cordova/CDVPlugin.h>

@interface PJSIP : CDVPlugin


- (void)pluginInitialize;
- (void) issupported:(CDVInvokedUrlCommand*)command;
- (void) connect:(CDVInvokedUrlCommand*)command;
- (void) makecall:(CDVInvokedUrlCommand*)command;
- (void) endcall:(CDVInvokedUrlCommand*)command;
- (void) disconnect:(CDVInvokedUrlCommand*)command;
- (void) isconnected:(CDVInvokedUrlCommand*)command;
- (void) activatespeaker:(CDVInvokedUrlCommand*)command;
- (void) dtmfcall:(CDVInvokedUrlCommand*)command;
- (void) mutemicrophone:(CDVInvokedUrlCommand*)command;
- (void) holdcall:(CDVInvokedUrlCommand*)command;
- (void) declinecall:(CDVInvokedUrlCommand*)command;
- (void) acceptcall:(CDVInvokedUrlCommand*)command;

@end
