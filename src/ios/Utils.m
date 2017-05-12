#import "Utils.h"
#import <Foundation/Foundation.h>
#import <Cordova/CDVViewController.h>



@implementation Utils: NSObject


id delegate;

//CordovaWebView appView = null;
//CordovaInterface cordova = null;


-(void) initialise:(id) dlg{
    

    delegate = dlg;
    
}

-(NSString *) getMACAddress{
    
    
    return @"";
}


-(NSString *) concat:(NSString *)str1 With:(NSString *)str2 {
    
    NSString *str = [NSString stringWithFormat:@"%@%@",str1,str2];
    
    return str;
    
}

-(NSMutableArray *) listFiles:(NSString *) sourcePath{
    
    NSArray* dirs = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:sourcePath error:NULL];
    NSMutableArray *files = [[NSMutableArray alloc] init];
    [dirs enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        NSString *filename = (NSString *)obj;
//        NSString *extension = [[filename pathExtension] lowercaseString];
        
        [files addObject:[sourcePath stringByAppendingPathComponent:filename]];
    }];
    
    return files;
    
}

-(void) executeJavascript:(NSString *)str{
    
    
    [delegate runInBackground:^{
    
        [delegate evalJs:str];
//        [webview stringByEvaluatingJavaScriptFromString:str];
    
     
    }];
    
    
}

@end
