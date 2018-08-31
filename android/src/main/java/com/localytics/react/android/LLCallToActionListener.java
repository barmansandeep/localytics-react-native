package com.localytics.react.android;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import com.localytics.android.CallToActionListener;
import com.localytics.android.Campaign;

import java.util.List;
import java.util.Map;

public class LLCallToActionListener implements CallToActionListener {

  private final RCTNativeAppEventEmitter eventEmitter;

  public LLCallToActionListener(ReactContext reactContext) {
    eventEmitter = reactContext.getJSModule(RCTNativeAppEventEmitter.class);
  }

  @Override
  public boolean localyticsShouldDeeplink(String url, Campaign campaign) {
    WritableMap params = Arguments.createMap();
    params.putString("url", url);
    params.putMap("campaign", LLLocalyticsModule.toWritableMap(campaign));
    eventEmitter.emit("localyticsShouldDeeplink", params);
    return true;
  }
 
  @Override
  public void localyticsDidOptOut(boolean optOut, Campaign campaign) {
    WritableMap params = Arguments.createMap();
    params.putBoolean("optedOut", optOut);
    params.putMap("campaign", LLLocalyticsModule.toWritableMap(campaign));
    eventEmitter.emit("localyticsDidOptOut", params);
  }
 
  @Override
  public void localyticsDidPrivacyOptOut(boolean optOut, Campaign campaign) {
    WritableMap params = Arguments.createMap();
    params.putBoolean("privacyOptedOut", optOut);
    params.putMap("campaign", LLLocalyticsModule.toWritableMap(campaign));
    eventEmitter.emit("localyticsDidPrivacyOptOut", params);
  }
 
  @Override
  public boolean localyticsShouldPromptForLocationPermissions(Campaign campaign) {
    WritableMap params = Arguments.createMap();
    params.putMap("campaign", LLLocalyticsModule.toWritableMap(campaign));
    eventEmitter.emit("localyticsShouldPromptForLocationPermissions", params);
    return true;
  }
}
