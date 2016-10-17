package bg.devlabs.photowatchface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class WatchFaceFolderService extends CanvasWatchFaceService {
    private static final long UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new WatchFaceFolderService.Engine(mInflater);
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        Paint mDigitalPaint;
        Paint mDigitalPaintOuter;
        boolean mMute;
        Time mTime;
        private GoogleApiClient mGoogleApiClient;
        LayoutInflater mInflater;
        RelativeLayout mFrameLayout;
        TextView timeTextView;
        TextView dateTextView;
        static final String COLON_STRING = ":";
        Calendar mCalendar;
        Date mDate;
        boolean mShouldDrawColons;
        java.text.DateFormat mDateFormat;

        public Engine(LayoutInflater mInflater) {
            this.mInflater = mInflater;
        }

        private void initFormats() {
            mDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            mDateFormat.setCalendar(mCalendar);
        }

        static final int MESSAGE_ID_UPDATE_TIME = 1000;
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MESSAGE_ID_UPDATE_TIME:
                        invalidate();
                        if (isVisible() && !isInAmbientMode()) {
                            long delay = UPDATE_INTERVAL
                                    - (System.currentTimeMillis() % UPDATE_INTERVAL);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MESSAGE_ID_UPDATE_TIME, delay);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        private int mTextSize = 24;
        private float mStrokeWidth = 32.0f;
        private int mTextColor = 0xffffffff;
        private int mTextColorOuter = 0xff000000;
        private float offsetx = (float) (-50 + 100 * Math.random());
        private float offsety = (float) (-50 + 100 * Math.random());

        boolean mLowBitAmbient = false;
        final int[] mBackgroundIDs = {R.drawable.image, R.drawable.image2, R.drawable.image};
        Bitmap mBG;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mFrameLayout = (RelativeLayout) mInflater.inflate(R.layout.main, null);
            timeTextView = (TextView) mFrameLayout.findViewById(R.id.time_text_view);
            dateTextView = (TextView) mFrameLayout.findViewById(R.id.date_text_view);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceFolderService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            // randomly pick one of the three photos by Chris Blunt
            mBG = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    mBackgroundIDs[(int) (mBackgroundIDs.length * Math.random())]), 320, 320, false);

            mDigitalPaint = new Paint();
            mDigitalPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mDigitalPaint.setTextSize(mTextSize);
            mDigitalPaint.setStrokeWidth(mStrokeWidth);
            mDigitalPaint.setColor(mTextColor);
            mDigitalPaint.setStyle(Paint.Style.FILL);
            mDigitalPaint.setAntiAlias(true);

            mDigitalPaintOuter = new Paint();
            mDigitalPaintOuter.setTypeface(Typeface.DEFAULT_BOLD);
            mDigitalPaintOuter.setTextSize(mTextSize);
            mDigitalPaintOuter.setStrokeWidth(mStrokeWidth);
            mDigitalPaintOuter.setColor(mTextColorOuter);
            mDigitalPaintOuter.setStyle(Paint.Style.FILL);
            mDigitalPaintOuter.setAntiAlias(true);

            mTime = new Time();

            mGoogleApiClient = new GoogleApiClient.Builder(WatchFaceFolderService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mCalendar = Calendar.getInstance();
            mDate = new Date();
            initFormats();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mLowBitAmbient) {
                mDigitalPaint.setAntiAlias(!inAmbientMode);
                mDigitalPaintOuter.setAntiAlias(!inAmbientMode);
            }

            invalidate();
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mDigitalPaint.setAlpha(inMuteMode ? 80 : 255);
                mDigitalPaintOuter.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            //Measure the view at the exact dimensions (otherwise the text won't center correctly)
            int widthSpec = View.MeasureSpec.makeMeasureSpec(bounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY);
            mFrameLayout.measure(widthSpec, heightSpec);

            //Lay the view out at the rect width and height
            mFrameLayout.layout(0, 0, bounds.width(), bounds.height());

            mFrameLayout.draw(canvas);

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            mDate.setTime(now);
            // Show colons for the first half of each second so the colons blink on when the time
            // updates.
            mShouldDrawColons = (System.currentTimeMillis() % 1000) < 500;

            //Time
            Date date = mCalendar.getTime();
            SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm");
            String stringDate = simpleDate.format(date);
            timeTextView.setText(stringDate);
            // Date
            dateTextView.setText(mDateFormat.format(mDate));
            // draw the background image
            if (mBG == null || mBG.getWidth() != bounds.width() || mBG.getHeight() != bounds.height())
                mBG = Bitmap.createScaledBitmap(mBG, bounds.width(), bounds.height(), false);
            mFrameLayout.setBackground(new BitmapDrawable(mBG));
//            canvas.drawBitmap(mBG, 0, 0, null);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                mGoogleApiClient.connect();
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
                releaseGoogleApiClient();
            }

            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver)
                return;

            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFaceFolderService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver)
                return;

            mRegisteredTimeZoneReceiver = false;
            WatchFaceFolderService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MESSAGE_ID_UPDATE_TIME);

            if (isVisible() && !isInAmbientMode())
                mUpdateTimeHandler.sendEmptyMessage(MESSAGE_ID_UPDATE_TIME);
        }

        private void releaseGoogleApiClient() {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(mGoogleApiClient, onDataChangedListener);
                mGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(onConnectedResultCallback);
        }

        private void updateAssetForDataItem(DataItem item) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

            if (dataMap.containsKey("assetbody")) {
                Asset asset = dataMap.getAsset("assetbody");

                if (asset == null)
                    return;

                ConnectionResult cr = mGoogleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
                if (!cr.isSuccess())
                    return;

                InputStream is = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();
                mGoogleApiClient.disconnect();

                if (is == null)
                    return;

                if (mBG != null) {
                    mBG.recycle();
                    mBG = null;
                }
                mBG = BitmapFactory.decodeStream(is);

                invalidate();
            }
        }

        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        if (item.getUri().getPath().equals("/image")) {
                            Log.d("TestLog", "onDataChanged: /image");
                            updateAssetForDataItem(item);
                        } else {
                            Log.d("TestLog", "onDataChanged:(( " +item.getUri().getPath());
                        }
                    }
                }

                dataEvents.release();
                if (isVisible() && !isInAmbientMode()) {
                    invalidate();
                }
            }
        };

        private final ResultCallback<DataItemBuffer> onConnectedResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
//                for (DataItem item : dataItems) {
//                    //updateParamsForDataItem(item);
//                }
//
                dataItems.release();
//                if (isVisible() && !isInAmbientMode()) {
//                    invalidate();
//                }
            }
        };

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MESSAGE_ID_UPDATE_TIME);
            releaseGoogleApiClient();
            super.onDestroy();
        }

    }
}
