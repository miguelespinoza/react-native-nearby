package com.miguele.nearby;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

/**
 * Created by miguele on 6/29/17.
 */

public class NearbyModule extends ReactContextBaseJavaModule implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LifecycleEventListener {

    public static final String TAG = "NearbyModule";

    private MessageListener mMessageListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private ReactContext mReactContext = null;
    private Message mActiveMessage = null;
    private String publishMessage = "";

    private static final int TTL_IN_SECONDS = 3 * 60; // Three minutes.
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    @Override
    public void onHostResume() {
        Log.i(TAG, "onHostResume");
    }

    @Override
    public void onHostPause() {
        Log.i(TAG, "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        Log.i(TAG, "onHostDestroy");
        nearbyUnpublish();
        nearbyUnsubscribe();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "on Nearby Connected");

        nearbyPublish(this.publishMessage);
        nearbySubscribe();
        emitSubscription("'onConnected'", "Event Emitter is at a healthy start");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "on Nearby Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "on Nearby Connection Failed: " + connectionResult.toString());
    }

    public NearbyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Nearby";
    }

    private synchronized void buildGoogleApiClient()
    {
        if (mGoogleApiClient == null) {
//            if (ContextCompat.checkSelfPermission(this.getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getReactApplicationContext())
                    .addApi(Nearby.MESSAGES_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .enableAutoManage((FragmentActivity) this.getCurrentActivity(), this)
                    .build();
//            }
        }
    }

    /**
     * init: Not sure how likely this will be used,
     * if anyone is interested shoot a message or pr.
     * @param publishObject
     */
    @ReactMethod
    public void init(Object publishObject) {

    }

    @ReactMethod
    public void init(String publishMessage) {
        Log.i(TAG, "Nearby init: " + publishMessage);
        mReactContext = this.getReactApplicationContext();
        this.publishMessage = publishMessage;
        this.mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String messageAsString = new String(message.getContent());
                emitSubscription("onFound", messageAsString);
                Log.d(TAG, "Found message: " + messageAsString);
            }

            @Override
            public void onLost(Message message) {
                String messageAsString = new String(message.getContent());
                emitSubscription("onLost", messageAsString);
                Log.d(TAG, "Lost sight of message: " + messageAsString);
            }
        };

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private void nearbyPublish(String message) {
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                    }
                }).build();

        Log.i(TAG, "Publishing message: " + message);
        mActiveMessage = new Message(message.getBytes());
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Published successfully.");
                            emitSubscription("publish.onResult",  "Published successfully.");
                        } else {
                            Log.i(TAG, "Could not publish, status = " + status);
                            emitSubscription("publish.onResultErr",  "Could not publish, status = " + status);
                        }
                    }
                });

        Log.i(TAG, "Publish was successful");
    }

    private void nearbyUnpublish() {
        Log.i(TAG, "Unpublishing.");
        if (mActiveMessage != null) {
            Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage);
            mActiveMessage = null;
        }
    }

    private void nearbySubscribe() {
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");
                    }
                }).build();

        Log.i(TAG, "Subscribing.");
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Subscribed successfully.");
                            emitSubscription("subscribe.onResult",  "Subscribed successfully.");
                        } else {
                            Log.i(TAG, "Could not subscribe, status = " + status);
                            emitSubscription("subscribe.onResultErr",  "Could not subscribe, status = " + status);
                        }
                    }
                });
    }

    private void nearbyUnsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    private void emitSubscription(String method, String message) {
        WritableMap params = Arguments.createMap();
        params.putString("method", method);
        params.putString("message", message);
        ReactNativeEventUtil.sendEvent(mReactContext, "nearbySubscribe", params);
    }
}
