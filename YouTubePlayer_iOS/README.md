# SmartViewSDK Youtube Sample App #

##Prerequisite



###1. Library
Download library manually [SmartView SDK iOS framework](http://developer.samsung.com/tv/develop/tools/extension-libraries/smart-view-sdk-download):  iOS Package(Mobile)
	
	add smartview.framework
	
###2. Build Environment
1. This code is developed using Objective-C language.
2. Required Xcode version is 8.3	
	
###3. YouTube Data API  

1. [YouTube V3 Data AP](https://developers.google.com/youtube/v3/docs/)I
2. [Youtube V3 Data API "Search"](https://developers.google.com/youtube/v3/docs/search) 
3. [Creating a YouTube API Key](http://support.andromo.com/kb/common-questions/creating-a-youtube-api-key)

###4. allow arbitrary loads in info.plist

	<key>NSAppTransportSecurity</key>
	<dict>
	<key>NSAllowArbitraryLoads</key>
	<true/>
	</dict>

##Required Modification

	You must get Youtube API KEY to test this app.
	refer to below url
	Creating a YouTube API Key
	
	: https://support.google.com/cloud/answer/6158857?hl=en
	
###1. Youtube API KEY ?[MUST]

	ViewController.m
	
	NSString* youtubeAPI = @"https://www.googleapis.com/youtube/v3/search?"
	NSString* FEED_WHAT  = @"q=movie+trailers&part=snippet";
	NSString* FEED_MAX   = @"&maxResults=50";
	NSString* FEED_ORDER = @"&order=relevance";	
	NSString* APIKey     = @"&key=YOUR_API_KEY";
	

## Fix Application ID(TV App) and Channel ID
	
	###1. Application ID

	1. iOS (Mobile) 
	PhotoShareController.h
	@property (nonatomic, strong) NSString* appId;
	
	PhotoShareController.m
	-(instancetype)init
	{
		self.appId = @"0rLFmRVi9d.youtubetest";
	}
	
	2. Tizen WebApp
	Config.xml
	<tizen:application id="0rLFmRVi9d.youtubetest" package="0rLFmRVi9d" required_version="2.3"/> 
	
###2. Channel ID

	1. iOS (Mobile)  
	PhotoShareController.h
	@property (nonatomic, strong) NSString* channelId;
	
	PhotoShareController.m
	-(instancetype)init
	{
		self.channelId = @"com.samsung.msf.youtubetest";
	}
	
	self.app = [service createApplication:self.appId channelURI:self.channelId args:nil];

	2. Tizen WebApp 
	main.js
	var mChannel = 'com.samsung.msf.youtubetest';
	
	var channel = service.channel(mChannel);	
	
## Discover : Search devices around your mobile.
1. If you push search button in ActionBar, Start Search API.
2. You can configure the device list to override onServiceFound(), onServiceLost() listener.
3. Search OnStop() API is called when you click a item of devices list.	

		-(void) searchServices
		{
			[self.search start];
		}
		-(void)onServiceFound:(Service *)service
		{
			[self.services addObject: service];
		}
		
		-(void)onServiceLost:(Service *)service
		{
			for (Service *s in self.services) {
				if ([s.id isEqualToString:service.id]) {
					[self.services removeObject:s];
					break;
				}
			}
		}
	
		- (void)onStop {
			[self.services removeAllObjects];
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

## Create Channel and launch a TV app.

1. Get a Service instance from devices list. when select any TV , [app connect] is called to connect with selected TV. 
2. And create a application instance using tv application Id and Channel ID.
3. Now, you are ready to launch the TV app. Call [PhotoShareController sharedInstance] launchApp:videoId :title :thumbnailURL];

		PhotoShareController.m
		
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
	
##Disconnect
1. To disconnect with connected TV you need to call:

		[[PhotoShareController sharedInstance].app disconnect];	
	
##Event Handling
1. You can check to connect, disconnect event and to join other devices, also to catch a error.	
	
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

## Use YouTube API
1. You have to require a API key
2. And need to parse video data after query to youtube.

		ViewController.m	
		
		-(void)performGetRequest
		{
			youtubeAPI = [youtubeAPI stringByAppendingString:FEED_WHAT];
			youtubeAPI = [youtubeAPI stringByAppendingString:FEED_MAX];
			youtubeAPI = [youtubeAPI stringByAppendingString:FEED_ORDER];
			youtubeAPI = [youtubeAPI stringByAppendingString:APIKey];
		
			NSURL *url = [NSURL URLWithString:youTubeAPI];
			NSURLRequest *request = [NSURLRequest requestWithURL:url];
	    
			NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
	    
			[[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
	        
				NSLog(@"error:%@", error.localizedDescription);
	        
				NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
	        
				itemsArray = [jsonDict objectForKey:@"items"];
	        
				[[self collectionView] reloadData];
	        
			}] resume];
	    
		}
		
		-(void)downloadImageWithURL:(NSURL *)url completionBlock:(void (^)(bool succeeded, UIImage *image))completionBlock
		{
			NSURLRequest *request = [NSURLRequest requestWithURL:url];
			NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
	    
			[[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
	       
				if(error == nil)
				{
					UIImage *image = [[UIImage alloc] initWithData:data];
					completionBlock(YES, image);
				}
			}] resume];
	    
		}
	
## Communicate with mobile to TV
Now we need to parse the Jsondata and get the videoId, title and thumbnailURL of selected video and publish it to TV.

	- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath 
	{
		PhotoCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:reuseIdentifier forIndexPath:indexPath];
    
		NSDictionary *item = [itemsArray objectAtIndex:indexPath.item + 1];
		if([[[item objectForKey:@"id"] objectForKey:@"kind" ] isEqualToString:@"youtube#video"])
		{
			NSString* videoId = [[item objectForKey:@"id"] objectForKey:@"videoId"];
			NSLog(@"id is %@", videoId);
        
			NSString *title = [[item objectForKey:@"snippet"] objectForKey:@"title"];
			NSLog(@"title is %@", title);
        
			NSString *thumbnailURL = [[[[item objectForKey:@"snippet"] objectForKey:@"thumbnails"] objectForKey:@"medium"] objectForKey:@"url"];
			NSLog(@"url is %@", thumbnailURL);
        
			NSURL *url = [NSURL URLWithString:thumbnailURL];
        
			[self downloadImageWithURL:url completionBlock:^(bool succeeded, UIImage *image) {
				if(succeeded){
					cell.photoCellImage.image = image;
					cell.titleText.text = title;
				}
			}];
		}
		return cell;
	}
	