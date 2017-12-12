@interface PjsipActions : NSObject


-(void) initialise:(id) dlg;
-(void) registerPBX:(NSString *)user Password:(NSString *) pass systemIpAddress:(NSString *) smIp proxyIpAddress:(NSString *) prxIp;
-(void) unregisterPBX;
-(void) acceptCall;
-(void) rejectCall;
-(void) holdCall:(BOOL) isActive;
-(void) makeCall:(NSString *) number;
-(void) playDTMF:(NSString *) number;
-(BOOL) isConnected;

@end
