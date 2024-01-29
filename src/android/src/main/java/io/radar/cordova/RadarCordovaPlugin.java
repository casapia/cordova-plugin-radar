package io.radar.cordova;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.radar.sdk.Radar;
import io.radar.sdk.RadarReceiver;
import io.radar.sdk.RadarTrackingOptions;
import io.radar.sdk.RadarTrackingOptions.RadarTrackingOptionsForegroundService;
import io.radar.sdk.RadarTripOptions;
import io.radar.sdk.model.RadarAddress;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarGeofence;
import io.radar.sdk.model.RadarPlace;
import io.radar.sdk.model.RadarTrip;
import io.radar.sdk.model.RadarUser;

public class RadarCordovaPlugin extends CordovaPlugin {
    private static RadarCordovaPlugin instance;
    private CallbackContext eventsCallbackContext;
    private CallbackContext locationCallbackContext;
    private CallbackContext clientLocationCallbackContext;
    private CallbackContext errorCallbackContext;
    private RadarCordovaReceiver receiver;
    private CallbackContext requestPermissionCallback;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private static String[] stringArrayForArray(JSONArray jsonArr) {
        if (jsonArr == null) {
            return null;
        }

        String[] arr = new String[jsonArr.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jsonArr.optString(i);
        }
        return arr;
    }

    private static Map<String, String> stringMapForJSONObject(JSONObject jsonObj) {
        try {
            if (jsonObj == null) {
                return null;
            }

            Map<String, String> stringMap = new HashMap<>();
            Iterator<String> keys = jsonObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                jsonObj.get(key);
                stringMap.put(key, jsonObj.getString(key));
            }
            return stringMap;
        } catch (JSONException j) {
            return null;
        }
    }

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) {
        try {
            switch (action) {
                case "initialize":
                    initialize(args, callbackContext);
                    break;
                case "setUserId":
                    setUserId(args, callbackContext);
                    break;
                case "getUserId":
                    getUserId(callbackContext);
                    break;
                case "setDescription":
                    setDescription(args, callbackContext);
                    break;
                case "getDescription":
                    getDescription(callbackContext);
                    break;
                case "setMetadata":
                    setMetadata(args, callbackContext);
                    break;
                case "getMetadata":
                    getMetadata(callbackContext);
                    break;
                case "setAnonymousTrackingEnabled":
                    setAnonymousTrackingEnabled(args, callbackContext);
                    break;
                case "getPermissionsStatus":
                    getPermissionsStatus(callbackContext);
                    break;
                case "requestPermissions":
                    requestPermissions(args, callbackContext);
                    break;
                case "requestPermissionsSync":
                    requestPermissionsSync(args, callbackContext);
                    break;
                case "getLocation":
                    getLocation(args, callbackContext);
                    break;
                case "trackOnce":
                    trackOnce(args, callbackContext);
                    break;
                case "startTrackingEfficient":
                    startTrackingEfficient(callbackContext);
                    break;
                case "startTrackingResponsive":
                    startTrackingResponsive(callbackContext);
                    break;
                case "startTrackingContinuous":
                    startTrackingContinuous(callbackContext);
                    break;
                case "startTrackingCustom":
                    startTrackingCustom(args, callbackContext);
                    break;
                case "mockTracking":
                    mockTracking(args, callbackContext);
                    break;
                case "stopTracking":
                    stopTracking(callbackContext);
                    break;
                case "isTracking":
                    isTracking(callbackContext);
                    break;
                case "getTrackingOptions":
                    getTrackingOptions(callbackContext);
                    break;
                case "onEvents":
                    onEvents(callbackContext);
                    break;
                case "onLocation":
                    onLocation(callbackContext);
                    break;
                case "onClientLocation":
                    onClientLocation(callbackContext);
                    break;
                case "onError":
                    onError(callbackContext);
                    break;
                case "offEvents":
                    offEvents();
                    break;
                case "offLocation":
                    offLocation();
                    break;
                case "offClientLocation":
                    offClientLocation();
                    break;
                case "offError":
                    offError();
                    break;
                case "getTripOptions":
                    getTripOptions(callbackContext);
                    break;
                case "startTrip":
                    startTrip(args, callbackContext);
                    break;
                case "updateTrip":
                    updateTrip(args, callbackContext);
                    break;
                case "completeTrip":
                    completeTrip(callbackContext);
                    break;
                case "cancelTrip":
                    cancelTrip(callbackContext);
                    break;
                case "getContext":
                    getContext(args, callbackContext);
                    break;
                case "searchPlaces":
                    searchPlaces(args, callbackContext);
                    break;
                case "searchGeofences":
                    searchGeofences(args, callbackContext);
                    break;
                case "autocomplete":
                    autocomplete(args, callbackContext);
                    break;
                case "geocode":
                    geocode(args, callbackContext);
                    break;
                case "reverseGeocode":
                    reverseGeocode(args, callbackContext);
                    break;
                case "ipGeocode":
                    ipGeocode(callbackContext);
                    break;
                case "getDistance":
                    getDistance(args, callbackContext);
                    break;
                case "getMatrix":
                    getMatrix(args, callbackContext);
                    break;
                case "setForegroundServiceOptions":
                    setForegroundServiceOptions(args, callbackContext);
                    break;
                case "setLogLevel":
                    setLogLevel(args, callbackContext);
                    break;
                default:
                    return false;
            }
        } catch (JSONException e) {
            Log.e("RadarCordovaPlugin", "JSONException", e);
            return false;
        }

        return true;
    }

    @Override
    public void pluginInitialize() {
        Log.d("RadarCordovaPlugin", "pluginInitialize");
        super.pluginInitialize();
        requestPermissionLauncher = cordova.getActivity().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            requestPermissionCallback.success(new JSONObject(isGranted));
        });
        receiver = new RadarCordovaReceiver();
        RadarCordovaPlugin.instance = this;
    }

    public void initialize(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "initialize");
        final String publishableKey = args.getString(0);
        Log.d("RadarCordovaPlugin", "initialize: " + publishableKey);
        Context context = this.cordova.getActivity().getApplicationContext();
        Radar.initialize(context, publishableKey, receiver, Radar.RadarLocationServicesProvider.GOOGLE, false);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void setUserId(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setUserId");
        final String userId = args.getString(0);
        Log.d("RadarCordovaPlugin", "setUserId: " + userId);
        Radar.setUserId(userId);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void getUserId(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getUserId");
        String userId = Radar.getUserId();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, userId));
    }

    public void setLogLevel(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setLogLevel");
        String level = args.getString(0);
        Log.d("RadarCordovaPlugin", "setLogLevel: " + level);
        Radar.RadarLogLevel logLevel;
        if (level != null) {
            level = level.toLowerCase();
            switch (level) {
                case "error":
                    logLevel = Radar.RadarLogLevel.ERROR;
                    break;
                case "warning":
                    logLevel = Radar.RadarLogLevel.WARNING;
                    break;
                case "info":
                    logLevel = Radar.RadarLogLevel.INFO;
                    break;
                case "debug":
                    logLevel = Radar.RadarLogLevel.DEBUG;
                    break;
                default:
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "invalid level: " + level));
                    return;
            }
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "level is required"));
            return;
        }
        Radar.setLogLevel(logLevel);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void setForegroundServiceOptions(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setForegroundServiceOptions");
        final JSONObject optionsJson = args.getJSONObject(0);
        RadarTrackingOptionsForegroundService options = RadarTrackingOptionsForegroundService.fromJson(optionsJson);
        Radar.setForegroundServiceOptions(options);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void setDescription(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setDescription");
        final String description = args.getString(0);

        Radar.setDescription(description);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void getDescription(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getDescription");
        String description = Radar.getDescription();

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, description));
    }

    public void setMetadata(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setMetadata");
        final JSONObject metadata = args.getJSONObject(0);

        Radar.setMetadata(metadata);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void getMetadata(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getMetadata");
        JSONObject metadata = Radar.getMetadata();

        assert metadata != null;
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, metadata));
    }

    public void setAnonymousTrackingEnabled(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "setAnonymousTrackingEnabled");
        final boolean enabled = args.getBoolean(0);

        Radar.setAnonymousTrackingEnabled(enabled);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void getPermissionsStatus(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getPermissionsStatus");
        String str;
        boolean foreground = cordova.hasPermission("android.permission.ACCESS_FINE_LOCATION");
        if (Build.VERSION.SDK_INT >= 29) {
            if (foreground) {
                boolean background = cordova.hasPermission("android.permission.ACCESS_BACKGROUND_LOCATION");
                str = background ? "GRANTED_BACKGROUND" : "GRANTED_FOREGROUND";
            } else {
                str = "DENIED";
            }
        } else {
            str = foreground ? "GRANTED_BACKGROUND" : "DENIED";
        }

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, str));
    }

    public void requestPermissions(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "requestPermissions");
        final boolean background = args.getBoolean(0);

        if (background && Build.VERSION.SDK_INT >= 29) {
            cordova.requestPermissions(this, 0, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"});
        } else {
            cordova.requestPermissions(this, 0, new String[]{"android.permission.ACCESS_FINE_LOCATION"});
        }

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void requestPermissionsSync(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "requestPermissions");
        final boolean background = args.getBoolean(0);

        try {
            this.requestPermissionCallback = callbackContext;
            if (background && Build.VERSION.SDK_INT >= 29) {
                this.requestPermissionLauncher.launch(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"});
            } else {
                this.requestPermissionLauncher.launch(new String[]{"android.permission.ACCESS_FINE_LOCATION"});
            }
            Log.d("RadarCordovaPlugin", "RequestPermission Launched");
        } catch (Exception e) {
            Log.e("RadarCordovaPlugin", e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    public void getLocation(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getLocation");
        String desiredAccuracy = args.getString(0);
        RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy accuracyLevel;
        String accuracy = desiredAccuracy != null ? desiredAccuracy.toLowerCase() : "medium";

        switch (accuracy) {
            case "low":
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.LOW;
                break;
            case "medium":
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;
                break;
            case "high":
                accuracyLevel = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.HIGH;
                break;
            default:
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "invalid desiredAccuracy: " + desiredAccuracy));
                return;
        }

        Radar.getLocation(accuracyLevel, (status, location, stopped) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                obj.put("stopped", stopped);

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void trackOnce(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "trackOnce");
        Radar.RadarTrackCallback callback = (status, location, events, user) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                if (user != null) {
                    obj.put("user", user.toJson());
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        Location location = null;
        RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy desiredAccuracy = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.MEDIUM;
        boolean beacons = false;
        if (args != null && args.length() > 0) {
            final JSONObject optionsObj = args.getJSONObject(0);
            if (optionsObj.has("location")) {
                JSONObject locationObj = optionsObj.getJSONObject("location");
                double latitude = locationObj.getDouble("latitude");
                double longitude = locationObj.getDouble("longitude");
                float accuracy = (float) locationObj.getDouble("accuracy");
                location = new Location("RadarCordovaPlugin");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAccuracy(accuracy);
            }
            if (optionsObj.has("desiredAccuracy")) {
                String desiredAccuracyStr = optionsObj.getString("desiredAccuracy").toLowerCase();
                switch (desiredAccuracyStr) {
                    case "none":
                        desiredAccuracy = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.NONE;
                        break;
                    case "low":
                        desiredAccuracy = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.LOW;
                        break;
                    case "medium":
                        break;
                    case "high":
                        desiredAccuracy = RadarTrackingOptions.RadarTrackingOptionsDesiredAccuracy.HIGH;
                        break;
                    default:
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "invalid desiredAccuracy: " + desiredAccuracy));
                        return;
                }
            }
            beacons = optionsObj.optBoolean("beacons", false);
        }

        if (location != null) {
            Radar.trackOnce(location, callback);
        } else {
            Radar.trackOnce(desiredAccuracy, beacons, callback);
        }
    }

    public void startTrackingEfficient(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "startTrackingEfficient");
        Radar.startTracking(RadarTrackingOptions.EFFICIENT);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void startTrackingResponsive(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "startTrackingResponsive");
        Radar.startTracking(RadarTrackingOptions.RESPONSIVE);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void startTrackingContinuous(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "startTrackingContinuous");
        Radar.startTracking(RadarTrackingOptions.CONTINUOUS);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void startTrackingCustom(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "startTrackingCustom");
        final JSONObject optionsObj = args.getJSONObject(0);

        RadarTrackingOptions options = RadarTrackingOptions.fromJson(optionsObj);
        Radar.startTracking(options);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void mockTracking(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "mockTracking");
        final JSONObject optionsObj = args.getJSONObject(0);

        JSONObject originObj = optionsObj.getJSONObject("origin");
        double originLatitude = originObj.getDouble("latitude");
        double originLongitude = originObj.getDouble("longitude");
        Location origin = new Location("RNRadarModule");
        origin.setLatitude(originLatitude);
        origin.setLongitude(originLongitude);
        JSONObject destinationObj = optionsObj.getJSONObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RNRadarModule");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        String modeStr = optionsObj.getString("mode");
        Radar.RadarRouteMode mode;
        switch (modeStr) {
            case "FOOT":
            case "foot":
                mode = Radar.RadarRouteMode.FOOT;
                break;
            case "BIKE":
            case "bike":
                mode = Radar.RadarRouteMode.BIKE;
                break;
            default:
                mode = Radar.RadarRouteMode.CAR;
                break;
        }
        int steps = optionsObj.has("steps") ? optionsObj.getInt("steps") : 10;
        int interval = optionsObj.has("interval") ? optionsObj.getInt("interval") : 1;

        Radar.mockTracking(origin, destination, mode, steps, interval, (status, location, events, user) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                if (user != null) {
                    obj.put("user", user.toJson());
                }

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void stopTracking(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "stopTracking");
        Radar.stopTracking();

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void isTracking(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "isTracking");
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, Radar.isTracking()));
    }

    public void getTrackingOptions(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getTrackingOptions");
        RadarTrackingOptions options = Radar.getTrackingOptions();
        JSONObject optionsJson = options.toJson();

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, optionsJson));
    }

    public void onEvents(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "onEvents");
        instance.eventsCallbackContext = callbackContext;
    }

    public void onLocation(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "onLocation");
        instance.locationCallbackContext = callbackContext;
    }

    public void onClientLocation(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "onClientLocation");
        instance.clientLocationCallbackContext = callbackContext;
    }

    public void onError(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "onError");
        instance.errorCallbackContext = callbackContext;
    }

    public void offEvents() throws JSONException {
        Log.d("RadarCordovaPlugin", "offEvents");
        instance.eventsCallbackContext = null;
    }

    public void offLocation() throws JSONException {
        Log.d("RadarCordovaPlugin", "offLocation");
        instance.locationCallbackContext = null;
    }

    public void offClientLocation() throws JSONException {
        Log.d("RadarCordovaPlugin", "offClientLocation");
        instance.clientLocationCallbackContext = null;
    }

    public void offError() throws JSONException {
        Log.d("RadarCordovaPlugin", "offError");
        instance.errorCallbackContext = null;
    }

    public void getTripOptions(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getTripOptions");
        RadarTripOptions options = Radar.getTripOptions();

        if (options == null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);

            return;
        }

        JSONObject optionsObj = options.toJson();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, optionsObj);
        callbackContext.sendPluginResult(pluginResult);
    }

    public void startTrip(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "startTrip");
        final JSONObject optionsObj = args.getJSONObject(0);

        // new format is { tripOptions, trackingOptions }
        JSONObject tripOptionsJson = optionsObj.optJSONObject("tripOptions");
        if (tripOptionsJson == null) {
            // legacy format
            tripOptionsJson = optionsObj;
        }
        RadarTripOptions options = RadarTripOptions.fromJson(tripOptionsJson);

        RadarTrackingOptions trackingOptions = null;
        JSONObject trackingOptionsJson = optionsObj.optJSONObject("trackingOptions");
        if (trackingOptionsJson != null) {
            trackingOptions = RadarTrackingOptions.fromJson(trackingOptionsJson);
        }

        Radar.startTrip(options, trackingOptions, (status, trip, events) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (trip != null) {
                    obj.put("trip", trip.toJson());
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void updateTrip(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "updateTrip");
        final JSONObject optionsObj = args.getJSONObject(0);

        JSONObject tripOptionsObj = optionsObj.getJSONObject("options");

        RadarTripOptions options = RadarTripOptions.fromJson(tripOptionsObj);
        RadarTrip.RadarTripStatus status = RadarTrip.RadarTripStatus.UNKNOWN;

        if (optionsObj.has("status")) {
            String statusStr = optionsObj.getString("status");
            if (statusStr.equalsIgnoreCase("started")) {
                status = RadarTrip.RadarTripStatus.STARTED;
            } else if (statusStr.equalsIgnoreCase("approaching")) {
                status = RadarTrip.RadarTripStatus.APPROACHING;
            } else if (statusStr.equalsIgnoreCase("arrived")) {
                status = RadarTrip.RadarTripStatus.ARRIVED;
            } else if (statusStr.equalsIgnoreCase("completed")) {
                status = RadarTrip.RadarTripStatus.COMPLETED;
            } else if (statusStr.equalsIgnoreCase("canceled")) {
                status = RadarTrip.RadarTripStatus.CANCELED;
            }
        }

        Radar.updateTrip(options, status, (status1, trip, events) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status1.toString());
                if (trip != null) {
                    obj.put("trip", trip.toJson());
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void completeTrip(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "completeTrip");
        Radar.completeTrip((status, trip, events) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (trip != null) {
                    obj.put("trip", trip.toJson());
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void cancelTrip(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "cancelTrip");
        Radar.cancelTrip((status, trip, events) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (trip != null) {
                    obj.put("trip", trip.toJson());
                }
                if (events != null) {
                    obj.put("events", RadarEvent.toJson(events));
                }
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void getContext(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getContext");
        Radar.RadarContextCallback callback = (status, location, context) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                if (context != null) {
                    obj.put("context", context.toJson());
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        if (args != null && args.length() > 0) {
            final JSONObject locationObj = args.getJSONObject(0);
            double latitude = locationObj.getDouble("latitude");
            double longitude = locationObj.getDouble("longitude");
            Location location = new Location("RadarCordovaPlugin");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            Radar.getContext(location, callback);
        } else {
            Radar.getContext(callback);
        }
    }

    public void searchPlaces(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "searchPlaces");
        Radar.RadarSearchPlacesCallback callback = (status, location, places) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                if (places != null) {
                    obj.put("places", RadarPlace.toJson(places));
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        final JSONObject optionsObj = args.getJSONObject(0);

        Location near = null;
        if (optionsObj.has("near")) {
            JSONObject nearObj = optionsObj.getJSONObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            near = new Location("RNRadarModule");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
        }
        int radius = optionsObj.has("radius") ? optionsObj.getInt("radius") : 1000;
        String[] chains = optionsObj.has("chains") ? RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("chains")) : null;
        Map<String, String> chainMetadata = optionsObj.has("chainMetadata") ? RadarCordovaPlugin.stringMapForJSONObject(optionsObj.getJSONObject("chainMetadata")) : null;
        String[] categories = optionsObj.has("categories") ? RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("categories")) : null;
        String[] groups = optionsObj.has("groups") ? RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("groups")) : null;
        int limit = optionsObj.has("limit") ? optionsObj.getInt("limit") : 10;

        if (near != null) {
            Radar.searchPlaces(near, radius, chains, chainMetadata, categories, groups, limit, callback);
        } else {
            Radar.searchPlaces(radius, chains, chainMetadata, categories, groups, limit, callback);
        }
    }

    public void searchGeofences(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "searchGeofences");
        Radar.RadarSearchGeofencesCallback callback = (status, location, geofences) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (location != null) {
                    obj.put("location", Radar.jsonForLocation(location));
                }
                if (geofences != null) {
                    obj.put("geofences", RadarGeofence.toJson(geofences));
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        final JSONObject optionsObj = args.getJSONObject(0);

        Location near = null;
        if (optionsObj.has("near")) {
            JSONObject nearObj = optionsObj.getJSONObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            near = new Location("RNRadarModule");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
        }
        JSONObject metadata = optionsObj.has("metadata") ? optionsObj.getJSONObject("metadata") : null;
        int radius = optionsObj.has("radius") ? optionsObj.getInt("radius") : 1000;
        String[] tags = optionsObj.has("tags") ? RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("tags")) : null;
        int limit = optionsObj.has("limit") ? optionsObj.getInt("limit") : 10;

        if (near != null) {
            Radar.searchGeofences(near, radius, tags, metadata, limit, callback);
        } else {
            Radar.searchGeofences(radius, tags, metadata, limit, callback);
        }
    }

    public void autocomplete(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "autocomplete");
        final JSONObject optionsObj = args.getJSONObject(0);

        String query = optionsObj.getString("query");
        Location near = null;
        if (optionsObj.has("near")) {
            JSONObject nearObj = optionsObj.getJSONObject("near");
            double latitude = nearObj.getDouble("latitude");
            double longitude = nearObj.getDouble("longitude");
            near = new Location("RNRadarModule");
            near.setLatitude(latitude);
            near.setLongitude(longitude);
        }
        int limit = optionsObj.has("limit") ? optionsObj.getInt("limit") : 10;
        String country = optionsObj.getString("country");
        String[] layers = optionsObj.has("layers") ? RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("layers")) : null;

        Radar.autocomplete(query, near, layers, limit, country, (status, addresses) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (addresses != null) {
                    obj.put("addresses", RadarAddress.toJson(addresses));
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void geocode(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "geocode");
        final String query = args.getString(0);

        Radar.geocode(query, (status, addresses) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (addresses != null) {
                    obj.put("addresses", RadarAddress.toJson(addresses));
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void reverseGeocode(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "reverseGeocode");
        Radar.RadarGeocodeCallback callback = (status, addresses) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (addresses != null) {
                    obj.put("addresses", RadarAddress.toJson(addresses));
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        if (args != null && args.length() > 0) {
            final JSONObject locationObj = args.getJSONObject(0);
            double latitude = locationObj.getDouble("latitude");
            double longitude = locationObj.getDouble("longitude");
            float accuracy = (float) locationObj.getDouble("accuracy");
            Location location = new Location("RadarCordovaPlugin");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(accuracy);

            Radar.reverseGeocode(location, callback);
        } else {
            Radar.reverseGeocode(callback);
        }
    }

    public void ipGeocode(final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "ipGeocode");
        Radar.ipGeocode((status, address, proxy) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (address != null) {
                    obj.put("address", address.toJson());
                    obj.put("proxy", proxy);
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public void getDistance(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getDistance");
        Radar.RadarRouteCallback callback = (status, routes) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (routes != null) {
                    obj.put("routes", routes.toJson());
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        };

        final JSONObject optionsObj = args.getJSONObject(0);

        Location origin = null;
        if (optionsObj.has("origin")) {
            JSONObject originObj = optionsObj.getJSONObject("origin");
            double originLatitude = originObj.getDouble("latitude");
            double originLongitude = originObj.getDouble("longitude");
            origin = new Location("RNRadarModule");
            origin.setLatitude(originLatitude);
            origin.setLongitude(originLongitude);
        }
        JSONObject destinationObj = optionsObj.getJSONObject("destination");
        double destinationLatitude = destinationObj.getDouble("latitude");
        double destinationLongitude = destinationObj.getDouble("longitude");
        Location destination = new Location("RNRadarModule");
        destination.setLatitude(destinationLatitude);
        destination.setLongitude(destinationLongitude);
        EnumSet<Radar.RadarRouteMode> modes = EnumSet.noneOf(Radar.RadarRouteMode.class);
        List<String> modesList = Arrays.asList(RadarCordovaPlugin.stringArrayForArray(optionsObj.getJSONArray("modes")));
        if (modesList.contains("FOOT") || modesList.contains("foot")) {
            modes.add(Radar.RadarRouteMode.FOOT);
        }
        if (modesList.contains("BIKE") || modesList.contains("bike")) {
            modes.add(Radar.RadarRouteMode.BIKE);
        }
        if (modesList.contains("CAR") || modesList.contains("car")) {
            modes.add(Radar.RadarRouteMode.CAR);
        }
        if (modesList.contains("TRUCK") || modesList.contains("truck")) {
            modes.add(Radar.RadarRouteMode.TRUCK);
        }
        if (modesList.contains("MOTORBIKE") || modesList.contains("motorbike")) {
            modes.add(Radar.RadarRouteMode.MOTORBIKE);
        }
        String unitsStr = optionsObj.getString("units");
        Radar.RadarRouteUnits units = unitsStr.equals("METRIC") || unitsStr.equals("metric") ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

        if (origin != null) {
            Radar.getDistance(origin, destination, modes, units, callback);
        } else {
            Radar.getDistance(destination, modes, units, callback);
        }
    }

    public void getMatrix(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("RadarCordovaPlugin", "getMatrix");
        final JSONObject optionsObj = args.getJSONObject(0);

        JSONArray originsArr = optionsObj.getJSONArray("origins");
        Location[] origins = new Location[originsArr.length()];
        for (int i = 0; i < originsArr.length(); i++) {
            JSONObject originObj = originsArr.getJSONObject(i);
            double originLatitude = originObj.getDouble("latitude");
            double originLongitude = originObj.getDouble("longitude");
            Location origin = new Location("RadarCordovaPlugin");
            origin.setLatitude(originLatitude);
            origin.setLongitude(originLongitude);
            origins[i] = origin;
        }
        JSONArray destinationsArr = optionsObj.getJSONArray("destinations");
        Location[] destinations = new Location[destinationsArr.length()];
        for (int i = 0; i < destinationsArr.length(); i++) {
            JSONObject destinationObj = destinationsArr.getJSONObject(i);
            double destinationLatitude = destinationObj.getDouble("latitude");
            double destinationLongitude = destinationObj.getDouble("longitude");
            Location destination = new Location("RadarCordovaPlugin");
            destination.setLatitude(destinationLatitude);
            destination.setLongitude(destinationLongitude);
            destinations[i] = destination;
        }
        Radar.RadarRouteMode mode;
        String modeStr = optionsObj.getString("mode");
        switch (modeStr) {
            case "FOOT":
            case "foot":
                mode = Radar.RadarRouteMode.FOOT;
                break;
            case "BIKE":
            case "bike":
                mode = Radar.RadarRouteMode.BIKE;
                break;
            case "TRUCK":
            case "truck":
                mode = Radar.RadarRouteMode.TRUCK;
                break;
            case "MOTORBIKE":
            case "motorbike":
                mode = Radar.RadarRouteMode.MOTORBIKE;
                break;
            default:
                mode = Radar.RadarRouteMode.CAR;
                break;
        }
        String unitsStr = optionsObj.getString("units");
        Radar.RadarRouteUnits units = unitsStr.equals("METRIC") || unitsStr.equals("metric") ? Radar.RadarRouteUnits.METRIC : Radar.RadarRouteUnits.IMPERIAL;

        Radar.getMatrix(origins, destinations, mode, units, (status, matrix) -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());
                if (matrix != null) {
                    obj.put("matrix", matrix.toJson());
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    public static class RadarCordovaReceiver extends RadarReceiver {
        @Override
        public void onEventsReceived(@NonNull Context context, @NonNull RadarEvent[] events, RadarUser user) {
            Log.d("RadarCordovaPlugin", "onEventsReceived");

            if (instance.eventsCallbackContext == null) {
                Log.w("RadarCordovaPlugin", "eventsCallbackContext == null");
                return;
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("events", RadarEvent.toJson(events));
                obj.put("user", user.toJson());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                instance.eventsCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                instance.eventsCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

        @Override
        public void onLocationUpdated(@NonNull Context context, @NonNull Location location, @NonNull RadarUser user) {
            Log.d("RadarCordovaPlugin", "onLocationUpdated");
            if (instance.locationCallbackContext == null) {
                return;
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("location", Radar.jsonForLocation(location));
                obj.put("user", user.toJson());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                instance.locationCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                instance.locationCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

        @Override
        public void onClientLocationUpdated(@NonNull Context context, @NonNull Location location, boolean stopped, @NonNull Radar.RadarLocationSource source) {
            Log.d("RadarCordovaPlugin", "onClientLocationUpdated");
            if (instance.clientLocationCallbackContext == null) {
                return;
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("location", Radar.jsonForLocation(location));
                obj.put("stopped", stopped);
                obj.put("source", source.toString());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                instance.clientLocationCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                instance.clientLocationCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

        @Override
        public void onError(@NonNull Context context, @NonNull Radar.RadarStatus status) {
            Log.d("RadarCordovaPlugin", "onError");
            if (instance.errorCallbackContext == null) {
                return;
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("status", status.toString());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                instance.errorCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                Log.e("RadarCordovaPlugin", "JSONException", e);
                instance.errorCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

        @Override
        public void onLog(@NonNull Context context, @NonNull String message) {
            Log.d("RadarCordovaPlugin", "onLog");
            Log.d("RadarCordovaPlugin", message);

        }

    }
}