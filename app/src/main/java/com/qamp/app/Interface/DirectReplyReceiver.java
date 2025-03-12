package com.qamp.app.Interface;



import static com.qamp.app.Interface.MyFirebaseMessagingService.channelId;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.MessagingModule.MesiboMessagingActivity;
import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.R;
import com.qamp.app.Utils.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DirectReplyReceiver extends BroadcastReceiver {

    String myNumber;
    String receiverNumber;
    String reply;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = android.app.RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence replyText = remoteInput.getCharSequence("key_text_reply");
            if (replyText != null) {
                // Handle the user's reply here
                receiverNumber = intent.getStringExtra("myNumber");
                myNumber = intent.getStringExtra("receiverNumber");
                String tag = intent.getStringExtra("tag");
                reply = replyText.toString();


                Log.e("myNumber",myNumber);
                Log.e("receiverNumber",receiverNumber);

                MesiboProfile profile1 = Mesibo.getSelfProfile();

                if (receiverNumber.length() == 4){
                    String phoneNumber = receiverNumber; // Your phone number with "91" prefix
                    String group;

                    if (phoneNumber.startsWith("91") && phoneNumber.length() > 2) {
                        group = phoneNumber.substring(2); // Remove the "91" prefix
                    } else {
                        group = phoneNumber; // No "91" prefix found, use the original number
                    }
                    Toast.makeText(context, myNumber, Toast.LENGTH_SHORT).show();
                    MesiboProfile profile2 = Mesibo.getProfile(Long.parseLong(group));
                    performGroupChatNotification(receiverNumber,profile2.getName(),reply,profile2.groupid,context);
                }else {
                    performChatNotification(myNumber,profile1.getName(),reply,receiverNumber,context);

                }

                int notificationId = intent.getIntExtra("notificationId", 0);
                if (notificationId != 0) {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationId);
                }

            }
        }
    }

    private void performChatNotification(String address, String name, String message, String mPeer,Context context) {

        String phoneNumber = address; // Your phone number with "91" prefix
        String myNumber;

        if (phoneNumber.startsWith("91") && phoneNumber.length() > 2) {
            myNumber = phoneNumber.substring(2); // Remove the "91" prefix
        } else {
            myNumber = phoneNumber; // No "91" prefix found, use the original number
        }

        String number = mPeer; // Your phone number with "91" prefix
        String receiverNumber;

        if (number.startsWith("91") && phoneNumber.length() > 2) {
            receiverNumber = number.substring(2); // Remove the "91" prefix
        } else {
            receiverNumber = number; // No "91" prefix found, use the original number
        }

        String API_URL = "http://dcore.qampservices.in/v1/notification-service/notification/mobilenumber/send";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("mobileNumber", receiverNumber);
            requestBody.put("countryCode", "91");
            requestBody.put("title", name);
            requestBody.put("body", message);
            requestBody.put("region", "TEST");
            requestBody.put("notificationType", "MESSAGE");
            requestBody.put("onClick", "onclick");
            requestBody.put("destinationId", myNumber);
            requestBody.put("sendersId", receiverNumber);
            requestBody.put("sendersAdd", receiverNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Create a JSON object request
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, API_URL, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            MesiboProfile profile = Mesibo.getProfile("91"+receiverNumber);

                            MesiboMessage message = profile.newMessage();
                            message.message = reply;
                            message.send();

                            // Do something with the response data
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any errors here
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set the headers for the request
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("user-session-token", AppConfig.getConfig().token);
                return headers;
            }
        };

        // Add the request to the queue
        requestQueue.add(jsonRequest);


    }


    private void performGroupChatNotification(String address, String name, String message, long groupid,Context context) {
        String API_URL = "http://dcore.qampservices.in/v1/notification-service/notification/group/send";

        Log.e("Group", String.valueOf(groupid));
        Log.e("add",address);
        Log.e("name",name);

        String phoneNumber = address; // Your phone number with "91" prefix
        String myNumber;

        if (phoneNumber.startsWith("91") && phoneNumber.length() > 2) {
            myNumber = phoneNumber.substring(2); // Remove the "91" prefix
        } else {
            myNumber = phoneNumber; // No "91" prefix found, use the original number
        }

        MesiboProfile profile = Mesibo.getSelfProfile();

        String groupId = String.valueOf(groupid);
        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("groupid", groupId);
            requestBody.put("title", name);
            requestBody.put("body", message);
            requestBody.put("onClick", "onclick");
            requestBody.put("notificationType", "MESSAGE_GROUP");
            requestBody.put("sendersId", profile.address);
            requestBody.put("sendersAdd", profile.address);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Create a JsonObjectRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, API_URL, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle a successful response here
                        try {
                            String status = response.getString("status");
                            MesiboProfile profile = Mesibo.getProfile(Long.parseLong(receiverNumber));

                            MesiboMessage message = profile.newMessage();
                            message.message = reply;
                            message.send();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors here
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set headers for the request
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("user-session-token", AppConfig.getConfig().token); // Replace with your actual session token
                return headers;
            }
        };

        // Add the request to the queue
        requestQueue.add(jsonRequest);
    }

}
