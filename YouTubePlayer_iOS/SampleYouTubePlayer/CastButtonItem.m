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

#import "CastButtonItem.h"

@implementation CastButtonItem

@synthesize castStatus;

-(id) initWithButtonFrame:(CGRect) buttonFrame
{
    UIButton * button =  [UIButton buttonWithType:UIButtonTypeCustom];
    button.frame = buttonFrame;
    self = [super initWithCustomView:button];
    if (self)
    {
        self.castButton =  button;
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(statusDidChange:)
                                                     name:@"CastStatusDidChange"
                                                   object:nil];
    }
    return self;
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(UIButton*) castButton
{
    NSLog(@"castButton View %@", [(UIButton*)self.customView description]);
    return (UIButton*)self.customView;
}
- (void)setCastStatus:(CastStatus)status
{
    castStatus = status;
    if (self.castButton.imageView.isAnimating) {
        [self.castButton.imageView stopAnimating];
    }
    UIImage *castImage = nil;
    self.tintColor = [UIColor whiteColor];
    switch (castStatus) {
        case notReady:
            castImage = [[UIImage imageNamed:@"cast_off.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
            [self.castButton setImage:castImage forState:UIControlStateNormal];
            self.castButton.tintColor = [UIColor whiteColor];
            self.enabled = NO;
            break;
        case readyToConnect:
            castImage = [[UIImage imageNamed:@"cast_off.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
            [self.castButton setImage:castImage forState:UIControlStateNormal];
            self.castButton.tintColor = [UIColor whiteColor];
            self.enabled = YES;
            break;
        case connecting:
            self.castButton.imageView.animationImages = @[[[UIImage imageNamed:@"cast_on0.png"]imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate],[[UIImage imageNamed:@"cast_on1.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate],[[UIImage imageNamed:@"cast_on2.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate],[[UIImage imageNamed:@"cast_on1.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate]];
            self.castButton.imageView.animationDuration = 2;
            [self.castButton.imageView startAnimating];
            self.castButton.tintColor = [UIColor yellowColor];
            break;
        case connected:
            if (self.castButton.imageView.isAnimating) {
                [self.castButton.imageView stopAnimating];
            }
            self.tintColor = [UIColor yellowColor];
            castImage = [[UIImage imageNamed:@"cast_on.png"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
            [self.castButton setImage:castImage forState:UIControlStateNormal];
            self.castButton.tintColor = [UIColor yellowColor];
            self.enabled = YES;
    }
}

-(void) statusDidChange:(NSNotification *)notification
{
    NSDictionary *info = [notification userInfo];
    NSString * status = info[@"status"];
    self.castStatus = [status CastStatusEnumFromString];
}

@end

@implementation NSString (CastStatusEnumParser)
- (CastStatus)CastStatusEnumFromString{
    NSDictionary *CastStatuses = [NSDictionary dictionaryWithObjectsAndKeys:
                            [NSNumber numberWithInteger:notReady], @"notReady",
                            [NSNumber numberWithInteger:readyToConnect], @"readyToConnect",
                            [NSNumber numberWithInteger:connecting], @"connecting",
                            [NSNumber numberWithInteger:connected], @"connected",
                            nil
                            ];
    return (CastStatus)[[CastStatuses objectForKey:self] intValue];
}
@end