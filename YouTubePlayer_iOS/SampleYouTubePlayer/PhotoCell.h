//
//  PhotoCell.h
//  SampleYouTubePlayer
//
//  Created by CHIRAG BAHETI on 21/03/16.
//  Copyright © 2016 samsung. All rights reserved.
//

#ifndef PhotoCell_h
#define PhotoCell_h

#import <UIKit/UIKit.h>

@interface PhotoCell : UICollectionViewCell


@property (weak, nonatomic) IBOutlet UIImageView *photoCellImage;

@property (weak, nonatomic) IBOutlet UILabel *titleText;

@end

#endif /* PhotoCell_h */
