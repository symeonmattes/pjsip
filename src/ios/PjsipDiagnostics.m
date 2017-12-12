#import "PjsipDiagnostics.h"
#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import <sys/utsname.h>
#include <ifaddrs.h>
#include <arpa/inet.h>


@implementation PjsipDiagnostics:NSObject

+ (PjsipDiagnostics *) sharedInstance {
    
    static PjsipDiagnostics *_sharedInstance=nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

-(NSString *) checkArchitecture{
    
    struct utsname systemInfo;
    uname(&systemInfo);
    
    NSString *model = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    NSString *cpu = model;
    
    
    //Simultor
    /*
     @"i386"      on 32-bit Simulator
     @"x86_64"    on 64-bit Simulator
     
     //iPhone
     @"iPhone1,1" on iPhone
     @"iPhone1,2" on iPhone 3G
     @"iPhone2,1" on iPhone 3GS
     @"iPhone3,1" on iPhone 4 (GSM)
     @"iPhone3,3" on iPhone 4 (CDMA/Verizon/Sprint)
     @"iPhone4,1" on iPhone 4S
     @"iPhone5,1" on iPhone 5 (model A1428, AT&T/Canada)
     @"iPhone5,2" on iPhone 5 (model A1429, everything else)
     @"iPhone5,3" on iPhone 5c (model A1456, A1532 | GSM)
     @"iPhone5,4" on iPhone 5c (model A1507, A1516, A1526 (China), A1529 | Global)
     @"iPhone6,1" on iPhone 5s (model A1433, A1533 | GSM)
     @"iPhone6,2" on iPhone 5s (model A1457, A1518, A1528 (China), A1530 | Global)
     @"iPhone7,1" on iPhone 6 Plus
     @"iPhone7,2" on iPhone 6
     @"iPhone8,1" on iPhone 6S
     @"iPhone8,2" on iPhone 6S Plus
     @"iPhone8,4" on iPhone SE
     @"iPhone9,1" on iPhone 7 (CDMA)
     @"iPhone9,3" on iPhone 7 (GSM)
     @"iPhone9,2" on iPhone 7 Plus (CDMA)
     @"iPhone9,4" on iPhone 7 Plus (GSM)
     
     //iPad 1
     @"iPad1,1" on iPad - Wifi (model A1219)
     @"iPad1,1" on iPad - Wifi + Cellular (model A1337)
     
     //iPad 2
     @"iPad2,1" - Wifi (model A1395)
     @"iPad2,2" - GSM (model A1396)
     @"iPad2,3" - 3G (model A1397)
     @"iPad2,4" - Wifi (model A1395)
     
     // iPad Mini
     @"iPad2,5" - Wifi (model A1432)
     @"iPad2,6" - Wifi + Cellular (model  A1454)
     @"iPad2,7" - Wifi + Cellular (model  A1455)
     
     //iPad 3
     @"iPad3,1" - Wifi (model A1416)
     @"iPad3,2" - Wifi + Cellular (model  A1403)
     @"iPad3,3" - Wifi + Cellular (model  A1430)
     
     //iPad 4
     @"iPad3,4" - Wifi (model A1458)
     @"iPad3,5" - Wifi + Cellular (model  A1459)
     @"iPad3,6" - Wifi + Cellular (model  A1460)
     
     //iPad AIR
     @"iPad4,1" - Wifi (model A1474)
     @"iPad4,2" - Wifi + Cellular (model A1475)
     @"iPad4,3" - Wifi + Cellular (model A1476)
     
     // iPad Mini 2
     @"iPad4,4" - Wifi (model A1489)
     @"iPad4,5" - Wifi + Cellular (model A1490)
     @"iPad4,6" - Wifi + Cellular (model A1491)
     
     // iPad Mini 3
     @"iPad4,7" - Wifi (model A1599)
     @"iPad4,8" - Wifi + Cellular (model A1600)
     @"iPad4,9" - Wifi + Cellular (model A1601)
     
     // iPad Mini 4
     @"iPad5,1" - Wifi (model A1538)
     @"iPad5,2" - Wifi + Cellular (model A1550)
     
     //iPad AIR 2
     @"iPad5,3" - Wifi (model A1566)
     @"iPad5,4" - Wifi + Cellular (model A1567)
     
     // iPad PRO 12.9"
     @"iPad6,3" - Wifi (model A1673)
     @"iPad6,4" - Wifi + Cellular (model A1674)
     @"iPad6,4" - Wifi + Cellular (model A1675)
     
     //iPad PRO 9.7"
     @"iPad6,7" - Wifi (model A1584)
     @"iPad6,8" - Wifi + Cellular (model A1652)
     
     //iPod Touch
     @"iPod1,1"   on iPod Touch
     @"iPod2,1"   on iPod Touch Second Generation
     @"iPod3,1"   on iPod Touch Third Generation
     @"iPod4,1"   on iPod Touch Fourth Generation
     @"iPod7,1"   on iPod Touch 6th Generation
     */
    
    
    
    //https://stackoverflow.com/questions/11197509/ios-how-to-get-device-make-and-model
    //https://medium.com/@FredericJacobs/why-i-m-not-enabling-bitcode-f35cd8fbfcc5
    if ([model isEqualToString:@"iPhone1,1"]  || [model isEqualToString:@"iPhone1,2"] ||
        [model isEqualToString:@"iPod1,1"]  || [model isEqualToString:@"iPod2,1"] ){
        cpu=[NSString stringWithFormat:@"%@ (%@)",@"ARMv6",model];
        
    }else if([model isEqualToString:@"iPhone2,1"] || [model isEqualToString:@"iPhone3,1"] ||
             [model isEqualToString:@"iPhone3,3"] || [model isEqualToString:@"iPhone4,1"] ||
             [model isEqualToString:@"iPad1,1"] || [model isEqualToString:@"iPad1,2"] ||
             [model isEqualToString:@"iPad2,1"] || [model isEqualToString:@"iPad2,2"] ||
             [model isEqualToString:@"iPad2,3"] || [model isEqualToString:@"iPad2,4"] ||
             [model isEqualToString:@"iPad2,5"] || [model isEqualToString:@"iPad2,6"] ||
             [model isEqualToString:@"iPad2,7"] || [model isEqualToString:@"iPad3,1"] ||
             [model isEqualToString:@"iPad3,2"] || [model isEqualToString:@"iPad3,3"] ||
             [model isEqualToString:@"iPod3,1"] || [model isEqualToString:@"iPod4,1"] ||
             [model isEqualToString:@"iPod7,1"]){
        cpu=[NSString stringWithFormat:@"%@ (%@)",@"ARMv7",model];
    }else if([model isEqualToString:@"iPhone5,1"] || [model isEqualToString:@"iPhone5,2"] ||
             [model isEqualToString:@"iPhone5,3"] || [model isEqualToString:@"iPhone5,4"] ||
             [model isEqualToString:@"iPad3,4"] || [model isEqualToString:@"iPad3,5"] ||
             [model isEqualToString:@"iPad3,6"]){
        cpu=[NSString stringWithFormat:@"%@ (%@)",@"ARMv7s",model];
    }else if( [model isEqualToString:@"iPhone6,1"] || [model isEqualToString:@"iPhone6,2"] ||
             [model isEqualToString:@"iPhone7,1"] || [model isEqualToString:@"iPhone7,2"] ||
             [model isEqualToString:@"iPad4,1"] || [model isEqualToString:@"iPad4,2"] ||
             [model isEqualToString:@"iPad4,3"] || [model isEqualToString:@"iPad4,4"] ||
             [model isEqualToString:@"iPad4,5"] || [model isEqualToString:@"iPad4,6"] ||
             [model isEqualToString:@"iPad4,7"] || [model isEqualToString:@"iPad4,8"] ||
             [model isEqualToString:@"iPad4,9"] || [model isEqualToString:@"iPhone8,1"] ||
             [model isEqualToString:@"iPhone8,2"] || [model isEqualToString:@"iPhone8,4"] ||
             [model isEqualToString:@"iPhone9,1"] || [model isEqualToString:@"iPhone9,2"] ||
             [model isEqualToString:@"iPhone9,3"] || [model isEqualToString:@"iPhone9,4"]){
        cpu=[NSString stringWithFormat:@"%@ (%@)",@"ARM64",model];
    } else if ([model isEqualToString:@"iPad5,1"] || [model isEqualToString:@"iPad5,2"] ||
               [model isEqualToString:@"iPad5,3"] || [model isEqualToString:@"iPad5,4"] ||
               [model isEqualToString:@"iPad6,3"] || [model isEqualToString:@"iPad6,4"] ||
               [model isEqualToString:@"iPad6,7"] || [model isEqualToString:@"iPad6,8"]){
        cpu=[NSString stringWithFormat:@"%@ (%@)",@"ARMv8",model];
    }

    
    return cpu;
    
}

- (NSString *)getIPAddress {
    
    NSString *address = @"error";
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    if (success == 0) {
        // Loop through linked list of interfaces
        temp_addr = interfaces;
        while(temp_addr != NULL) {
            if(temp_addr->ifa_addr->sa_family == AF_INET) {
                // Check if interface is en0 which is the wifi connection on the iPhone
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"]) {
                    // Get NSString from C String
                    address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)];
                    
                }
                
            }
            
            temp_addr = temp_addr->ifa_next;
        }
    }
    // Free memory
    freeifaddrs(interfaces);
    return address;
    
}

- (NSString *)getWifiSSID {
    
    NSString *ssid = @"error";

    NSArray *ifs = (__bridge_transfer id)CNCopySupportedInterfaces();
    for (NSString *ifnam in ifs) {
        NSDictionary *info = (__bridge_transfer id)CNCopyCurrentNetworkInfo((__bridge CFStringRef)ifnam);
        if (info[@"SSID"]) {
            ssid = info[@"SSID"];
        }
    }
    
    
    return ssid;
    
}

@end
