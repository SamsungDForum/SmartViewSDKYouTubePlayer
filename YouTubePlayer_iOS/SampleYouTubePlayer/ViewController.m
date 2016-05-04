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
#import "YoutubeDataFetcher.h"

@interface ViewController ()

@end

@implementation ViewController

static NSString * const reuseIdentifier = @"photoCell";

//static NSString* youtubeAPI = @"https://www.googleapis.com/youtube/v3/search?q=movie+trailers&part=snippet&maxResults=50&order=relevance";

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    self.collectionView.allowsMultipleSelection = YES;
    
    CGRect rect = CGRectMake(0, 0, 43, 40);
    self.castItem = [[CastButtonItem alloc] initWithButtonFrame:rect];
    self.navigationItem.rightBarButtonItem = self.castItem;
    self.navigationItem.rightBarButtonItem.enabled = YES;
    [self setTitle:@"YouTube Player"];
    self.castItem.castStatus = [[PhotoShareController sharedInstance] getCastStatus];
    [self.castItem.castButton addTarget:self action:@selector(cast) forControlEvents:UIControlEventTouchUpInside];
    
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(listRefreshed:) name:kListPrepared object:[YoutubeDataFetcher getSharedInstance]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)testBtnAction:(id)sender {


}

-(void)downloadImageWithURL:(NSURL *)url completionBlock:(void (^)(bool succeeded, UIImage *image))completionBlock
{
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
    [[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
       
        if(error == nil)
        {
            UIImage *image = [[UIImage alloc] initWithData:data];
            completionBlock(YES, image);
            
        }
        
    }] resume];
        });
}

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{  
    return [[[YoutubeDataFetcher getSharedInstance] videoArray] count] ;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    PhotoCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:reuseIdentifier forIndexPath:indexPath];
    
    if([[[YoutubeDataFetcher getSharedInstance] videoArray] count] > indexPath.item)
    {
        VideoInfo *vidObj = [[[YoutubeDataFetcher getSharedInstance] videoArray] objectAtIndex:indexPath.item];
        UIImage *img = [vidObj getThumbnailImage];
        
        if (img == nil)
        {
            NSURL *imageURL = [NSURL URLWithString:[vidObj thumbnailURL]];
            dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0ul);
            dispatch_async(queue, ^{
                NSURLRequest *request = [NSURLRequest requestWithURL:imageURL];
                NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
                
                [[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                    
                    if(error == nil)
                    {
                        UIImage *img = [UIImage imageWithData:data];
                        [vidObj storeThumbnailImage:data];
                        dispatch_async(dispatch_get_main_queue(), ^{
                            cell.photoCellImage.image = img;
                        });
                        
                    }
                    
                }] resume];
                
                
            });
        }
        else
        {
            cell.photoCellImage.image = img;
        }
    }
    
    return cell;
}

-(void) listRefreshed: (NSNotification *)notification
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self collectionView] reloadData];
    });
    
}

#pragma mark <UICollectionViewDelegate>

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    
    if(![PhotoShareController sharedInstance].app.isConnected)
    {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Not Connected" message:@"Please connect to TV and then play video" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        
        [alert show];
      
        return;
    }
    
    VideoInfo *vidObj = [[[YoutubeDataFetcher getSharedInstance] videoArray] objectAtIndex:indexPath.item];
    [[PhotoShareController sharedInstance] launchApp:vidObj.videoId  :vidObj.title :vidObj.thumbnailURL];
    
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
