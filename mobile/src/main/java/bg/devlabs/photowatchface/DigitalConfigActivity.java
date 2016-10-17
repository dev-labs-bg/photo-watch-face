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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.nononsenseapps.filepicker.FilePickerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The phone-side config activity for {@code DigitalWatchFaceService}. Like the watch-side config
 * activity ({@code DigitalWatchFaceWearableConfigActivity}), allows for setting the background
 * path. Additionally, enables setting the path for hour, minute and second digits.
 */
public class DigitalConfigActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = "DigitalWatchFaceConfig";
    String path;

    // TODO: use the shared constants (needs covering all the samples with Gradle build model)
    private static final String KEY_IMAGE_PATHS = "IMAGE_PATHS";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";
    private static final int FILE_CODE = 444444;
    @BindView(R.id.imagesRecyclerView)
    RecyclerView imagesRecyclerView;
    @BindView(R.id.path_text_view)
    TextView pathTextView;
    SimpleImageAdapter adapter;

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_watch_face_config);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: ButterKnife.bind");
        path = UserSharedPreferences.getInstance(this).readImagesPath();
        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Sets up selected items for all pickers according to given {@code config} and sets up their
     * item selection listeners.
     *
     * @param config the {@code DigitalWatchFaceService} config {@link DataMap}. If null, the
     *               default items are selected.
     */
    private void setUpAllPickers(DataMap config) {
        setUpRecyclerView(KEY_IMAGE_PATHS, config);
    }

    private void setUpRecyclerView(final String configKey, DataMap config) {
//        if (config != null) {
//            path = config.getString(configKey, "");
//            Log.d(TAG, "setUpRecyclerView: path= " + path);
//            if (path != null) {
//                pathTextView.setText(path);
//            }
//        } else {
//        Uri fileUri = Uri.parse("android.resource://com.example.android.wearable.watchface/"
//                + R.drawable.image);
        pathTextView.setText(path);
        pathTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This always works
                Intent i = new Intent(DigitalConfigActivity.this, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                // Configure initial directory by specifying a String.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM);
                startActivityForResult(i, FILE_CODE);
            }
        });
//        }

        imagesRecyclerView.setNestedScrollingEnabled(false);
        imagesRecyclerView.setHasFixedSize(false);
        StaggeredGridLayoutManager layoutManager
                = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        imagesRecyclerView.setLayoutManager(layoutManager);

        adapter = new SimpleImageAdapter(path, this, mGoogleApiClient);
        imagesRecyclerView.setAdapter(adapter);

        // public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        // final String pathName = (String) adapterView.getItemAtPosition(pos);
        // sendConfigUpdateMessage(configKey, Color.parseColor(pathName));
    }

    private void sendConfigUpdateMessage(String configKey, int path) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, path);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + Integer.toHexString(path));
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            path = uri.getPath() + "/";
            UserSharedPreferences.getInstance(this).saveImagesPath(path);
            adapter.setImagePaths(path);
            pathTextView.setText(path);
        }
    }
}
