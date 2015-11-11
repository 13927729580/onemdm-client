package com.multunus.onemdm.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.multunus.onemdm.config.Config;
import com.multunus.onemdm.heartbeat.HeartbeatRecorder;
import com.multunus.onemdm.model.Device;
import com.multunus.onemdm.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceRegistration {

    Context context;
    RequestQueue requestQueue;

    public DeviceRegistration(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(this.context);
    }

    public void sendRegistrationRequestToServer() {
        JsonObjectRequest deviceRegistrationRequest = new JsonObjectRequest(
                Request.Method.POST,
                Config.REGISTRATION_URL,
                getJsonPayload(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        saveSettings(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.warning(error.toString());
                    }
                }
        );
        requestQueue.add(deviceRegistrationRequest);
    }

    private void saveSettings(JSONObject response) {
        String accessToken = "";
        long nextHeartbeatTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = this.context.getSharedPreferences(
                Config.PREFERENCE_TAG, Context.MODE_PRIVATE).edit();
        try {
            accessToken = response.getString(Config.ACCESS_TOKEN);
            nextHeartbeatTime = response.getLong("next_heartbeat_time") * 1000;
        } catch (JSONException e) {
            Logger.warning("Exception while registering",e);
        }
        editor.putString(Config.ACCESS_TOKEN, accessToken);
        editor.commit();
        new HeartbeatRecorder(context).configureNextHeartbeatWithMilliSeconds(nextHeartbeatTime);
    }

    private JSONObject getJsonPayload() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        JSONObject deviceData = new JSONObject();
        try {
            deviceData.put("device", new JSONObject(gson.toJson(getDevice())));
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.warning(e.getMessage());
        }
        Logger.debug("Device data to be send "+deviceData);
        return deviceData;
    }

    private Device getDevice() {
        Device device = new Device();
        device.setModel(getDeviceModel());
        device.setImeiNumber(getImeiNumber());
        device.setUniqueId(getAndroidId());
        return device;
    }

    private String getDeviceModel() {
        return Build.MANUFACTURER + " - " + Build.MODEL;
    }

    private String getImeiNumber() {
//        TelephonyManager telephonyManager =
//                (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
//        return telephonyManager.getDeviceId();
        return "";
    }

    private String getAndroidId() {
        return Settings.Secure.getString(
                this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
