package com.qamp.app.APIHandling;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mesibo.api.Mesibo;
import com.qamp.app.Activity.OnBoardingScreens;
import com.qamp.app.LoginModule.MesiboApiClasses.SampleAPI;
import com.qamp.app.R;
import com.qamp.app.Utils.AppConfig;
import com.qamp.app.Utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiGeneratorClass {
    static String host = "http://dcore.qampservices.in";
    static String userService = "/v2/user-service";
    static String verifyEndPoint = "/user/validateotp";

    public static void callOTPFunction(String phoneNumberText, String countryCode, Activity activity) {
        AppUtils.showCustomToast(activity, "Enter otp 1527 to continue");
    }

    public static void verifyOTPFunction(String phoneNumberText, String code, Activity activity) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(activity);
            String URL = "http://192.168.1.137:9501/create_user";
            JSONObject jsonBody = new JSONObject();
            String phone = phoneNumberText;
            jsonBody.put("address", "91"+phone);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.e("VOLLEY======", response);
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        Log.e("VOLLEY Status======", status);
                            JSONObject data = jsonObject.getJSONObject("data");
                            String mobileNumber = data.getString("address");
                            String token = data.getString("token");
                            int responseVersion = data.getInt("version");
                            String profileStatus = "";
                            String fullName = "";
                            String profilePicId = "";
                            if (data.has("status")) {
                                profileStatus = data.getString("status");
                            }
                            if (data.has("fullName")) {
                                fullName = data.getString("fullName");
                            }
                            if (data.has("profilePicId")) {
                                profilePicId = data.getString("profilePicId");
                            }
                            if (!TextUtils.isEmpty(token)) {
                                AppConfig.getConfig().token = token; //TBD, save into preference
                                AppConfig.getConfig().phone =  mobileNumber;
                                AppConfig.getConfig().countryCode = "91";
                                AppConfig.getConfig().name = fullName; //TBD, save into preference
                                AppConfig.getConfig().status = profileStatus;
                                AppConfig.getConfig().ts = 1649220290;
                                AppConfig.getConfig().tnm = "1649220290";
                                AppConfig.getConfig().version = responseVersion;
                                AppConfig.getConfig().profileId = profilePicId;
                                SampleAPI.setSyncFlags();
                                Mesibo.reset();
                                if (SampleAPI.startMesibo(true)) {
                                    SampleAPI.startSync();
                                }
                                AppConfig.save();
                                if (AppUtils.getStringValue(activity, "isOnBoardingScreensSaved", "").equals("true")) {
                                    Intent intent = new Intent(activity, OnBoardingScreens.class);
                                    activity.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    activity.finish();
                                } else {
                                    Intent intent = new Intent(activity, OnBoardingScreens.class);
                                    activity.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    activity.finish();
                                }
                            }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Error", e.toString());
                        Toast.makeText(activity, "Not Login " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    Toast.makeText(activity, activity.getString(R.string.general_error), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    try {
                        params.put("address", jsonBody.getString("address"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (params != null && params.size() > 0) {
                        return AppUtils.encodeParameters(params, getParamsEncoding());
                    }

                    return null;
                }
            };
            requestQueue.add(stringRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity, activity.getString(R.string.general_error), Toast.LENGTH_LONG).show();
        }
    }
}

