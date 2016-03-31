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

#import "ViewController.h"
#import "PhotoCell.h"
#import "PhotoShareController.h"
#import "DeviceListViewController.h"
#import "TerminateAppViewController.h"

@interface ViewController ()

@end

@implementation ViewController

static NSString * const reuseIdentifier = @"photoCell";
static NSArray *itemsArray;

//static NSString* youtubeAPI = @"https://www.googleapis.com/youtube/v3/search?q=movie+trailers&part=snippet&maxResults=50&order=relevance";

static NSString* youtubeAPI = @"https://www.googleapis.com/youtube/v3/search?";
static NSString* FEED_WHAT = @"q=movie+trailers&part=snippet";
static NSString* FEED_MAX = @"&maxResults=50";
static NSString* FEED_ORDER = @"&order=relevance";
static NSString* APIKey = @"&key=YOUR_API_KEY";

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    self.collectionView.allowsMultipleSelection = YES;
    
    CGRect rect = CGRectMake(0, 0, 43, 40);
    self.castItem = [[CastButtonItem alloc] initWithButtonFrame:rect];
    self.navigationItem.rightBarButtonItem = self.castItem;
    self.navigationItem.rightBarButtonItem.enabled = YES;
    self.castItem.castStatus = [[PhotoShareController sharedInstance] getCastStatus];
    [self.castItem.castButton addTarget:self action:@selector(cast) forControlEvents:UIControlEventTouchUpInside];
    
    [self performGetRequest];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)performGetRequest
{
    //NSString* youtubeURL = [youtubeAPI stringByAppendingString:APIKey];
    
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

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    
    NSLog(@"count is %lu", itemsArray.count);
    
    return itemsArray.count - 1;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
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

#pragma mark <UICollectionViewDelegate>

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    
    if(![PhotoShareController sharedInstance].app.isConnected)
    {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Not Connected" message:@"Please connect to TV and then play video" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        
        [alert show];
      
        return;
    }
    
     NSDictionary *item = [itemsArray objectAtIndex:indexPath.item + 1];
    
    if([[[item objectForKey:@"id"] objectForKey:@"kind" ] isEqualToString:@"youtube#video"])
    {
        NSString* videoId = [[item objectForKey:@"id"] objectForKey:@"videoId"];
        NSLog(@"id is %@", videoId);
        
        NSString *title = [[item objectForKey:@"snippet"] objectForKey:@"title"];
        NSLog(@"title is %@", title);
        
        NSString *thumbnailURL = [[[[item objectForKey:@"snippet"] objectForKey:@"thumbnails"] objectForKey:@"high"] objectForKey:@"url"];
        NSLog(@"url is %@", thumbnailURL);
        
        [[PhotoShareController sharedInstance] launchApp:videoId :title :thumbnailURL];
        
    }
}

- (void)collectionView:(UICollectionView *)collectionView didDeselectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    
}

- (void)collectionView:(UICollectionView *)collectionView didHighlightItemAtIndexPath:(NSIndexPath *)indexPath {
    
    
}

- (void)collectionView:(UICollectionView *)collectionView didUnhighlightItemAtIndexPath:(NSIndexPath *)indexPath {
    
    
}

-(void) cast
{
    switch (self.castItem.castStatus) {
        case notReady:
            return;
        case connecting:
            return;
        case connected:
        {
            TerminateAppViewController * terminateApp = [[TerminateAppViewController alloc] initWithStyle:UITableViewStylePlain];
            [self presentPopover:terminateApp];
        }
            
        case readyToConnect:
        {
            DeviceListViewController * deviceList = [[DeviceListViewController alloc] initWithStyle:UITableViewStylePlain];
            [self presentPopover:deviceList];
        }
    }
}

-(void) presentPopover:(UIViewController *)viewController
{
    
    viewController.preferredContentSize = CGSizeMake(320,186);
    viewController.modalPresentationStyle = UIModalPresentationPopover;
    UIPopoverPresentationController * presentationController = viewController.popoverPresentationController;
    presentationController.sourceView = self.castItem.castButton;
    presentationController.sourceRect = self.castItem.castButton.bounds;
    viewController.popoverPresentationController.delegate = self;
    [self presentViewController:viewController animated:NO completion:^{}];
}

-(UIModalPresentationStyle)adaptivePresentationStyleForPresentationController:(UIPresentationController *)controller
{
    return UIModalPresentationNone;
}

@end
