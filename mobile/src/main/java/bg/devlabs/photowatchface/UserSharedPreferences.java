package bg.devlabs.photowatchface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by Simona Stoyanova on 27.7.2016 г..
 * Dev Labs
 * simona@devlabs.bg
 * <p>
 * Simple class to help us handle the shared preference storage
 * <p>
 * All names will start with the "shared_" prefix
 * for example shared_native_lang_code
 * <p>
 * The string resources will be saved in CamelCase
 * for example SharedNativeLangCode
 * <p>
 * All deafult values will be reused by type
 * string="", int = 0, etc
 * for example shared_default_string_value
 */
public class UserSharedPreferences {
    public static final String preferenceFileKey = "bg.devlabs.lingozing.PREFERENCE_FILE_KEY";
    private static UserSharedPreferences instance = null;
    private SharedPreferences sharedPreferences;
    private Context context;

    private UserSharedPreferences() {
    }

    public static UserSharedPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new UserSharedPreferences();
            instance.context = context;
            instance.setSharedPreferences(context.getSharedPreferences(
                    preferenceFileKey, Context.MODE_PRIVATE));
        }
        return instance;
    }


    private SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String readImagesPath() {
        String defaultPaht = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera/";;
        return sharedPreferences.getString(context.getString(R.string.shared_images_path), defaultPaht);
    }

    @SuppressLint("CommitPrefEdits")
    public void saveImagesPath(String nativeLanguage) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(context.getString(R.string.shared_images_path), nativeLanguage);
        editor.commit();
    }
}
