#import <Cordova/CDV.h>
#import <RadarSDK/RadarSDK.h>
#import <CoreLocation/CoreLocation.h>

@interface CDVRadar : CDVPlugin <RadarDelegate, CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager* locationManager;
@property (nonatomic, strong) CDVInvokedUrlCommand *command;

@end
