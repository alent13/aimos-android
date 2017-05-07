package com.applexis.aimos_android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author applexis
 */

public class SharedPreferencesHelper {

    public static final String APP_PREFERENCES = "aimos_settings";
    public static final String APP_PREFERENCES_ID = "aimos_id";
    public static final String APP_PREFERENCES_LOGIN = "aimos_login";
    public static final String APP_PREFERENCES_NAME = "aimos_name";
    public static final String APP_PREFERENCES_SURNAME = "aimos_surname";
    public static final String APP_PREFERENCES_TOKEN = "aimos_token";
    public static final String APP_PREFERENCES_GLOBAL_AES_KEY = "aimos_global_aes_key";
    public static final String APP_PREFERENCES_GLOBAL_PUBLIC_KEY = "aimos_global_public_key";
    public static final String APP_PREFERENCES_GLOBAL_PRIVATE_KEY = "aimos_global_private_key";

    private static Long id;
    private static String login;
    private static String name;
    private static String surname;
    private static String token;
    private static String globalAesKey;
    private static String globalPublicKey;
    private static String globalPrivateKey;

    private static SharedPreferences settings;

    public static void initialize(Context context) {
        settings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static Long getId() {
        if(id == null) {
            id = settings.getLong(APP_PREFERENCES_ID, 0);
        }
        return id;
    }

    public static void setId(Long id) {
        SharedPreferencesHelper.id = id;
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(APP_PREFERENCES_ID, id);
        editor.apply();
    }

    private static void setValue(String param, String login) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(param, login);
        editor.apply();
    }

    public static String getLogin() {
        if(login == null) {
            login = settings.getString(APP_PREFERENCES_LOGIN, "");
        }
        return login;
    }

    public static void setLogin(String login) {
        SharedPreferencesHelper.login = login;
        setValue(APP_PREFERENCES_LOGIN, login);
    }

    public static String getToken() {
        if(token == null) {
            token = settings.getString(APP_PREFERENCES_TOKEN, "");
        }
        return token;
    }

    public static void setToken(String token) {
        SharedPreferencesHelper.token = token;
        setValue(APP_PREFERENCES_TOKEN, token);
    }

    public static String getGlobalAesKey() {
        if(globalAesKey == null) {
            globalAesKey = settings.getString(APP_PREFERENCES_GLOBAL_AES_KEY, "");
        }
        return globalAesKey;
    }

    public static void setGlobalAesKey(String globalDesKey) {
        SharedPreferencesHelper.globalAesKey = globalDesKey;
        setValue(APP_PREFERENCES_GLOBAL_AES_KEY, globalDesKey);
    }

    public static String getGlobalPublicKey() {
        if(globalPublicKey == null) {
            globalPublicKey = settings.getString(APP_PREFERENCES_GLOBAL_PUBLIC_KEY, "");
        }
        return globalPublicKey;
    }

    public static void setGlobalPublicKey(String globalPublicKey) {
        SharedPreferencesHelper.globalPublicKey = globalPublicKey;
        setValue(APP_PREFERENCES_GLOBAL_PUBLIC_KEY, globalPublicKey);
    }

    public static String getGlobalPrivateKey() {
        if(globalPrivateKey == null) {
            globalPrivateKey = settings.getString(APP_PREFERENCES_GLOBAL_PRIVATE_KEY, "");
        }
        return globalPrivateKey;
    }

    public static void setGlobalPrivateKey(String globalPrivateKey) {
        SharedPreferencesHelper.globalPrivateKey = globalPrivateKey;
        setValue(APP_PREFERENCES_GLOBAL_PRIVATE_KEY, globalPrivateKey);
    }

    public static String getName() {
        if(name == null) {
            name = settings.getString(APP_PREFERENCES_NAME, "");
        }
        return name;
    }

    public static void setName(String name) {
        SharedPreferencesHelper.name = name;
        setValue(APP_PREFERENCES_NAME, name);
    }

    public static String getSurname() {
        if(surname == null) {
            surname = settings.getString(APP_PREFERENCES_SURNAME, "");
        }
        return surname;
    }

    public static void setSurname(String surname) {
        SharedPreferencesHelper.surname = surname;
        setValue(APP_PREFERENCES_SURNAME, surname);
    }
}
