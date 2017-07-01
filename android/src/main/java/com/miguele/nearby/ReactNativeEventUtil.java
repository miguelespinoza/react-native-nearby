package com.miguele.nearby;

import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by miguele on 6/29/17.
 */

public class ReactNativeEventUtil {
    private static final String TAG = "ReactNativeEventUtil";


    /**
     * thanks to react-native-socketio:
     * https://github.com/gcrabtree/react-native-socketio/blob/master/android/src/main/java/com/gcrabtree/rctsocketio/ReactNativeEventUtil.java
     *
     * Send the event back so that our javascript code can listen using the DeviceEventEmitter
     * https://facebook.github.io/react-native/docs/native-modules-android.html#content
     * @param reactContext
     * @param eventName
     * @param params
     */
    public static void sendEvent(ReactContext reactContext, String eventName, Object params) {
        if (reactContext != null) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } else {
            Log.e(TAG, "Could not submit event for a null context...");
        }
    }
}
