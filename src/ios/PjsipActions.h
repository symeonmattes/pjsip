@interface PjsipActions : NSObject


-(void) initialise:(id) dlg;
-(void) initPjSip;
-(void) registerPBX:(NSString *)user Password:(NSString *) pass systemIpAddress:(NSString *) smIp proxyIpAddress:(NSString *) prxIp;
-(void) unregisterPBX;
-(void) acceptCall;
-(void) rejectCall;
-(void) holdCall:(BOOL) isActive;
-(void) makeCall:(NSString *) number;
-(void) playDTMF:(NSString *) number;

@end
