package patelheggere.com.locationtracking.helper;

import android.content.Context;
import android.content.SharedPreferences;

import patelheggere.com.locationtracking.BaseApp;

/**
 * Created by Patel Heggere on 3/28/2018.
 */

public class SharedPreferenceHelper {

    private static final String TAG = "SharedPrefsHelper";

    private static final String SHARED_PREFS_NAME = "LocationTrack";

    private static SharedPreferenceHelper instance;

    private SharedPreferences sharedPreferences;

    public static synchronized SharedPreferenceHelper getInstance() {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
        }
        return instance;
    }

    private SharedPreferenceHelper() {
        instance = this;
        sharedPreferences = BaseApp.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void delete(String key) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit();
        }
    }

    public void save(String key, Object value) {
        SharedPreferences.Editor editor = getEditor();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (int) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Enum) {
            editor.putString(key, value.toString());
        } else if (value != null) {

            throw new RuntimeException("Attempting to save non-supported preference");
        }

        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public boolean has(String key) {
        return sharedPreferences.contains(key);
    }

    public void clearAllData() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear().commit();
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

}

