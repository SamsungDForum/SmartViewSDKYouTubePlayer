//
//  Requester.m
//  SampleYouTubePlayer
//
//  Created by CHIRAG BAHETI on 04/03/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Requester.h"

@implementation Requester

-(void) getRequest
{
    NSURL* url = [NSURL URLWithString:@"http://www.google.com"];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:60.0];
    
    [request setHTTPMethod:@"GET"];
    
    
}

@end