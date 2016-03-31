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

#import "DeviceListViewController.h"
#import "PhotoShareController.h"

@interface DeviceListViewController ()

@end

@implementation DeviceListViewController

static NSString * const reuseIdentifier = @"DeviceCell";

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:reuseIdentifier];
}

-(void)viewWillAppear:(BOOL)animated
{
    self.didFindServiceObserver = [[PhotoShareController sharedInstance].search on:@"ms.didFindService" performClosure:^(NSNotification * notification) {
        [self.tableView reloadData];
    }];
    
    self.didRemoveServiceObserver = [[PhotoShareController sharedInstance].search on:@"ms.didRemoveService" performClosure:^(NSNotification * notification) {
        [self.tableView reloadData];
    }];
}

-(void)viewWillDisappear:(BOOL)animated
{
    if (self.didFindServiceObserver != nil) {
        [[PhotoShareController sharedInstance].search off:self.didFindServiceObserver];
    }
    if (self.didRemoveServiceObserver != nil) {
        [[PhotoShareController sharedInstance].search off:self.didRemoveServiceObserver];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if ([PhotoShareController sharedInstance].search.isSearching)
    {
        return [PhotoShareController sharedInstance].services.count;
    } else {
        return 1;
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier forIndexPath:indexPath];
    
    // Configure the cell...
    cell.textLabel.text = [[PhotoShareController sharedInstance].services[indexPath.row] name];
    return cell;
    
}
-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Devices";
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([PhotoShareController sharedInstance].search.isSearching)
    {
        [[PhotoShareController sharedInstance] connect:[PhotoShareController sharedInstance].services[indexPath.row]];
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}



@end
