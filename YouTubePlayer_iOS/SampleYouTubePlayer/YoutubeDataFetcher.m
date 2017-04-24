//
//  YoutubeDataFetcher.m
//  SampleYouTubePlayer
//
//  Created by aseemkapoor on 26/04/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#import "YoutubeDataFetcher.h"

static NSString* youtubeAPI = @"https://www.googleapis.com/youtube/v3/search?";
static NSString* FEED_WHAT = @"q=movie+trailers&part=snippet";
static NSString* FEED_MAX = @"&maxResults=50";
static NSString* FEED_ORDER = @"&order=relevance";
static NSString* APIKey = @"&key=YOUR_API_KEY";

NSString *const kListPrepared = @"ListPrepared";

@implementation YoutubeDataFetcher

+ (YoutubeDataFetcher *)getSharedInstance
{
    static YoutubeDataFetcher *sharingInstance;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        sharingInstance = [[YoutubeDataFetcher alloc] init];
    });
    return sharingInstance;
}

- (instancetype)init
{
    self = [super init];
    
    if (self != nil)
    {
        self.videoArray = [[NSMutableArray alloc]init];
    }
    
    return self;
}

-(void)performGetRequest
{
    youtubeAPI = [youtubeAPI stringByAppendingString:FEED_WHAT];
    youtubeAPI = [youtubeAPI stringByAppendingString:FEED_MAX];
    youtubeAPI = [youtubeAPI stringByAppendingString:FEED_ORDER];
    youtubeAPI = [youtubeAPI stringByAppendingString:APIKey];
    
    NSLog(@"youtube api is %@", youtubeAPI);
    
    NSURL *url = [NSURL URLWithString:youtubeAPI];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    
    [[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        
        
        NSLog(@"error:%@", error.localizedDescription);
        
        NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
        
        itemsArray = [jsonDict objectForKey:@"items"];
        
        if(error == nil)
        {
            [self parseArray];
        }
        
    }] resume];
    
}

- (void) parseArray
{
    NSInteger count = 1;
    for (NSDictionary *dict in itemsArray)
    {
        if([[[dict objectForKey:@"id"] objectForKey:@"kind" ] isEqualToString:@"youtube#video"])
        {
            NSString* videoId = [[dict objectForKey:@"id"] objectForKey:@"videoId"];
            NSLog(@"id is %@", videoId);
    
            NSString *title = [[dict objectForKey:@"snippet"] objectForKey:@"title"];
            NSLog(@"title is %@", title);
            
            NSString *thumbnailURL = [[[[dict objectForKey:@"snippet"] objectForKey:@"thumbnails"] objectForKey:@"default"] objectForKey:@"url"];
            NSLog(@"url is %@", thumbnailURL);
            
            VideoInfo *videoData = [[VideoInfo alloc] initWithVideoId:videoId titleName:title thumbnailurl:thumbnailURL];
            [self.videoArray addObject:videoData];
            
            
            count ++;
        }
        
//        if (count % 5 == 0)
//        {
//            [[NSNotificationCenter defaultCenter] postNotificationName:kListPrepared object:self];
////            break;
//
//        }
    }
    
    [[NSNotificationCenter defaultCenter] postNotificationName:kListPrepared object:self];
}

@end
