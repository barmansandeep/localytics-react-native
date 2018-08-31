
package com.localytics.react.android;

import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import com.localytics.android.CircularRegion;
import com.localytics.android.Customer;
import com.localytics.android.Campaign;
import com.localytics.android.InAppCampaign;
import com.localytics.android.InboxCampaign;
import com.localytics.android.InboxRefreshListener;
import com.localytics.android.Localytics;
import com.localytics.android.PlacesCampaign;
import com.localytics.android.PushCampaign;
import com.localytics.android.Region;

import org.w3c.dom.Text;

import java.lang.Runnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLLocalyticsModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private final Handler backgroundHandler;

  private LongSparseArray<InboxCampaign> inboxCampaignCache = new LongSparseArray<InboxCampaign>();
  private final LongSparseArray<InAppCampaign> inAppCampaignCache = new LongSparseArray<InAppCampaign>();
  private final LongSparseArray<PushCampaign> pushCampaignCache = new LongSparseArray<PushCampaign>();
  private final LongSparseArray<PlacesCampaign> placesCampaignCache = new LongSparseArray<PlacesCampaign>();

  private LLAnalyticsListener analyticsListener;
  private LLLocationListener locationListener;
  private LLCallToActionListener ctaListener;
  private LLMessagingListener messagingListener;

  public LLLocalyticsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    HandlerThread thread = new HandlerThread("LLLocalyticsModule-Handler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    backgroundHandler = new Handler(thread.getLooper());
  }

  @Override
  public String getName() {
    return "LLLocalytics";
  }

  /************************************
   * Integration
   ************************************/

  @ReactMethod
  public void upload() {
    Localytics.upload();
  }

  @ReactMethod
  public void openSession() {
    Localytics.openSession();
  }

  @ReactMethod
  public void closeSession() {
    Localytics.closeSession();
  }

  @ReactMethod
  public void pauseDataUploading(Boolean paused) {
    Localytics.pauseDataUploading(paused);
  }

  /************************************
   * Analytics
   ************************************/

  @ReactMethod
  public void setOptedOut(Boolean optedOut) {
    Localytics.setOptedOut(optedOut);
  }

  @ReactMethod
  public void isOptedOut(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isOptedOut());
      }
    });
  }

  @ReactMethod
  public void setPrivacyOptedOut(Boolean optedOut) {
    Localytics.setPrivacyOptedOut(optedOut);
  }

  @ReactMethod
  public void isPrivacyOptedOut(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isPrivacyOptedOut());
      }
    });
  }

  @ReactMethod
  public void tagEvent(ReadableMap params) {
    String name = getString(params, "name");
    if (!TextUtils.isEmpty(name)) {
      int clv = getInt(params, "customerValueIncrease");
      Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
      Localytics.tagEvent(name, attributes, clv);
    } else {
      logNullParameterError("tagEvent", "name", name);
    }
  }

  @ReactMethod
  public void tagPurchased(ReadableMap params) {
    String itemName = getString(params, "itemName");
    String itemId = getString(params, "itemId");
    String itemType = getString(params, "itemType");
    Long itemPrice = getLong(params, "itemPrice");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagPurchased(itemName, itemId, itemType, itemPrice, attributes);
  }

  @ReactMethod
  public void tagAddedToCart(ReadableMap params) {
    String itemName = getString(params, "itemName");
    String itemId = getString(params, "itemId");
    String itemType = getString(params, "itemType");
    Long itemPrice = getLong(params, "itemPrice");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagAddedToCart(itemName, itemId, itemType, itemPrice, attributes);
  }

  @ReactMethod
  public void tagStartedCheckout(ReadableMap params) {
    Long totalPrice = getLong(params, "totalPrice");
    Long itemCount = getLong(params, "itemCount");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagStartedCheckout(totalPrice, itemCount, attributes);
  }

  @ReactMethod
  public void tagCompletedCheckout(ReadableMap params) {
    Long totalPrice = getLong(params, "totalPrice");
    Long itemCount = getLong(params, "itemCount");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagCompletedCheckout(totalPrice, itemCount, attributes);
  }

  @ReactMethod
  public void tagContentViewed(ReadableMap params) {
    String contentName = getString(params, "contentName");
    String contentId = getString(params, "contentId");
    String contentType = getString(params, "contentType");
    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagContentViewed(contentName, contentId, contentType, attributes);
  }

  @ReactMethod
  public void tagSearched(ReadableMap params) {
    String queryText = getString(params, "queryText");
    String contentType = getString(params, "contentType");
    Long resultCount = getLong(params, "resultCount");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagSearched(queryText, contentType, resultCount, attributes);
  }

  @ReactMethod
  public void tagShared(ReadableMap params) {
    String contentName = getString(params, "contentName");
    String contentId = getString(params, "contentId");
    String contentType = getString(params, "contentType");
    String methodName = getString(params, "methodName");
    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagShared(contentName, contentId, contentType, methodName, attributes);
  }

  @ReactMethod
  public void tagContentRated(ReadableMap params) {
    String contentName = getString(params, "contentName");
    String contentId = getString(params, "contentId");
    String contentType = getString(params, "contentType");
    Long rating = getLong(params, "rating");

    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagContentRated(contentName, contentId, contentType, rating, attributes);
  }

  @ReactMethod
  public void tagCustomerRegistered(ReadableMap params) {
    Customer customer = null;
    if (params.hasKey("customer")) {
      customer = toCustomer(params.getMap("customer"));
    }
    String methodName = getString(params, "methodName");
    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagCustomerRegistered(customer, methodName, attributes);
  }

  @ReactMethod
  public void tagCustomerLoggedIn(ReadableMap params) {
    Customer customer = null;
    if (params.hasKey("customer")) {
      customer = toCustomer(params.getMap("customer"));
    }
    String methodName = getString(params, "methodName");
    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagCustomerLoggedIn(customer, methodName, attributes);
  }

  @ReactMethod
  public void tagCustomerLoggedOut(ReadableMap attributes) {
    Localytics.tagCustomerLoggedOut(toStringMap(attributes));
  }

  @ReactMethod
  public void tagInvited(ReadableMap params) {
    String methodName = getString(params, "methodName");
    Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
    Localytics.tagInvited(methodName, attributes);
  }

  @ReactMethod
  public void tagInboxImpression(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    InboxCampaign campaign = inboxCampaignCache.get(campaignId);
    if (campaign != null) {
      String action = getString(params, "action");
      if ("click".equalsIgnoreCase(action)) {
        Localytics.tagInboxImpression(campaign, Localytics.ImpressionType.CLICK);
      } else if ("dismiss".equalsIgnoreCase(action)) {
        Localytics.tagInboxImpression(campaign, Localytics.ImpressionType.DISMISS);
      } else if (!TextUtils.isEmpty(action)) {
        Localytics.tagInboxImpression(campaign, action);
      } else {
        logNullParameterError("tagInboxImpression", "action", action);
      }
    } else {
      logInvalidParameterError("tagInboxImpression", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void tagPushToInboxImpression(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    InboxCampaign campaign = inboxCampaignCache.get(campaignId);
    if (campaign != null) {
      Localytics.tagPushToInboxImpression(campaign);
    } else {
      logInvalidParameterError("tagPushToInboxImpression", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void tagInAppImpression(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    InAppCampaign campaign = inAppCampaignCache.get(campaignId);
    if (campaign != null) {
      String action = getString(params, "action");
      if ("click".equalsIgnoreCase(action)) {
        Localytics.tagInAppImpression(campaign, Localytics.ImpressionType.CLICK);
      } else if ("dismiss".equalsIgnoreCase(action)) {
        Localytics.tagInAppImpression(campaign, Localytics.ImpressionType.DISMISS);
      } else if (!TextUtils.isEmpty((action))) {
        Localytics.tagInAppImpression(campaign, action);
      } else {
        logNullParameterError("tagInAppImpression", "action", action);
      }
    } else {
      logInvalidParameterError("tagInAppImpression", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void tagPlacesPushReceived(long campaignId) {
    PlacesCampaign campaign = placesCampaignCache.get(campaignId);
    if (campaign != null) {
      Localytics.tagPlacesPushReceived(campaign);
    } else {
      logInvalidParameterError("tagPlacesPushReceived", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void tagPlacesPushOpened(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    PlacesCampaign campaign = placesCampaignCache.get(campaignId);
    if (campaign != null) {
      String action = getString(params, "action");
      Localytics.tagPlacesPushOpened(campaign, action);
    } else {
      logInvalidParameterError("tagPlacesPushOpened", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void tagScreen(String screen) {
    if (!TextUtils.isEmpty(screen)) {
      Localytics.tagScreen(screen);
    } else {
      logInvalidParameterError("tagScreen", "screen", "Parameter screen can not be empty", screen);
    }
  }

  @ReactMethod
  public void setCustomDimension(ReadableMap params) {
      int dimension = getCustomDimensionIndex(params, "dimension");
      if (0 <= dimension && dimension <= 19) {
        String value = getString(params, "value");
        Localytics.setCustomDimension(dimension, value);
      } else {
        logInvalidParameterError("setCustomDimension", "dimension", "Custom dimension index must be between 0 and 19", Integer.toString(dimension));
      }
  }

  @ReactMethod
  public void getCustomDimension(final int dimension, final Promise promise) {
    if (0 <= dimension && dimension <= 19) {
      backgroundHandler.post(new Runnable() {
        @Override
        public void run() {
          promise.resolve(Localytics.getCustomDimension(dimension));
        }
      });
    } else {
      logInvalidParameterError("getCustomDimension", "dimension", "Custom dimension index must be between 0 and 19", Integer.toString(dimension));
    }
  }

  @ReactMethod
  public void setAnalyticsEventsEnabled(Boolean enabled) {
    if (enabled) {
      if (analyticsListener == null) {
        analyticsListener = new LLAnalyticsListener(reactContext);
      }
      Localytics.setAnalyticsListener(analyticsListener);
    } else {
      Localytics.setAnalyticsListener(null);
    }
  }

  /************************************
   * Profiles
   ************************************/

  @ReactMethod
  public void setProfileAttribute(ReadableMap params) {
    String name = getString(params, "name");
    Dynamic value = getDynamic(params, "value");
    if (!TextUtils.isEmpty(name) && value != null) {
      String scope = getString(params, "scope");
      switch(value.getType()) {
        case String:
          // Dates will be passed in as "YYYY-MM-DD"
          Localytics.setProfileAttribute(name, value.asString(), toScope(scope));
          break;
        case Number:
          Localytics.setProfileAttribute(name, (long) value.asInt(), toScope(scope));
          break;
        case Array:
          ReadableArray array = value.asArray();
          if (array.size() > 0) {
            for (int i = 0; i < array.size(); i++) { // for-each loop not available with ReadableArray
              ReadableType type = array.getType(i);
              if (!ReadableType.Number.equals(type)) { // default to String
                Localytics.setProfileAttribute(name, toStringArray(array), toScope(scope));
                return;
              }
            }
            Localytics.setProfileAttribute(name, toLongArray(array), toScope(scope));
          } else {
            logNullParameterError("setProfileAttribute", "value", array.toString());
          }
          break;
       }
    } else {
      logNullParameterError("setProfileAttribute", "name, value", String.format("name: %s, value: %s", name, value));
    }
   }

  @ReactMethod
  public void addProfileAttributesToSet(ReadableMap params) {
    String name = getString(params, "name");
    ReadableArray values = getReadableArray(params, "values");
    if (!TextUtils.isEmpty(name) && values != null) {
      String scope = getString(params, "scope");
      if (values.size() > 0) {
        for (int i = 0; i < values.size(); i++) { // for-each loop not available with ReadableArray
          ReadableType type = values.getType(i);
          if (!ReadableType.Number.equals(type)) { // default to String
            Localytics.setProfileAttribute(name, toStringArray(values), toScope(scope));
            return;
          }
        }
        Localytics.setProfileAttribute(name, toLongArray(values), toScope(scope));
      } else {
        logNullParameterError("addProfileAttributesToSet", "values", values.toString());
      }
    } else {
      logNullParameterError("addProfileAttributesToSet", "name, values", String.format("name: %s, values: %s", name, values));
    }
  }

  @ReactMethod
  public void removeProfileAttributesFromSet(ReadableMap params) {
    String name = getString(params, "name");
    ReadableArray values = getReadableArray(params, "values");
    if (!TextUtils.isEmpty(name) && values != null) {
      String scope = getString(params, "scope");
      if (values.size() > 0) {
        for (int i = 0; i < values.size(); i++) { // for-each loop not available with ReadableArray
          ReadableType type = values.getType(i);
          if (!ReadableType.Number.equals(type)) { // default to String
            Localytics.removeProfileAttributesFromSet(name, toStringArray(values), toScope(scope));
            return;
          }
        }
        Localytics.removeProfileAttributesFromSet(name, toLongArray(values), toScope(scope));
      } else {
        logNullParameterError("removeProfileAttributesFromSet", "values", values.toString());
      }
    } else {
      logNullParameterError("removeProfileAttributesFromSet", "name, values", String.format("name: %s, values: %s", name, values));
    }
  }

  @ReactMethod
  public void incrementProfileAttribute(ReadableMap params) {
    String name = getString(params, "name");
    if (!TextUtils.isEmpty(name)) {
      int value = getInt(params, "value");
      if (value != 0) {
        String scope = getString(params, "scope");
        Localytics.incrementProfileAttribute(name, value, toScope(scope));
      } else {
        logInvalidParameterError("incrementProfileAttribute", "value", "Attempting to increment by 0", Integer.toString(value));
      }
    } else {
      logNullParameterError("incrementProfileAttribute", "name", name);
    }
  }

  @ReactMethod
  public void decrementProfileAttribute(ReadableMap params) {
    String name = getString(params, "name");
    if (!TextUtils.isEmpty(name)) {
      int value = getInt(params, "value");
      if (value != 0) {
        String scope = getString(params, "scope");
        Localytics.decrementProfileAttribute(name, value, toScope(scope));
      } else {
        logInvalidParameterError("decrementProfileAttribute", "value", "Attempting to decrement by 0", Integer.toString(value));
      }
    } else {
      logNullParameterError("decrementProfileAttribute", "name", name);
    }
  }

  @ReactMethod
  public void deleteProfileAttribute(ReadableMap params) {
    String name = getString(params, "name");
    if (!TextUtils.isEmpty(name)) {
      String scope = getString(params, "scope");
      Localytics.deleteProfileAttribute(name, toScope(scope));
    } else {
      logNullParameterError("deleteProfileAttribute", "name", name);
    }
  }

  @ReactMethod
  public void setCustomerEmail(String email) {
    Localytics.setCustomerEmail(email);
  }

  @ReactMethod
  public void setCustomerFirstName(String firstName) {
    Localytics.setCustomerFirstName(firstName);
  }

  @ReactMethod
  public void setCustomerLastName(String lastName) {
    Localytics.setCustomerLastName(lastName);
  }

  @ReactMethod
  public void setCustomerFullName(String fullName) {
    Localytics.setCustomerFullName(fullName);
  }

  /************************************
   * Messaging
   ************************************/

  @ReactMethod
  public void triggerInAppMessage(ReadableMap params) {
    String triggerName = getString(params, "triggerName");
    if (!TextUtils.isEmpty(triggerName)) {
      Map<String, String> attributes = toStringMap(getReadableMap(params, "attributes"));
      if (attributes != null) {
        Localytics.triggerInAppMessage(triggerName, attributes);
      } else {
        Localytics.triggerInAppMessage(triggerName);
      }
    } else {
      logNullParameterError("triggerInAppMessage", "triggerName", triggerName);
    }
  }

  @ReactMethod
  public void triggerInAppMessagesForSessionStart() {
    Localytics.triggerInAppMessagesForSessionStart();
  }

  @ReactMethod
  public void dismissCurrentInAppMessage() {
    Localytics.dismissCurrentInAppMessage();
  }

  @ReactMethod
  public void setInAppMessageDismissButtonLocation(String location) {
    Localytics.setInAppMessageDismissButtonLocation(toDismissButtonLocation(location));
  }

  @ReactMethod
  public void getInAppMessageDismissButtonLocation(Promise promise) {
    Localytics.InAppMessageDismissButtonLocation location = Localytics.getInAppMessageDismissButtonLocation();
    if (Localytics.InAppMessageDismissButtonLocation.RIGHT.equals(location)) {
      promise.resolve("right");
    } else {
      promise.resolve("left");
    }
  }

  @ReactMethod
  public void setInAppMessageDismissButtonHidden(Boolean hidden) {
    Localytics.setInAppMessageDismissButtonVisibility(hidden ? View.GONE : View.VISIBLE);
  }

  @ReactMethod
  public void setInAppMessageConfiguration(ReadableMap config) {
    getMessagingListener(true).setInAppConfigurationMap(config);
  }

  @ReactMethod
  public void appendAdidToInAppUrls(Boolean append) {
    Localytics.appendAdidToInAppUrls(append);
  }

  @ReactMethod void isAdidAppendedToInAppUrls(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isAdidAppendedToInAppUrls());
      }
    });
  }

  @ReactMethod
  public void registerPush() {
    Localytics.registerPush();
  }

  @ReactMethod
  public void setPushRegistrationId(String registrationId) {
    Localytics.setPushRegistrationId(registrationId);
  }

  @ReactMethod
  public void getPushRegistrationId(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getPushRegistrationId());
      }
    });
  }

  @ReactMethod
  public void setNotificationsDisabled(Boolean disabled) {
    Localytics.setNotificationsDisabled(disabled);
  }

  @ReactMethod
  public void areNotificationsDisabled(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.areNotificationsDisabled());
      }
    });
  }

  @ReactMethod
  public void setPushMessageConfiguration(ReadableMap config) {
    // Enable messaging events first
    getMessagingListener(true).setPushConfigurationMap(config);
  }

  @ReactMethod
  public void getInboxCampaigns(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        List<InboxCampaign> campaigns = Localytics.getInboxCampaigns();

        // Cache campaigns
        for (InboxCampaign campaign : campaigns) {
          inboxCampaignCache.put(campaign.getCampaignId(), campaign);
        }

        promise.resolve(toInboxCampaignsWritableArray(campaigns));
      }
    });
  }

  @ReactMethod
  public void getDisplayableInboxCampaigns(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        List<InboxCampaign> campaigns = Localytics.getDisplayableInboxCampaigns();

        // Cache campaigns
        for (InboxCampaign campaign : campaigns) {
          inboxCampaignCache.put(campaign.getCampaignId(), campaign);
        }

        promise.resolve(toInboxCampaignsWritableArray(campaigns));
      }
    });
  }

  @ReactMethod
  public void getAllInboxCampaigns(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        List<InboxCampaign> campaigns = Localytics.getAllInboxCampaigns();
        LongSparseArray<InboxCampaign> cache = new LongSparseArray<InboxCampaign>();

        // Cache campaigns
        for (InboxCampaign campaign : campaigns) {
          cache.put(campaign.getCampaignId(), campaign);
        }

        //Set new inbox cache
        inboxCampaignCache = cache;
        promise.resolve(toInboxCampaignsWritableArray(campaigns));
      }
    });
  }

  @ReactMethod
  public void refreshInboxCampaigns(final Promise promise) {
    Localytics.refreshInboxCampaigns(new InboxRefreshListener() {
      @Override
      public void localyticsRefreshedInboxCampaigns(List<InboxCampaign> campaigns) {
        // Cache campaigns
        for (InboxCampaign campaign : campaigns) {
          inboxCampaignCache.put(campaign.getCampaignId(), campaign);
        }

        promise.resolve(toInboxCampaignsWritableArray(campaigns));
      }
    });
  }

  @ReactMethod
  public void refreshAllInboxCampaigns(final Promise promise) {
    Localytics.refreshAllInboxCampaigns(new InboxRefreshListener() {
      @Override
      public void localyticsRefreshedInboxCampaigns(List<InboxCampaign> campaigns) {
        LongSparseArray<InboxCampaign> cache = new LongSparseArray<InboxCampaign>();

        // Cache campaigns
        for (InboxCampaign campaign : campaigns) {
          cache.put(campaign.getCampaignId(), campaign);
        }

        //Set new inbox cache
        inboxCampaignCache = cache;
        promise.resolve(toInboxCampaignsWritableArray(campaigns));
      }
    });
  }

  @ReactMethod
  public void setInboxCampaignRead(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    InboxCampaign campaign = inboxCampaignCache.get(campaignId);
    if (campaign != null) {
      boolean read = getBoolean(params, "read");
      Localytics.setInboxCampaignRead(campaign, read);
    } else {
      logInvalidParameterError("setInboxCampaignRead", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void deleteInboxCampaign(final Integer campaignId) {
    InboxCampaign campaign = inboxCampaignCache.get(campaignId);
    if (campaign != null) {
      Localytics.deleteInboxCampaign(campaign);
    } else {
      logInvalidParameterError("deleteInboxCampaign", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
    }
  }

  @ReactMethod
  public void getInboxCampaignsUnreadCount(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getInboxCampaignsUnreadCount());
      }
    });
  }

  @ReactMethod
  public void appendAdidToInboxUrls(Boolean append) {
    Localytics.appendAdidToInboxUrls(append);
  }

  @ReactMethod void isAdidAppendedToInboxUrls(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isAdidAppendedToInboxUrls());
      }
    });
  }

  @ReactMethod
  public void triggerPlacesNotification(ReadableMap params) {
    long campaignId = getLong(params, "campaignId");
    String regionId = getString(params, "regionId");
    if (TextUtils.isEmpty(regionId)) {
      PlacesCampaign campaign = placesCampaignCache.get(campaignId);
      if (campaign != null) {
        Localytics.triggerPlacesNotification(campaign);
      } else {
        logInvalidParameterError("triggerPlacesNotification", "campaignId", "Unable to find campaign by id", Long.toString(campaignId));
      }
    } else {
      Localytics.triggerPlacesNotification(campaignId, regionId);
    }
  }

  @ReactMethod
  public void setPlacesMessageConfiguration(ReadableMap config) {
    getMessagingListener(true).setPlacesConfigurationMap(config);
  }

  @ReactMethod
  public void setMessagingEventsEnabled(Boolean enabled) {
    if (enabled) {
      if (messagingListener == null) {
        messagingListener = new LLMessagingListener(reactContext, inAppCampaignCache, pushCampaignCache, placesCampaignCache);
      }
      Localytics.setMessagingListener(messagingListener);
    } else {
      Localytics.setMessagingListener(null);
    }
  }

  private LLMessagingListener getMessagingListener(boolean eventsEnabled) {
    if (messagingListener == null) {
      messagingListener = new LLMessagingListener(reactContext, inAppCampaignCache, pushCampaignCache, placesCampaignCache);
    }
    if (eventsEnabled) {
      Localytics.setMessagingListener(messagingListener);
    } else {
      Localytics.setMessagingListener(null);
    }

    return messagingListener;
  }

  /************************************
   * Location
   ************************************/

  @ReactMethod
  public void setLocationMonitoringEnabled(Boolean enabled) {
    Localytics.setLocationMonitoringEnabled(enabled);
  }

  @ReactMethod
  public void getGeofencesToMonitor(ReadableMap params, final Promise promise) {
    final Double latitude = getLatitude(params, "latitude");
    final Double longitude = getLongitude(params, "longitude");
    if (latitude != null && longitude != null) {
      backgroundHandler.post(new Runnable() {
        @Override
        public void run() {
          List<CircularRegion> regions = Localytics.getGeofencesToMonitor(latitude, longitude);
          promise.resolve(toCircularRegionsWritableArray(regions));
        }
      });
    } else {
      logInvalidParameterError("getGeofencesToMonitor", "latitude, longitude", "Invalid coordinates provided", String.format("latitude: %s, longitude: %s", latitude, longitude));
    }

  }

  @ReactMethod
  public void triggerRegion(ReadableMap params) {
    ReadableMap region = getReadableMap(params, "region");
    String event = getString(params, "event");

    if (params.hasKey("location")) {
      ReadableMap locationMap = getReadableMap(params, "location");
      Location location = toLocation(locationMap);
      if (location != null) {
        Localytics.triggerRegion(toRegion(region), toEvent(event), location);
        return;
      } else {
        logInvalidParameterError("triggerRegion (with location)", "location", "Invalid location was provided. triggerRegion was called with a null location", locationMap.toString());
      }
    }
    Localytics.triggerRegion(toRegion(region), toEvent(event), null);
  }

  @ReactMethod
  public void triggerRegions(ReadableMap params) {
    ReadableArray regions = getReadableArray(params, "regions");
    String event = getString(params, "event");

    if (params.hasKey("location")) {
      ReadableMap locationMap = getReadableMap(params, "location");
      Location location = toLocation(locationMap);
      if (location != null) {
        Localytics.triggerRegions(toRegions(regions), toEvent(event), location);
        return;
      } else {
        logInvalidParameterError("triggerRegions (with location)", "location", "Invalid location was provided. triggerRegions was called with a null location", locationMap.toString());
      }
    }
    Localytics.triggerRegions(toRegions(regions), toEvent(event), null);
  }

  @ReactMethod
  public void setLocationEventsEnabled(Boolean enabled) {
    if (enabled) {
      if (locationListener == null) {
        locationListener = new LLLocationListener(reactContext);
      }
      Localytics.setLocationListener(locationListener);
    } else {
      Localytics.setLocationListener(null);
    }
  }

  @ReactMethod
  public void setCallToActionEventsEnabled(Boolean enabled) {
    if (enabled) {
      if (ctaListener == null) {
        ctaListener = new LLCallToActionListener(reactContext);
      }
      Localytics.setCallToActionListener(ctaListener);
    } else {
      Localytics.setCallToActionListener(null);
    }
  }

  /************************************
   * User Information
   ************************************/

  @ReactMethod
  public void setIdentifier(ReadableMap params) {
    String identifier = getString(params, "identifier");
    if (!TextUtils.isEmpty(identifier)) {
      String value = getString(params, "value");
      Localytics.setIdentifier(identifier, value);
    }
  }

  @ReactMethod
  public void getIdentifier(final String identifier, final Promise promise) {
    if (!TextUtils.isEmpty(identifier)) {
      backgroundHandler.post(new Runnable() {
        @Override
        public void run() {
          promise.resolve(Localytics.getIdentifier(identifier));
        }
      });
    }
  }

  @ReactMethod
  public void setCustomerId(String customerId) {
    Localytics.setCustomerId(customerId);
  }

  @ReactMethod
  public void setCustomerIdWithPrivacyOptedOut(String customerId, Boolean optedOut) {
    Localytics.setCustomerIdWithPrivacyOptedOut(customerId, optedOut);
  }

  @ReactMethod
  public void getCustomerId(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getCustomerId());
      }
    });
  }

  @ReactMethod
  public void setLocation(ReadableMap locationMap) {
    Localytics.setLocation(toLocation(locationMap));
  }

  /************************************
   * Developer Options
   ************************************/

  @ReactMethod
  public void setOptions(ReadableMap optionsMap) {
    ReadableMapKeySetIterator iterator = optionsMap.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      switch(optionsMap.getType(key)) {
        case String:
          Localytics.setOption(key, optionsMap.getString(key));
          break;
        case Number:
          Localytics.setOption(key, optionsMap.getInt(key));
          break;
        case Boolean:
          Localytics.setOption(key, optionsMap.getBoolean(key));
          break;
      }
    }
  }

  @ReactMethod
  public void setLoggingEnabled(Boolean enabled) {
    Localytics.setLoggingEnabled(enabled);
  }

  @ReactMethod
  public void isLoggingEnabled(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isLoggingEnabled());
      }
    });
  }

  @ReactMethod
  public void redirectLogsToDisk(ReadableMap params) {
    Boolean external = getBoolean(params, "external");
    Localytics.redirectLogsToDisk(external, reactContext);
  }

  @ReactMethod
  public void setTestModeEnabled(Boolean enabled) {
    Localytics.setTestModeEnabled(enabled);
  }

  @ReactMethod
  public void isTestModeEnabled(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.isTestModeEnabled());
      }
    });
  }

  @ReactMethod
  public void getInstallId(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getInstallId());
      }
    });
  }

  @ReactMethod
  public void getAppKey(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getAppKey());
      }
    });
  }

  @ReactMethod
  public void getLibraryVersion(final Promise promise) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        promise.resolve(Localytics.getLibraryVersion());
      }
    });
  }

  /************************************
   * React Native Helpers
   ************************************/

  private void logNullParameterError(String operation, String failingParameters, String providedParameterValues) {
    logInvalidParameterError(operation, failingParameters, "invalid null value(s)", providedParameterValues);
  }

  private void logInvalidParameterError(String operation, String failingParameters, String invalidReason, String providedParameterValues) {
    Log.w("Localytics React Native", String.format("Localytics failed to complete operation: %s. Parameter(s): %s were invalid for reason: %s. The provided parameter values were: %s.", operation, failingParameters, invalidReason, providedParameterValues));
  }


  /************************************
   * ReadableMap Getters
   ************************************/

  static String getString(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getString(key) : null;
  }

  static Integer getInt(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getInt(key) : 0;
  }

  static Integer getCustomDimensionIndex(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getInt(key) : -1;
  }

  static Double getLatitude(ReadableMap readableMap, String key) {
    if (readableMap.hasKey(key)) {
      Double latitude = readableMap.getDouble(key);
      if (-90 <= latitude && latitude <= 90) {
        return latitude;
      }
    }

    return null;
  }

  static Double getLongitude(ReadableMap readableMap, String key) {
    if (readableMap.hasKey(key)) {
      Double longitude = readableMap.getDouble(key);
      if (-180 <= longitude && longitude <= 180) {
        return longitude;
      }
    }

    return null;
  }

  static Long getLong(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? (long) readableMap.getDouble(key) : 0;
  }

  static Double getDouble(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getDouble(key) : 0.0;
  }

  static Float getFloat(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? (float) readableMap.getDouble(key) : 0.0f;
  }

  static Boolean getBoolean(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getBoolean(key) : true;
  }

  static Dynamic getDynamic(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getDynamic(key) : null;
  }

  static ReadableMap getReadableMap(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getMap(key) : null;
  }

  static ReadableArray getReadableArray(ReadableMap readableMap, String key) {
    return readableMap.hasKey(key) ? readableMap.getArray(key) : null;
  }

  /************************************
   * Conversions
   ************************************/

  static Map<String, String> toStringMap(ReadableMap readableMap) {
    if (readableMap == null) {
      return null;
    }

    ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
    if (!iterator.hasNextKey()) {
      return null;
    }

    Map<String, String> result = new HashMap<String, String>();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      ReadableType type = readableMap.getType(key);
      switch (type) {
        case Number:
          // Can be int or double
          double tmp = readableMap.getDouble(key);
          if (tmp == (int) tmp) {
            result.put(key, Integer.toString((int) tmp));
          } else {
            result.put(key, Double.toString(tmp));
          };
          break;
        case String:
          result.put(key, readableMap.getString(key));
          break;
      }
    }

    return result;
  }

  static Long toLong(Integer integer) {
    if (integer == null) {
      return null;
    }

    return Long.valueOf(integer.longValue());
  }

  static Customer toCustomer(ReadableMap readableMap) {
    if (readableMap == null) {
      return null;
    }

    return new Customer.Builder()
        .setCustomerId(getString(readableMap, "customerId"))
        .setFirstName(getString(readableMap, "firstName"))
        .setLastName(getString(readableMap, "lastName"))
        .setFullName(getString(readableMap, "fullName"))
        .setEmailAddress(getString(readableMap, "emailAddress"))
        .build();
  }

  static Localytics.ProfileScope toScope(String scope) {
    if ("org".equalsIgnoreCase(scope)) {
      return Localytics.ProfileScope.ORGANIZATION;
    } else {
      return Localytics.ProfileScope.APPLICATION;
    }
  }

  static Localytics.InAppMessageDismissButtonLocation toDismissButtonLocation(String location) {
    if ("right".equalsIgnoreCase(location)) {
      return Localytics.InAppMessageDismissButtonLocation.RIGHT;
    } else {
      return Localytics.InAppMessageDismissButtonLocation.LEFT;
    }
  }

  static String[] toStringArray(ReadableArray readableArray) {
    int size = readableArray.size();
    String[] array = new String[size];
    for (int i = 0; i < size; i++) {
      array[i] = readableArray.getString(i);
    }

    return array;
  }

  static long[] toLongArray(ReadableArray readableArray) {
    int size = readableArray.size();
    long[] array = new long[size];
    for (int i = 0; i < size; i++) {
      array[i] = (long) readableArray.getInt(i);
    }

    return array;
  }

  static WritableArray toCircularRegionsWritableArray(List<CircularRegion> regions) {
    WritableArray writableArray = Arguments.createArray();
    for (CircularRegion region : regions) {
      writableArray.pushMap(toWritableMap(region));
    }

    return writableArray;
  }

  static WritableArray toRegionsWritableArray(List<Region> regions) {
    WritableArray writableArray = Arguments.createArray();
    for (Region region : regions) {
      if (region instanceof CircularRegion) {
        writableArray.pushMap(toWritableMap((CircularRegion) region));
      }
    }

    return writableArray;
  }

  static WritableMap toWritableMap(CircularRegion region) {
    WritableMap writableMap = Arguments.createMap();
    writableMap.putString("uniqueId", region.getUniqueId());
    writableMap.putDouble("latitude", region.getLatitude());
    writableMap.putDouble("longitude", region.getLongitude());
    writableMap.putString("name", region.getName());
    writableMap.putString("type", region.getType());
    writableMap.putMap("attributes", toWritableMap(region.getAttributes()));
    writableMap.putString("originLocation", region.getOriginLocation().toString());
    writableMap.putInt("radius", region.getRadius());

    return writableMap;
  }

  static WritableMap toWritableMap(Map<String, String> map) {
    WritableMap writableMap = Arguments.createMap();
    if (map != null) {
      for (Map.Entry<String, String> entry : map.entrySet()) {
        writableMap.putString(entry.getKey(), entry.getValue());
      }
    }

    return writableMap;
  }

  static List<Region> toRegions(ReadableArray readableArray) {
    int size = readableArray.size();
    List<Region> regions = new ArrayList<Region>();
    for (int i = 0; i < size; i++) {
      regions.add(toRegion(readableArray.getMap(i)));
    }

    return regions;
  }

  static Region toRegion(ReadableMap readableMap) {
    return new CircularRegion.Builder()
        .setUniqueId(getString(readableMap, "uniqueId"))
        .build();
  }

  static Location toLocation(ReadableMap readableMap) {
    Location location = new Location("react-native");

    Double latitude = getLatitude(readableMap, "latitude");
    if (latitude != null) {
      location.setLatitude(latitude);
    } else {
      return null;
    }

    Double longitude = getLongitude(readableMap, "longitude");
    if (longitude != null) {
      location.setLongitude(longitude);
    } else {
      return null;
    }

    location.setAltitude(getDouble(readableMap, "altitude"));
    location.setTime(getLong(readableMap, "time"));
    location.setAccuracy(getFloat(readableMap, "horizontalAccuracy"));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      location.setVerticalAccuracyMeters(getFloat(readableMap, "verticalAccuracy"));
    }
    location.setSpeed(getFloat(readableMap, "speed"));
    location.setBearing(getFloat(readableMap, "direction"));
    return location;
  }

  static Region.Event toEvent(String event) {
    if ("enter".equalsIgnoreCase(event)) {
      return Region.Event.ENTER;
    } else {
      return Region.Event.EXIT;
    }
  }

  static WritableArray toInboxCampaignsWritableArray(List<InboxCampaign> campaigns) {
    WritableArray writableArray = Arguments.createArray();
    for (InboxCampaign campaign : campaigns) {
      writableArray.pushMap(toWritableMap(campaign));
    }

    return writableArray;
  }

  static WritableMap toWritableMap(InboxCampaign campaign) {
    WritableMap writableMap = Arguments.createMap();

    // Campaign
    writableMap.putInt("campaignId", (int) campaign.getCampaignId());
    writableMap.putString("name", campaign.getName());
    writableMap.putMap("attributes", toWritableMap(campaign.getAttributes()));

    // WebViewCampaign
    Uri creativeFilePath = campaign.getCreativeFilePath();
    writableMap.putString("creativeFilePath", creativeFilePath != null ? creativeFilePath.toString() : "");

    // InboxCampaign
    writableMap.putBoolean("read", campaign.isRead());
    writableMap.putString("title", campaign.getTitle());
    writableMap.putInt("sortOrder", (int) campaign.getSortOrder());
    writableMap.putInt("receivedDate", (int) (campaign.getReceivedDate().getTime() / 1000));
    writableMap.putString("summary", campaign.getSummary());
    writableMap.putBoolean("hasThumbnail", campaign.hasThumbnail());
    Uri thumbnailUri = campaign.getThumbnailUri();
    writableMap.putString("thumbnailUrl", thumbnailUri != null ? thumbnailUri.toString() : "");
    writableMap.putBoolean("hasCreative", campaign.hasCreative());
    writableMap.putBoolean("visible", campaign.isVisible());
    writableMap.putBoolean("pushToInboxCampaign", campaign.isPushToInboxCampaign());
    writableMap.putString("deeplinkUrl", campaign.getDeepLinkUrl());
    writableMap.putBoolean("deleted", campaign.isDeleted());

    return writableMap;
  }

  static WritableMap toWritableMap(android.location.Location location) {
    WritableMap writableMap = Arguments.createMap();
    writableMap.putDouble("latitude", location.getLatitude());
    writableMap.putDouble("longitude", location.getLongitude());
    writableMap.putDouble("altitude", location.getAltitude());
    writableMap.putInt("time", (int) (location.getTime() / 1000));
    writableMap.putDouble("accuracy", location.getAccuracy());

    return writableMap;
  }

  static WritableMap toWritableMap(Campaign campaign) {
    if (campaign instanceof PlacesCampaign) {
      return toWritableMap((PlacesCampaign) campaign);
    } else if (campaign instanceof InboxCampaign) {
      return toWritableMap((InboxCampaign) campaign);
    } else if (campaign instanceof InAppCampaign) {
      return toWritableMap((InAppCampaign) campaign);
    } else if (campaign instanceof PushCampaign) {
      return toWritableMap((PushCampaign) campaign);
    }
    return null; //should never happen.
  }

  static WritableMap toWritableMap(InAppCampaign campaign) {
    WritableMap writableMap = Arguments.createMap();

    // Campaign
    writableMap.putInt("campaignId", (int) campaign.getCampaignId());
    writableMap.putString("name", campaign.getName());
    writableMap.putMap("attributes", toWritableMap(campaign.getAttributes()));

    // WebViewCampaign
    Uri creativeFilePath = campaign.getCreativeFilePath();
    writableMap.putString("creativeFilePath", creativeFilePath != null ? creativeFilePath.toString() : "");

    // InAppCampaign
    if (!Double.isNaN(campaign.getAspectRatio())) {
      writableMap.putDouble("aspectRatio", campaign.getAspectRatio());
    }
    writableMap.putInt("bannerOffsetDps", campaign.getOffset());
    writableMap.putDouble("backgroundAlpha", campaign.getBackgroundAlpha());
    writableMap.putString("displayLocation", campaign.getDisplayLocation());
    writableMap.putBoolean("dismissButtonHidden", campaign.isDismissButtonHidden());
    if (Localytics.InAppMessageDismissButtonLocation.RIGHT.equals(campaign.getDismissButtonLocation())) {
      writableMap.putString("dismissButtonLocation", "right");
    } else {
      writableMap.putString("dismissButtonLocation", "left");
    }
    writableMap.putString("eventName", campaign.getEventName());
    writableMap.putMap("eventAttributes", toWritableMap(campaign.getEventAttributes()));

    return writableMap;
  }

  static WritableMap toWritableMap(PushCampaign campaign) {
    WritableMap writableMap = Arguments.createMap();

    // Campaign
    writableMap.putInt("campaignId", (int) campaign.getCampaignId());
    writableMap.putString("name", campaign.getName());
    writableMap.putMap("attributes", toWritableMap(campaign.getAttributes()));

    // PushCampaign
    writableMap.putString("title", campaign.getTitle());
    writableMap.putInt("creativeId", (int) campaign.getCreativeId());
    writableMap.putString("creativeType", campaign.getCreativeType());
    writableMap.putString("message", campaign.getMessage());
    writableMap.putString("soundFilename", campaign.getSoundFilename());
    writableMap.putString("attachmentUrl", campaign.getAttachmentUrl());

    return writableMap;
  }

  static WritableMap toWritableMap(PlacesCampaign campaign) {
    WritableMap writableMap = Arguments.createMap();

    // Campaign
    writableMap.putInt("campaignId", (int) campaign.getCampaignId());
    writableMap.putString("name", campaign.getName());
    writableMap.putMap("attributes", toWritableMap(campaign.getAttributes()));

    // PlacesCampaign
    writableMap.putString("title", campaign.getTitle());
    writableMap.putInt("creativeId", (int) campaign.getCreativeId());
    writableMap.putString("creativeType", campaign.getCreativeType());
    writableMap.putString("message", campaign.getMessage());
    writableMap.putString("soundFilename", campaign.getSoundFilename());
    writableMap.putString("attachmentUrl", campaign.getAttachmentUrl());
    writableMap.putMap("region", toWritableMap((CircularRegion) campaign.getRegion()));
    if (Region.Event.ENTER.equals(campaign.getTriggerEvent())) {
      writableMap.putString("triggerEvent", "enter");
    } else {
      writableMap.putString("triggerEvent", "exit");
    }

    return writableMap;
  }

  static List<String> toStringList(ReadableArray array) {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < array.size(); i++) {
      result.add(array.getString(i));
    }

    return result;
  }
}
