#import "CDVVibration.h"
#import <AudioToolbox/AudioToolbox.h>
#import <Cordova/CDVPlugin.h>

@interface CDVVibration ()

@property (nonatomic, strong) NSTimer *patternTimer;
@property (nonatomic, strong) NSArray *patternArray;
@property (nonatomic, assign) NSUInteger patternIndex;

@end

@implementation CDVVibration

- (void)vibrate:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSNumber *duration = [command.arguments objectAtIndex:0];

        if (duration && [duration integerValue] > 0) {
            AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        }
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)vibrateWithPattern:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSArray *pattern = [command.arguments objectAtIndex:0];

        if (pattern && [pattern count] > 0) {
            [self cancelVibration:nil];

            self.patternArray = pattern;
            self.patternIndex = 0;

            [self executePatternStep];
        }
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)executePatternStep {
    if (self.patternIndex >= [self.patternArray count]) {
        [self cancelVibration:nil];
        return;
    }

    NSNumber *duration = [self.patternArray objectAtIndex:self.patternIndex];
    NSTimeInterval timeInterval = [duration doubleValue] / 1000.0;

    if (self.patternIndex % 2 == 0 && [duration integerValue] > 0) {
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    }

    self.patternIndex++;

    self.patternTimer = [NSTimer scheduledTimerWithTimeInterval:timeInterval
                                                         target:self
                                                       selector:@selector(executePatternStep)
                                                       userInfo:nil
                                                        repeats:NO];
}

- (void)cancelVibration:(CDVInvokedUrlCommand*)command {
    if (self.patternTimer) {
        [self.patternTimer invalidate];
        self.patternTimer = nil;
    }

    self.patternArray = nil;
    self.patternIndex = 0;
    
    if (command) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)dealloc {
    [self cancelVibration:nil];
}

@end
