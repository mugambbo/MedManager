package mugambbo.github.com.medmanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Abdulmajid on 4/10/18.
 */

public class SharedPreferencesUtil {

    public static SharedPreferencesUtil sSharedPreferencesUtil;

    private static SharedPreferences sSharedPreferences;

    public SharedPreferencesUtil(Context context) {
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized SharedPreferencesUtil getInstance(Context context){
        if (sSharedPreferencesUtil == null){
            sSharedPreferencesUtil = new SharedPreferencesUtil(context);
        }
        return sSharedPreferencesUtil;
    }

    public void putString(String key, String val){
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public String getString(String key, String defaultVal){
        return sSharedPreferences.getString(key, defaultVal);
    }
}
