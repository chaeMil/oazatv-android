package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SharedPrefUtils sharedPrefUtils;

    public static final String APPLICATION_PREFERENCES = "com.chaemil.hgms.preferences";
    public static final String PREFERENCES_TOKEN = "token";
    private static final String PREFERENCES_NICK = "nick";
    private static final String PREFERENCES_EMAIL = "email";

    public static SharedPrefUtils getInstance(Context context) {
        if (sharedPrefUtils == null) {
            sharedPrefUtils = new SharedPrefUtils(context);
        }
        return sharedPrefUtils;
    }

    private SharedPrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(
                APPLICATION_PREFERENCES,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString(PREFERENCES_TOKEN, token);
        editor.commit();
    }

    public String loadToken() {
        return sharedPreferences.getString(PREFERENCES_TOKEN, null);
    }

    public void saveNick(String nick) {
        editor.putString(PREFERENCES_NICK, nick);
        editor.commit();
    }

    public String loadNick() {
        return sharedPreferences.getString(PREFERENCES_NICK, null);
    }

    public void saveEmail(String email) {
        editor.putString(PREFERENCES_EMAIL, email);
        editor.commit();
    }

    public String loadEmail() {
        return sharedPreferences.getString(PREFERENCES_EMAIL, null);
    }


    public void deleteUserData() {
        editor.remove(PREFERENCES_EMAIL);
        editor.remove(PREFERENCES_NICK);
        editor.remove(PREFERENCES_TOKEN);
        editor.commit();
    }
}
