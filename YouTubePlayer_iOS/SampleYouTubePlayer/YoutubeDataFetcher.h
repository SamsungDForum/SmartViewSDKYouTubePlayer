//
//  YoutubeDataFetcher.h
//  SampleYouTubePlayer
//
//  Created by aseemkapoor on 26/04/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "VideoInfo.h"


extern NSString *const kListPrepared;

@interface YoutubeDataFetcher : NSObject
{
    NSArray *itemsArray;
}

@property (nonatomic) NSMutableArray *videoArray;

+ (YoutubeDataFetcher *)getSharedInstance;
-(void)performGetRequest;

@end
