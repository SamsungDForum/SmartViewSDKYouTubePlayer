//
//  VideoInfo.m
//  SampleYouTubePlayer
//
//  Created by aseemkapoor on 22/04/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#import "VideoInfo.h"

@implementation VideoInfo

- (id)initWithVideoId:(NSString *)videoId titleName:(NSString *)title thumbnailurl:(NSString *)thumbnailURL
{
    self = [super init];
    if (self)
    {
        _videoId = videoId;
        _title = title;
        _thumbnailURL = thumbnailURL;
    }
    
    return self;
}

- (NSString *)getCacheDirectory
{
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = [paths objectAtIndex:0];
    cacheDirectory = [cacheDirectory stringByAppendingPathComponent:@"SampleYouTubeCaches"];
    return cacheDirectory;
}

- (void) resetCache
{
    [[NSFileManager defaultManager] removeItemAtPath:[self getCacheDirectory] error:nil];
}

- (UIImage *)getThumbnailImage
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    __block UIImage *image = nil;
    
    NSArray *strings = [self.thumbnailURL componentsSeparatedByString:@"/"];
    if ([strings count] <= 0)
    {
        return image;
    }
    
    NSString *key = [strings objectAtIndex:[strings count] - 2];
    NSString *filename = [[self getCacheDirectory] stringByAppendingPathComponent:key];
    
    
    if ([fileManager fileExistsAtPath:filename])
    {
        
        NSData *data = [NSData dataWithContentsOfFile:filename];
        image = [UIImage imageWithData:data];
        
    }
    
    return image;
}

- (void) storeThumbnailImage:(NSData*)data
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *strings = [self.thumbnailURL componentsSeparatedByString:@"/"];
    NSString *key = [strings objectAtIndex:[strings count] - 2];
    
    NSString *filename = [[self getCacheDirectory] stringByAppendingPathComponent:key];
    
    
    BOOL isDir = YES;
    if (![fileManager fileExistsAtPath:[self getCacheDirectory] isDirectory:&isDir])
    {
        [fileManager createDirectoryAtPath:[self getCacheDirectory] withIntermediateDirectories:NO attributes:nil error:nil];
    }
    
    NSError *error;
    @try
    {
        [data writeToFile:filename options:NSDataWritingAtomic error:&error];
    }
    @catch (NSException * e)
    {
        //TODO: error handling maybe
    }
}

@end
