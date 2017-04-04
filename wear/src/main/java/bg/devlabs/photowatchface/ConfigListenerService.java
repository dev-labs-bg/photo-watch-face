/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bg.devlabs.photowatchface;

import com.google.android.gms.wearable.WearableListenerService;

/**
 * A {@link WearableListenerService} listening for {@link DigitalWatchFaceService} config messages
 * and updating the config {@link com.google.android.gms.wearable.DataItem} accordingly.
 */
public class ConfigListenerService {// extends WearableListenerService
//        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
//    private static final String TAG = "DigitalListenerService";
//    private static final long TIMEOUT_MS = 1000;
//
//    private GoogleApiClient mGoogleApiClient;
//
//    @Override // WearableListenerService
//    public void onMessageReceived(MessageEvent messageEvent) {
//
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onMessageReceived: " + messageEvent);
//        }
//
//        if (!messageEvent.getPath().equals(DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
//            return;
//        }
//        byte[] rawData = messageEvent.getData();
//        // It's allowed that the message carries only some of the keys used in the config DataItem
//        // and skips the ones that we don't want to change.
//        DataMap configKeysToOverwrite = DataMap.fromByteArray(rawData);
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "Received watch face config message: " + configKeysToOverwrite);
//        }
//
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
//        }
//        if (!mGoogleApiClient.isConnected()) {
//            ConnectionResult connectionResult =
//                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//
//            if (!connectionResult.isSuccess()) {
//                Log.e(TAG, "Failed to connect to GoogleApiClient.");
//                return;
//            }
//        }
//
//        DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
//    }
//
//    @Override // GoogleApiClient.ConnectionCallbacks
//    public void onConnected(Bundle connectionHint) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onConnected: " + connectionHint);
//        }
//    }
//
//    @Override  // GoogleApiClient.ConnectionCallbacks
//    public void onConnectionSuspended(int cause) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onConnectionSuspended: " + cause);
//        }
//    }
//
//    @Override  // GoogleApiClient.OnConnectionFailedListener
//    public void onConnectionFailed(ConnectionResult result) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onConnectionFailed: " + result);
//        }
//    }
//
//
//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        Log.d("TestLog", "onDataChanged: dataEvents ConfigListenerService");
//        for (DataEvent dataEvent : dataEvents) {
//            if (dataEvent.getDataItem().getUri().getPath().equals("/image")) {
//                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
//                Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
//                ConfigListenerService listenerService = new ConfigListenerService();
//                Bitmap bitmap = listenerService.loadBitmapFromAsset(profileAsset);
//                //mFrameLayout.setBackground(new BitmapDrawable(bitmap));
//            }
//        }
//    }
//
//
//    public Bitmap loadBitmapFromAsset(Asset asset) {
//        if (asset == null) {
//            throw new IllegalArgumentException("Asset must be non-null");
//        }
//        ConnectionResult result =
//                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
//        if (!result.isSuccess()) {
//            return null;
//        }
//        // convert asset into a file descriptor and block until it's ready
//        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                mGoogleApiClient, asset).await().getInputStream();
//        mGoogleApiClient.disconnect();
//
//        if (assetInputStream == null) {
//            Log.w(TAG, "Requested an unknown Asset.");
//            return null;
//        }
//        // decode the stream into a bitmap
//        return BitmapFactory.decodeStream(assetInputStream);
//    }
}
