
#if __has_include(<React/RCTBridgeModule.h>)
  #import <React/RCTBridgeModule.h>
#else
  #import "RCTBridgeModule.h"
#endif

typedef NS_ENUM(NSUInteger, LLInAppMessageDismissButtonLocation);

@interface LLLocalytics : NSObject <RCTBridgeModule>

@end
