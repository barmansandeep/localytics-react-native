
package com.localytics.react.android;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.localytics.android.*;

public class LLLocalyticsPackage implements ReactPackage {
    static {
        Localytics.setOption("plugin_library", "RN_2.3.1");
    }

    private LLLocalyticsModule localyticsModule;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
      return Collections.<NativeModule>singletonList(getLocalyticsModule(reactContext));
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.<ViewManager>singletonList(new LLWebViewManager(getLocalyticsModule(reactContext)));
    }

    @NonNull
    private LLLocalyticsModule getLocalyticsModule(ReactApplicationContext reactContext) {
        if (localyticsModule == null) {
            localyticsModule = new LLLocalyticsModule(reactContext);
        }
        return localyticsModule;
    }
}
