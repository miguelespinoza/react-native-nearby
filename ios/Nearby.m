//
//  Nearby.m
//  infect
//
//  Created by Miguel  Espinoza on 6/24/17.
//  Copyright © 2017 650 Industries, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <GNSMessages.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import "Nearby.h"

@implementation Nearby

GNSMessageManager *messageManager;
id<GNSPublication> publication;
id<GNSSubscription> subscription;

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(init:(NSString *)message) {
    RCTLog(@"ios publish: %@", message);
    
    messageManager = [[GNSMessageManager alloc] initWithAPIKey:@"AIzaSyDIqyxKzvQwZhoZZJoOeh5Hr3sIJvqNYHo"];
    
    publication = [messageManager publicationWithMessage:[GNSMessage messageWithContent:[message dataUsingEncoding:NSUTF8StringEncoding]]];
    
    subscription = [
                    messageManager subscriptionWithMessageFoundHandler:^(GNSMessage *message) {
                        RCTLog(@"ios subscribe: %@", message);
                        NSString *messageStr = [[NSString alloc] initWithData:message.content encoding:NSUTF8StringEncoding];
                        [self.bridge.eventDispatcher sendAppEventWithName:@"nearbySubscribe"
                                                                     body:@{@"method": @"onFound", @"message": [NSString stringWithFormat:@"%@", messageStr]}];
                    }
                    messageLostHandler:^(GNSMessage *message) {
                        RCTLog(@"ios subscribe lost: %@", message);
                        NSString *messageStr = [[NSString alloc] initWithData:message.content encoding:NSUTF8StringEncoding];
                        [self.bridge.eventDispatcher sendAppEventWithName:@"nearbySubscribe"
                                                                     body:@{@"method": @"onLost", @"message": [NSString stringWithFormat:@"%@", messageStr]}];
                    }];
    
    //	[bridge.eventDispatcher sendDeviceEventWithName:
    //	 @"nearbySubscribe" body:@{@"method": @"onPublish", @"message": @"Hey from ios"}];
    [self.bridge.eventDispatcher sendAppEventWithName:@"nearbySubscribe"
                                                 body:@{@"method": @"onPublish", @"message": @"Hey from ios"}];
}

@end
