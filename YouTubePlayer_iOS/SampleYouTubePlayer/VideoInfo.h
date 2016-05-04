//
//  VideoInfo.h
//  SampleYouTubePlayer
//
//  Created by aseemkapoor on 22/04/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface VideoInfo : NSObject

@property (nonatomic, readwrite) NSString* videoId;
@property (nonatomic, readwrite) NSString *title;
@property (nonatomic, readwrite) NSString *thumbnailURL;


- (id)initWithVideoId:(NSString *)videoId titleName:(NSString *)title thumbnailurl:(NSString *)thumbnailURL;
- (UIImage *)getThumbnailImage;
- (void) storeThumbnailImage:(NSData*)data;
- (void) resetCache;

@end
