@interface Utils : NSObject



-(void) initialise:(id) dlg;

-(NSString *) concat:(NSString *)str1 With:(NSString *)str2;
-(NSString *) getMACAddress;
-(void) executeJavascript:(NSString *)str;

-(NSMutableArray *) listFiles:(NSString *) sourcePath;



@end
