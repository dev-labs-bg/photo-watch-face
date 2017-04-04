package bg.devlabs.photowatchface;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Simona Stoyanova on 13.10.2016 г..
 * Dev Labs
 * simona@devlabs.bg
 */
class SimpleImageAdapter extends RecyclerView.Adapter<SimpleImageAdapter.ViewHolder> {
    private static final int MAX_IMAGES_SHOWING = 30;
    private Activity activity;
    private String directoryPath = "";
    private List<String> imagePaths;
    private GoogleApiClient mGoogleApiClient;
    private boolean computing = false;

    SimpleImageAdapter(String directoryPath, Activity activity, GoogleApiClient mGoogleApiClient) {
        this.directoryPath = directoryPath;
        this.mGoogleApiClient = mGoogleApiClient;
        this.activity = activity;
        fillImagePaths();
    }

    void setImagePaths(String directoryPath) {
        this.directoryPath = directoryPath;
        fillImagePaths();
        notifyDataSetChanged();
    }

    private void fillImagePaths() {
        imagePaths = new ArrayList<>();
        File dir = new File(directoryPath);
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });
        if (foundFiles != null && foundFiles.length > 0) {
            int max = foundFiles.length;
            if (max > MAX_IMAGES_SHOWING) {
                max = MAX_IMAGES_SHOWING;
            }
            for (int i = 0; i < max; i++) {
                File file = foundFiles[i];
                String name = file.getName();
                imagePaths.add(name);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View root = inflater.inflate(R.layout.image_layout, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        final String imagePath = directoryPath + imagePaths.get(i);
        File file = new File(imagePath);
        Glide.with(activity)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(computing){
                    Toast.makeText(activity, "Please wait", Toast.LENGTH_SHORT).show();
                    return;
                }
                computing = true;
                Log.d("TestLog", "onClick: imageview");
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                //Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.image);
                Asset asset = createAssetFromBitmap(bitmap);

//                PutDataRequest request = PutDataRequest.create("/image");
//                request.putAsset("profileImage", asset);
//                Wearable.DataApi.putDataItem(mGoogleApiClient, request);

                PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
                dataMap.getDataMap().putAsset("profileImage", asset);
                PutDataRequest request = dataMap.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        computing = false;
                        // something
                    }
                } );
            }
        });
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        Log.d("TestLog", "createAssetFromBitmap: ");
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Log.d("TestLog", "bitmap.compress");
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'card_order.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view)
        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
