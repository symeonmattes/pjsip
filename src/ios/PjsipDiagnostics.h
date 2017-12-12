@interface PjsipDiagnostics : NSObject

+ (PjsipDiagnostics *) sharedInstance;
-(NSString *) checkArchitecture;
-(NSString *) getIPAddress;
-(NSString *) getWifiSSID;

@end
