package com.doochik.RNAppMetrica;

import android.content.Intent;
import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.lang.Exception;
import org.json.JSONObject;

import com.facebook.react.bridge.ReadableType;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.GenderAttribute;
import com.yandex.metrica.profile.UserProfile;
import com.yandex.metrica.profile.UserProfileUpdate;

public class AppMetricaModule extends ReactContextBaseJavaModule {
    final static String ModuleName = "AppMetrica";
    static ReactApplicationContext reactApplicationContext;
    static ReadableMap activateParams = null;
    static String activateKey = null;

    public AppMetricaModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
    }

    @Override
    public String getName() {
        return ModuleName;
    }


    public static void reportAppOpen(@NonNull Intent intent) {
        if (activateMetrics()) {
            YandexMetrica.reportAppOpen(intent);
        }
    }

    public static void reportAppOpen(@NonNull Activity activity) {
        if (activateMetrics()) {
            YandexMetrica.reportAppOpen(activity);
        }
    }

    public static void reportAppOpen(@NonNull String deeplink) {
        if (activateMetrics()) {
            YandexMetrica.reportAppOpen(deeplink);
        }
    }

    private static boolean activateMetrics() {
        if (activateParams != null) {
            staticActivateWithConfig(activateParams);
            return true;
        } else if (activateKey != null) {
            staticActivateWithApiKey(activateKey);
            return true;
        }

        return false;
    }

    private static void staticActivateWithApiKey(String key) {
        activateKey = key;
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(key);
        YandexMetrica.activate(reactApplicationContext.getApplicationContext(), configBuilder.build());
        Activity activity = reactApplicationContext.getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    private static void staticActivateWithConfig(ReadableMap params) {
        activateParams = params;
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(params.getString("apiKey"));
        if (params.hasKey("sessionTimeout")) {
            configBuilder.withSessionTimeout(params.getInt("sessionTimeout"));
        }
        if (params.hasKey("firstActivationAsUpdate")) {
            configBuilder.handleFirstActivationAsUpdate(params.getBoolean("firstActivationAsUpdate"));
        }
        YandexMetrica.activate(reactApplicationContext.getApplicationContext(), configBuilder.build());
        Activity activity = reactApplicationContext.getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    @ReactMethod
    public void activateWithApiKey(String key) {
        activateKey = key;
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(key);
        YandexMetrica.activate(getReactApplicationContext().getApplicationContext(), configBuilder.build());
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    @ReactMethod
    public void activateWithConfig(ReadableMap params) {
        activateParams = params;
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(params.getString("apiKey"));
        if (params.hasKey("sessionTimeout")) {
            configBuilder.withSessionTimeout(params.getInt("sessionTimeout"));
        }
        if (params.hasKey("firstActivationAsUpdate")) {
            configBuilder.handleFirstActivationAsUpdate(params.getBoolean("firstActivationAsUpdate"));
        }
        YandexMetrica.activate(getReactApplicationContext().getApplicationContext(), configBuilder.build());
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    @ReactMethod
    public void reportError(String message) {
        try {
            Integer.valueOf("00xffWr0ng");
        } catch (Throwable error) {
            YandexMetrica.reportError(message, error);
        }
    }

    @ReactMethod
    public void reportEvent(String message, @Nullable ReadableMap params) {
        if (params != null) {
            YandexMetrica.reportEvent(message, convertReadableMapToJson(params));
        } else {
            YandexMetrica.reportEvent(message);
        }
    }

    @ReactMethod
    public void setUserProfileID(String profileID) {
        YandexMetrica.setUserProfileID(profileID);
    }

    private String convertReadableMapToJson(final ReadableMap readableMap) {
		ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        JSONObject json = new JSONObject();

        try {
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();

                switch (readableMap.getType(key)) {
                    case Null:
                        json.put(key, null);
                        break;
                    case Boolean:
                        json.put(key, readableMap.getBoolean(key));
                        break;
                    case Number:
                        json.put(key, readableMap.getDouble(key));
                        break;
                    case String:
                        json.put(key, readableMap.getString(key));
                        break;
                    case Array:
                        json.put(key, readableMap.getArray(key));
                        break;
                    case Map:
                        json.put(key, convertReadableMapToJson(readableMap.getMap(key)));
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            Log.d(ModuleName, "convertReadableMapToJson fail: " + ex);
        }

        return json.toString();
    }

    @ReactMethod
    public void reportUserProfile(String userProfileID, ReadableMap userProfileParam, Promise promise) {
        if(userProfileID == null) {
            promise.reject("-101", "UserProfileId can't be null");
        }

        setUserProfileID(userProfileID);

        if(userProfileParam != null) {
            UserProfile.Builder userProfileBuilder = UserProfile.newBuilder();
            ReadableMapKeySetIterator iterator = userProfileParam.keySetIterator();

            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();

                switch (key) {
                    case "name": {
                        UserProfileUpdate name = Attribute.name().withValue(userProfileParam.getString(key));
                        userProfileBuilder.apply(name);
                        break;
                    }
                    case "gender": {
                        String genderProp = userProfileParam.getString(key);

                        if(genderProp.equalsIgnoreCase("male")) {
                            UserProfileUpdate gender = Attribute.gender().withValue(GenderAttribute.Gender.MALE);
                            userProfileBuilder.apply(gender);
                        } else if(genderProp.equalsIgnoreCase("female")) {
                            UserProfileUpdate gender = Attribute.gender().withValue(GenderAttribute.Gender.FEMALE);
                            userProfileBuilder.apply(gender);
                        } else {
                            UserProfileUpdate gender = Attribute.gender().withValue(GenderAttribute.Gender.OTHER);
                            userProfileBuilder.apply(gender);
                        }

                        break;
                    }
                    case "birthDate": {
                        UserProfileUpdate birthDate = Attribute.birthDate().withAge(userProfileParam.getInt(key));
                        userProfileBuilder.apply(birthDate);
                        break;
                    }
                    case "notificationsEnabled": {
                        UserProfileUpdate notificationsEnabled = Attribute.notificationsEnabled().withValue(userProfileParam.getBoolean(key));
                        userProfileBuilder.apply(notificationsEnabled);
                        break;
                    }
                    default: {
                        ReadableType keyType = userProfileParam.getType(key);
                        UserProfileUpdate customAttribute = null;

                        if(keyType == ReadableType.String) {
                            customAttribute = Attribute.customString(key).withValue(userProfileParam.getString(key));
                        } else if(keyType == ReadableType.Number) {
                            customAttribute = Attribute.customNumber(key).withValue(userProfileParam.getInt(key));
                        } else if(keyType == ReadableType.Boolean) {
                            customAttribute = Attribute.customBoolean(key).withValue(userProfileParam.getBoolean(key));
                        }

                        if(customAttribute != null) {
                            userProfileBuilder.apply(customAttribute);
                        }

                        break;
                    }
                }
            }

            UserProfile userProfile = userProfileBuilder.build();

            if(userProfile.getUserProfileUpdates().size() > 0) {
                YandexMetrica.reportUserProfile(userProfile);

                promise.resolve(true);
            } else {
                promise.reject("-102", "Valid keys not found");
            }
        }
    }
}
