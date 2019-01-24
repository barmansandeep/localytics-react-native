#import "LLWebView.h"
#import "LocalyticsPlugin.h"

@interface LLWebView()

@property (nonatomic, assign, nonnull) LLMarketingWebViewHandler *webViewHandler;
@property (nonatomic, assign, nonnull) id<LLCampaignUpdated> updatedCallback;

@end

@implementation LLWebView

-(instancetype)initWithMarketingHandler:(LLMarketingWebViewHandler *)webViewHandler
                andCampaignUpdatedBlock:(id<LLCampaignUpdated>)callback {
    //this will be overridden by react - so this doesn't matter.
    CGRect frame = CGRectMake(0, 0, 100, 100);
    if (self = [super initWithFrame:frame configuration:[WKWebViewConfiguration new]]) {
        _webViewHandler = webViewHandler;
        _updatedCallback = callback;
    }
    return self;
}

- (void)setCampaign:(NSInteger)campaignId {
    LLInboxCampaign *campaign = [LocalyticsPlugin inboxCampaignFromCache:campaignId];
    if (campaign != nil) {
        //this should update the scripts
        [Localytics setupWebViewConfiguration:self.configuration withCampaign:campaign];
        
        self.webViewHandler.campaign = campaign;
        [self.updatedCallback campaignUpdated];
        [self.webViewHandler loadCreative];
    }
}

@end
