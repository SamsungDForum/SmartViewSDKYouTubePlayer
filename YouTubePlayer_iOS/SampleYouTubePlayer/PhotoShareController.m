/*
 
 Copyright (c) 2014 Samsung Electronics
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
 */

#import "PhotoShareController.h"

@import AssetsLibrary;

@implementation PhotoShareController

+ (instancetype)sharedInstance
{
    static PhotoShareController *controller;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        controller = [self new];
    });
    return controller;
}

-(instancetype)init
{
    self = [super init];
    self.appURL = @"http://prod-multiscreen-examples.s3-website-us-west-1.amazonaws.com/examples/photoshare/tv/";
    self.appId = @"0rLFmRVi9d.youtubetest";
    self.channelId = @"com.samsung.msf.youtubetest";
    self.services = [NSMutableArray arrayWithCapacity:0];
    self.search = [Service search];
    self.search.delegate = self;
    return self;
}

-(void) searchServices
{
    [self.search start];
    [self updateCastStatus];
}


-(void) connect:(Service *) service
{
    self.app = [service createApplication:self.appId channelURI:self.channelId args:nil];
    self.app.delegate = self;
    self.app.connectionTimeout = 5;
    self.isConnecting = YES;
    [self updateCastStatus];
    [self.app connect:@{@"name": [UIDevice currentDevice].name}];
}

-(void)launchApp:(NSString*)videoId :(NSString*)videoName :(NSString*) videoThumbnail
{
    NSDictionary* dict = @{
                           @"videoId":videoId,
                           @"videoName":videoName,
                           @"videoThumnail":videoThumbnail
                           };
    NSError* error;
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    
    if(!jsonData)
    {
        NSLog(@"error is %@", error);
    }
    else
    {
        NSString* jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        
        [self.app publishWithEvent:@"play" message:jsonString];
    }
    
}


-(CastStatus) getCastStatus
{
    CastStatus castStatus = notReady;
    if (self.app != nil && self.app.isConnected)
    {
        castStatus = connected;
    }
    else if (self.isConnecting)
    {
        castStatus = connecting;
    }
    else if (self.services.count > 0)
    {
        castStatus = readyToConnect;
    }
    return castStatus;
}

-(void) castImage:(NSURL*) imageURL
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        ALAssetsLibrary * assetLib = [[ALAssetsLibrary alloc] init];
        
        [assetLib assetForURL:imageURL resultBlock:^(ALAsset *asset) {
            if (asset != nil)
            {
                ALAssetRepresentation *representation = [asset defaultRepresentation];
                
                UIImage * image = [UIImage imageWithCGImage:[representation fullResolutionImage]];
                NSData *imageData = UIImageJPEGRepresentation(image, 0.6);
                [[PhotoShareController sharedInstance].app publishWithEvent:@"showPhoto" message:nil data:imageData target:@"all"];
            }
        } failureBlock:^(NSError *error) {
            NSLog(@"Error");
        }];
    });
}

// MARK: Private Methods
-(void) updateCastStatus
{
    // Update the cast button status: Since there may be many cast buttons and
    // the PhotoShareController does not need to be coupled to the view controllers
    // the use of Notifications seems appropriate.
    CastStatus castStatus = [self getCastStatus];
    
    NSString * castStatusString = @"notReady";
    if (castStatus == notReady)
        castStatusString = @"notReady";
    else if (castStatus == readyToConnect)
        castStatusString = @"readyToConnect";
    else if (castStatus == connecting)
        castStatusString = @"connecting";
    else if (castStatus == connected)
        castStatusString = @"connected";
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"CastStatusDidChange" object:self userInfo:@{@"status": castStatusString}];
    
}

// MARK: - ChannelDelegate -

- (void)onConnect:(ChannelClient *)client error:(NSError *)error
{
    if (error != nil)
    {
        [self.search start];
        NSLog(@"onConnect failed - %@",[error localizedDescription]);
    }
    self.isConnecting = NO;
    [self updateCastStatus];
}

- (void)onDisconnect:(ChannelClient *)client error:(NSError *)error
{
    self.app = nil;
    self.isConnecting = NO;
    [self updateCastStatus];
}

// MARK: - ServiceDiscoveryDelegate Methods -

// These two delegate method will help us know when to change the cast button status

-(void)onServiceFound:(Service *)service
{
    [self.services addObject: service];
    [self updateCastStatus];
}
-(void)onServiceLost:(Service *)service
{
    for (Service *s in self.services) {
        if ([s.id isEqualToString:service.id]) {
            [self.services removeObject:s];
            break;
        }
    }
    [self updateCastStatus];
}

- (void)onStop {
    [self.services removeAllObjects];
}

@end
