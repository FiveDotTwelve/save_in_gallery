#import "SaveInGalleryPlugin.h"
#import <save_in_gallery/save_in_gallery-Swift.h>

@implementation SaveInGalleryPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSaveInGalleryPlugin registerWithRegistrar:registrar];
}
@end
