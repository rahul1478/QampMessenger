/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 3:25 AM
 *
 */

package com.qamp.app.LoginModule.MesiboApiClasses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboHttp;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboPhoneContactsManager;
import com.mesibo.api.MesiboProfile;
//import com.mesibo.contactutils.ContactUtils;
import com.mesibo.mediapicker.MediaPicker;
import com.qamp.app.MainApplication;
import com.qamp.app.Utils.AppConfig;
import org.json.JSONObject;

import java.util.ArrayList;

public class SampleAPI {
    public final static String KEY_SYNCEDCONTACTS = "AppSyncedContacts";
    public final static String KEY_SYNCEDDEVICECONTACTSTIME = "AppSyncedPhoneContactTs";
    public final static String KEY_SYNCEDCONTACTSTIME = "AppSyncedTsNew";
    public final static String KEY_AUTODOWNLOAD = "autodownload";
    public final static String KEY_GCMTOKEN = "gcmtoken";
    private static final String TAG = "SampleAPI";
    //private static NotifyUser mNotifyUser = null;
    private static boolean mSyncPending = true;
    private static Context mContext = null;
    private static boolean mResetSyncedContacts = false;
    private static Gson mGson = new Gson();
    private static String mApiUrl = "https://messenger.mesibo.com";
    private static String mGCMToken = null;
    private static boolean mGCMTokenSent = false;
    private static boolean mAutoDownload = true;

    private static boolean invokeApi(final Context context, final JSONObject postBunlde, String filePath, String formFieldName, boolean uiThread) {
        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {

            }
        };

        http.setContext(context);
        http.setOnUiThread(uiThread);
        return http.sendRequest(postBunlde, filePath, formFieldName);
    }

//    public static String phoneBookLookup(String phone) {
//        if (TextUtils.isEmpty(phone)) return null;
//
//        return ContactUtils.reverseLookup(phone);
//    }

    public static void updateDeletedGroup(long gid) {
        if (0 == gid) return;
        MesiboProfile u = Mesibo.getProfile(gid);
        if (null == u) return;
        //u.flag |= MesiboProfile.FLAG_DELETED;
        u.save();
    }

    private static boolean parseResponse(Response response, Context context, boolean uiThread) {

        if (null == response || null == response.result) {

            if (uiThread && null != context) {
                showConnectionError(context);
            }
            return false;
        }

        if (!response.result.equalsIgnoreCase("OK")) {
            if (null != response.error && response.error.equalsIgnoreCase("AUTHFAIL")) {
                forceLogout();
                return false;
            }

            if (null != response.error && response.error.equalsIgnoreCase("UPDATE")) {
            }

            if (null != response.errmsg) {
                if (null == response.errtitle) response.errtitle = "Failed";
                UIManager.showAlert(context, response.errtitle, response.errmsg);
            }
            return false;
        }

        boolean save = false;
        if (null != response.urls) {
            AppConfig.getConfig().uploadurl = response.urls.upload;
            AppConfig.getConfig().downloadurl = response.urls.download;
            save = true;
        }

        if (null != response.share) {
            //AppConfig.getConfig().invite = response.share;
            save = true;
        }

        if (!TextUtils.isEmpty(response.message)) {
            UIManager.showAlert(context, response.title, response.message);
        }

        if (response.op.equals("login") && !TextUtils.isEmpty(response.token)) {
            AppConfig.getConfig().token = response.token; //TBD, save into preference
            AppConfig.getConfig().phone = response.phone;
            mResetSyncedContacts = true;
            mSyncPending = true;

            save = true;

            Mesibo.reset();
            if (startMesibo(true)) {
                // need permission
                startSync();
            }
        } else if (response.op.equals("logout")) {
            forceLogout();
            AppConfig.reset();
        }

        if (save) AppConfig.save();
        return true;
    }

    public static void showConnectionError(Context context) {
        String title = "No Internet Connection";
        String message = "Your phone is not connected to the internet. Please check your internet connection and try again later.";
        UIManager.showAlert(context, title, message);
    }

    public static void saveLocalSyncedContacts(String contacts, long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTS, contacts);
        Mesibo.setKey(SampleAPI.KEY_SYNCEDDEVICECONTACTSTIME, String.valueOf(timestamp));
    }

    public static void saveSyncedTimestamp(long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTSTIME, String.valueOf(timestamp));
    }

    public static void init(Context context) {
        mContext = context;

        Mesibo api = Mesibo.getInstance();
        api.init(context);

        Mesibo.initCrashHandler(MesiboListeners.getInstance());
        Mesibo.uploadCrashLogs();

        if (!TextUtils.isEmpty(AppConfig.getConfig().token)) {
            if (startMesibo(false)) startSync();
        }
    }

    public static String getPhone() {
        if (!TextUtils.isEmpty(AppConfig.getConfig().phone)) {
            return AppConfig.getConfig().phone;
        }


        MesiboProfile u = Mesibo.getSelfProfile();

        //MUST not happen
        if (null == u) {
            forceLogout();
            return null;
        }
        AppConfig.getConfig().phone = u.address;
        AppConfig.save();
        return AppConfig.getConfig().phone;
    }

    public static String getToken() {
        return AppConfig.getConfig().token;
    }

    public static String getUploadUrl() {
        return AppConfig.getConfig().uploadurl;
    }

    public static String getDownloadUrl() {
        return AppConfig.getConfig().downloadurl;
    }

    public static void startOnlineAction() {
        sendGCMToken();
        startSync();
    }

    public static void startSync() {

        synchronized (SampleAPI.class) {
            if (!mSyncPending) return;
            mSyncPending = false;
        }

        startContactsSync();
    }

    // this is called to indicate first round of sync is done
    //TBD, this may trigger getcontact with hidden=1 to reach server before last contact sysc getconatct request
    public static void syncDone() {
        synchronized (SampleAPI.class) {

        }
    }

    public static void startContactsSync() {
        MesiboPhoneContactsManager contacts = Mesibo.getPhoneContactsManager();
        contacts.overrideProfileName(true);
        contacts.start();
    }

    public static boolean startMesibo(boolean resetContacts) {
        Log.e("Mesibo_______", "Starting Mesibo initialization...");

        MesiboRegistrationIntentService.startRegistration(mContext, AppConfig.getConfig().phone, MesiboListeners.getInstance());
        Log.e("Mesibo_______", "Registration service started."+AppConfig.getConfig().phone);

        String path = Mesibo.getBasePath();
        MediaPicker.setPath(path);
        Log.e("Mesibo_______", "Media picker path set: " + path);

        // Add listeners
        Mesibo.addListener(MesiboListeners.getInstance());
        MesiboUI.setListener(MesiboListeners.getInstance());
        Log.e("Mesibo_______", "Listeners added.");

        // Add file transfer handler
        MesiboFileTransferHelper fileTransferHelper = new MesiboFileTransferHelper();
        Log.e("Mesibo_______", "File transfer helper initialized.");

        // Set access token
        Log.e("Mesibo_______", "Using Access Token: " + AppConfig.getConfig().token);
        if (0 != Mesibo.setAccessToken(AppConfig.getConfig().token)) {
            Log.e("Mesibo_______", "Failed to set access token. Clearing token...");
            AppConfig.getConfig().token = "";
            AppConfig.save();
            return false;
        }
        Log.e("Mesibo_______", "Access token set successfully.");


        // Set database
        Mesibo.setDatabase("mesibo.db");
        Log.e("Mesibo_______", "Database set.");

        // Reset contacts if required
        if (resetContacts) {
            SampleAPI.saveLocalSyncedContacts("", 0);
            SampleAPI.saveSyncedTimestamp(0);
            Log.e("Mesibo_______", "Contacts reset.");
        }

        initAutoDownload();
        Log.e("Mesibo_______", "Auto-download initialized.");

        Mesibo.enableGalleryScanForMedia(true);
        Mesibo.setSecureScreen(true);
        Log.e("Mesibo_______", "Gallery scan enabled, secure screen set.");

        // Enable end-to-end encryption
        Mesibo.e2ee().enable(true);
        Log.e("Mesibo_______", "End-to-End Encryption enabled.");

        // Start Mesibo
        int mesiboStartStatus = Mesibo.start();
        if (0 != mesiboStartStatus) {
            Log.e("Mesibo_______", "Mesibo failed to start. Error Code: " + mesiboStartStatus);
            return false;
        }
        Log.e("Mesibo_______", "Mesibo started successfully.");

        String ts = Mesibo.readKey(KEY_SYNCEDCONTACTSTIME);
        Log.e("Mesibo_______", "Synced contacts timestamp: " + ts);

        MesiboPhoneContactsManager contactsManager = Mesibo.getPhoneContactsManager();
        contactsManager.overrideProfileName(true);

        if(resetContacts) {
            Mesibo.getPhoneContactsManager().reset();
        }

        Intent restartIntent = new Intent(MainApplication.getRestartIntent());
        Mesibo.runInBackground(MainApplication.getAppContext(), null, restartIntent);
        Log.e("Mesibo_______", "Mesibo is running in the background.");




        return true;
    }

    public static void forceLogout() {
        mGCMTokenSent = false;
        Mesibo.setKey(KEY_GCMTOKEN, "");
        SampleAPI.saveLocalSyncedContacts("", 0);
        SampleAPI.saveSyncedTimestamp(0);
        Mesibo.stop(true);
        AppConfig.getConfig().reset();
//        mNotifyUser.clearNotification();
        Mesibo.reset();
//        ContactUtils.syncReset();

        UIManager.launchStartupActivity(mContext, true);
    }

    public static boolean startLogout() {
        if (TextUtils.isEmpty(AppConfig.getConfig().token)) return false;
        JSONObject b = new JSONObject();
        try {
            b.put("op", "logout");
            b.put("token", AppConfig.getConfig().token);
        } catch (Exception e) {

        }
        invokeApi(null, b, null, null, false);
        return true;
    }

    public static void login(String phoneNumber, String verificationCode, ResponseHandler handler) {
        //Mesibo.resetDB();

        JSONObject b = new JSONObject();
        try {
            b.put("op", "login");
            b.put("appid", mContext.getPackageName());
            b.put("phone", phoneNumber);
            if (!TextUtils.isEmpty(verificationCode)) b.put("otp", verificationCode);
        } catch (Exception e) {

        }

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
    }

    public static boolean syncContactsInBulk(ArrayList<String> contacts) {
        if (contacts == null || contacts.isEmpty()) return false;

        for (String contact : contacts) {
            MesiboProfile profile = new MesiboProfile();
            profile.address = contact;
            profile.setName(contact);

            // Save the contact in Mesibo
            profile.setContact(true);
        }

        return true;
    }


//    public static boolean deleteContacts(ArrayList<String> contacts) {
//        if (null == contacts || 0 == contacts.size()) return false;
//
//        String[] c = contacts.toArray(new String[contacts.size()]);
//        Mesibo.syncContacts(c, false, true, 0, true);
//
//        return true;
//
//    }

    public static void notify(int id, String title, String message) {
///        mNotifyUser.sendNotification(id, title, message);
    }

    public static void notify(MesiboMessage params, String message) {
        // if call is in progress, we must give notification even if reading because user is in call
        // screen
       // if (!MesiboCall.getInstance().isCallInProgress() && Mesibo.isReading(params)) return;

        // TBD, create read session for unread messages in database
        if (!params.isRealtimeMessage() || Mesibo.MSGSTATUS_OUTBOX == params.getStatus()) return;

        //MUST not happen for realtime message
        if (params.groupid > 0 && null == params.groupProfile) return;

        MesiboProfile profile = Mesibo.getProfile(params);

        // this will also mute message from user in group
        if (null != profile && profile.isMuted()) return;

        String name = params.peer;
        if (null != profile) {
            name = profile.getName();
        }

        if (params.groupid > 0) {
            MesiboProfile gp = Mesibo.getProfile(params.groupid);
            if (null == gp) return; // must not happen

            if (gp.isMuted()) return;

            name += " @ " + gp.getName();
        }

        if (params.isMissedCall()) {
            String subject = "Missed Call";
            message = "You missed a  " + (params.isVideoCall() ? "video " : "") + "call from " + profile.getNameOrAddress();
            SampleAPI.notify(2, subject, message);
            return;
        }

        // outgoing or incoming call
        if (params.isCall()) return;

//        mNotifyUser.sendNotificationInList(name, message);
    }

    public static void addContacts(ArrayList<MesiboProfile> profiles, boolean hidden) {

        ArrayList<String> c = new ArrayList<String>();

        for (int i = 0; i < profiles.size(); i++) {
            MesiboProfile profile = profiles.get(i);
            if (!TextUtils.isEmpty(profile.address)) c.add(profile.address);

        }

        if (c.size() == 0) return;

//        getContacts(c, hidden, true);
    }

    public static void setGCMToken(String token) {
        mGCMToken = token;
        sendGCMToken();
    }

    public static void setSyncFlags() {
        mResetSyncedContacts = true;
        mSyncPending = true;
    }



    private static void sendGCMToken() {
        if (null == mGCMToken || mGCMTokenSent) {
            return;
        }

        synchronized (SampleAPI.class) {
            if (mGCMTokenSent) return;
            mGCMTokenSent = true;
        }

        Mesibo.setPushToken(mGCMToken);

    }

    /* if it is called from service, it's okay to block, we should wait till
       we are online. As soon as we return, service will be destroyed
     */
    public static void onGCMMessage(boolean inService) {
        Mesibo.setForegroundContext(null, -1, true);

        while (inService) {
            if (Mesibo.STATUS_ONLINE == Mesibo.getConnectionStatus()) break;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        // wait for messages to receive etc
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public static void notifyClear() {
        //   mNotifyUser.clearNotification();
    }

    private static void initAutoDownload() {
        String autodownload = Mesibo.readKey(KEY_AUTODOWNLOAD);
        mAutoDownload = (TextUtils.isEmpty(autodownload));
    }

    public static boolean getMediaAutoDownload() {
        return mAutoDownload;
    }

    public static void setMediaAutoDownload(boolean autoDownload) {
        mAutoDownload = autoDownload;
        Mesibo.setKey(KEY_AUTODOWNLOAD, mAutoDownload ? "" : "0");
    }

    public static abstract class ResponseHandler implements MesiboHttp.Listener {
        public static boolean result = true;
        public Context mContext = null;
        private MesiboHttp http = null;
        private Bundle mRequest = null;
        private boolean mBlocking = false;
        private boolean mOnUiThread = false;

        @Override
        public boolean Mesibo_onHttpProgress(MesiboHttp http, int state, int percent) {
            if (percent < 0) {
                HandleAPIResponse(null);
                return true;
            }

            if (100 == percent && MesiboHttp.STATE_DOWNLOAD == state) {
                String strResponse = http.getDataString();
                Response response = null;

                if (null != strResponse) {
                    try {
                        response = mGson.fromJson(strResponse, Response.class);
                    } catch (Exception e) {
                        result = false;
                    }
                }

                if (null == response) result = false;

                final Context context = (null == this.mContext) ? SampleAPI.mContext : this.mContext;

                if (!mOnUiThread) {
                    parseResponse(response, context, false);
                    HandleAPIResponse(response);
                } else {
                    final Response r = response;

                    if (null == context) return true;

                    Handler uiHandler = new Handler(context.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            parseResponse(r, context, true);
                            HandleAPIResponse(r);
                        }
                    };
                    uiHandler.post(myRunnable);
                }
            }
            return true;
        }

        public void setOnUiThread(boolean onUiThread) {
            mOnUiThread = onUiThread;
        }

        public boolean sendRequest(JSONObject j, String filePath, String formFieldName) {

            try {
                j.put("dt", String.valueOf(Mesibo.getDeviceType()));
            } catch (Exception e) {

            }
            int nwtype = Mesibo.getNetworkConnectivity();
            if (nwtype == 0xFF) {

            }

            http = new MesiboHttp();
            http.url = mApiUrl;
            try {
                http.post = j.toString().getBytes();
            } catch (Exception e) {
            }

            http.contentType = "application/json";
            http.uploadFile = filePath;
            http.uploadFileField = formFieldName;
            http.notifyOnCompleteOnly = true;
            http.concatData = true;
            http.listener = this;
            if (mBlocking) return http.executeAndWait();
            return http.execute();
        }

        public Context getContext() {
            return this.mContext;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public abstract void HandleAPIResponse(Response response);
    }

    public static class Urls {
        public String upload = "";
        public String download = "";
    }

    public static class Invite {
        public String text = "";
        public String subject = "";
        public String title = "";
    }

    public static class Contacts {
        public String name = "";
        public String phone = "";
        public long gid = 0;
        public long ts = 0;
        public String status = "";
        public String photo = "";
        public String tn = "";
        public String members = "";
    }

    public static class Response {
        public String result;
        public String op;
        public String error;
        public String errmsg;
        public String errtitle;
        public String message;
        public String title;
        public String token;
        public Contacts[] contacts;
        public String name;
        public String status;
        public String members;
        public String photo;
        public String phone;
        public String cc;

        public Urls urls;
        public Invite share;

        public long gid;
        public int type;
        public int profile;
        public long ts = 0;
        public String tn = null;

        Response() {
            result = null;
            op = null;
            error = null;
            errmsg = null;
            errtitle = null;
            token = null;
            contacts = null;
            gid = 0;
            type = 0;
            profile = 0;
            urls = null;
        }
    }


}
