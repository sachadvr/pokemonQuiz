#import <Cordova/CDVPlugin.h>

@interface CDVVibration : CDVPlugin

- (void)vibrate:(CDVInvokedUrlCommand*)command;
- (void)vibrateWithPattern:(CDVInvokedUrlCommand*)command;
- (void)cancelVibration:(CDVInvokedUrlCommand*)command;

@end
