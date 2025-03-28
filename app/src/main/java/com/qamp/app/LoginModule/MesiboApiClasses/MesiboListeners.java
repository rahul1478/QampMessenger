/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 3:25 AM
 *
 */

package com.qamp.app.LoginModule.MesiboApiClasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboDateTime;
import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
//import com.mesibo.contactutils.ContactUtils;
import com.qamp.app.MainApplication;

import java.util.ArrayList;

public class MesiboListeners implements Mesibo.ConnectionListener, Mesibo.MessageListener, MesiboUI.Listener, Mesibo.MessageFilter, Mesibo.ProfileListener, Mesibo.CrashListener, MesiboRegistrationIntentService.GCMListener, MesiboCall.IncomingListener, Mesibo.GroupListener, Mesibo.AppStateListener, Mesibo.EndToEndEncryptionListener {
    public static final String TAG = "MesiboListeners";
    public static Context mLoginContext = null;
    private static Gson mGson = new Gson();

//    @Override
//    public boolean onLogin(Context p0, String p1, String p2, ILoginResultsInterface p3) {
//        return false;
//    }

    public static class MesiboNotification {
        public String subject;
        public String msg;
        public String type;
        public String action;
        public String name;
        public long gid;
        public String phone;
        public String status;
        public String members;
        public String photo;
        public long ts;
        public String tn;

        MesiboNotification() {
        }
    }

   // ILoginResultsInterface mILoginResultsInterface = null;
    Handler mGroupHandler = null;
    String mCode = null;
    String mPhone = null;
    boolean mSyncDone = false;
    Context mUserListContext = null;
    Context mMessageContext = null;
    Context mLastContext = null;

    @SuppressWarnings("all")
    private SampleAPI.ResponseHandler mHandler = new SampleAPI.ResponseHandler() {
        @Override
        public void HandleAPIResponse(SampleAPI.Response response) {
            Log.d(TAG, "Respose: " + response);
            if (null == response)
                return;

            if (response.op.equals("login")) {
                if (!TextUtils.isEmpty(SampleAPI.getToken())) {
                    MesiboProfile u = Mesibo.getSelfProfile();

                    if (TextUtils.isEmpty(u.getName())) {
                       // UIManager.launchEditProfile(mLoginContext, 0, 0, true);
                    } else {
                        UIManager.launchMesibo(mLoginContext, 0, false, true);
                    }
                }

//                if(null != mILoginResultsInterface && null == response.errmsg)
//                    mILoginResultsInterface.onLoginResult(response.result.equals("OK"), -1);

            } else if (response.op.equals("setgroup")) {

                if(null != mGroupHandler) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupid", response.gid);
                    bundle.putString("result", response.result);
                    msg.setData(bundle);
                    mGroupHandler.handleMessage(msg);
                }
            } else if (response.op.equals("getgroup")) {

                if(null != mGroupHandler) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", response.result);
                    msg.setData(bundle);
                    mGroupHandler.handleMessage(msg);
                }
            }
            //handleAPIResponse(response);
        }
    };

    @Override
    public void Mesibo_onConnectionStatus(int status) {
        Log.d(TAG, "Mesibo_onConnectionStatus: " + status);
        if (Mesibo.STATUS_SIGNOUT == status) {
            UIManager.showAlert(mUserListContext, "Signed Out", "You have signed-in from other device and hence signed out here");
            SampleAPI.forceLogout();
        } else if (Mesibo.STATUS_AUTHFAIL == status) {
            UIManager.showAlert(mUserListContext, "Signed Out", "Login Expired. Login again to continue.");
         //   SampleAPI.forceLogout();
        }

        if(Mesibo.STATUS_ONLINE == status) {
            SampleAPI.startOnlineAction();
        }
    }

    @Override
    public void Mesibo_onEndToEndEncryption(MesiboProfile profile, int status) {
        Log.d(TAG, "Mesibo_onEndToEndEncryption: " + status);
    }

    @Override
    public void Mesibo_onMessage(MesiboMessage msg) {
        if(!msg.isRealtimeMessage() || Mesibo.MSGSTATUS_OUTBOX == msg.getStatus())
            return;

        if(msg.isEndToEndEncryptionStatus())
            return;

        // if(Mesibo.isAppInForeground()) return true;

        if(Mesibo.isReading(msg))
            return;

        String message = msg.message;
        if(TextUtils.isEmpty(message))
            message = msg.title;
        if(TextUtils.isEmpty(message) && msg.hasImage())
            message = "Picture";
        if(TextUtils.isEmpty(message) && msg.hasVideo())
            message = "Video";
        if(TextUtils.isEmpty(message) && msg.hasAudio())
            message = "Audio";
        if(TextUtils.isEmpty(message) && msg.hasDocument())
            message = "Attachment";
        if(TextUtils.isEmpty(message) && msg.hasLocation())
            message = "Location";

        SampleAPI.notify(msg, message);
        return;
    }

    @Override
    public void Mesibo_onMessageStatus(MesiboMessage params) {
    }

    @Override
    public void Mesibo_onMessageUpdate(MesiboMessage mesiboMessage) {

    }

    @Override
    public void Mesibo_onProfileUpdated(MesiboProfile userProfile) {

    }



//    @Override
//    public boolean Mesibo_onGetProfile(MesiboProfile profile) {
//        if(null == profile) {
//            return false;
//        }
//
//        if(!profile.isActive()) {
//            if(profile.groupid > 0) {
//                profile.lookedup = true; //else getProfile will be recursive call
//                //SampleAPI.updateDeletedGroup(profile.groupid);
//                return true;
//            }
//        }
//
//        if(profile.groupid > 0) {
//            return true;
//        }
//
//        if(!TextUtils.isEmpty(profile.address)) {
//            long ts = Mesibo.getTimestamp();
//            String name = ContactUtils.reverseLookup(profile.address);
//            if(null == name) {
//                return mSyncDone;
//            }
//
//            profile.setOverrideName(name);
//            return true;
//        }
//
//        return false;
//    }

//    @Override
//    public void MesiboUI_onShowProfile(Context context, MesiboProfile userProfile) {
//        UIManager.launchUserProfile(context, userProfile.groupid, userProfile.address);
//    }

    @Override
    public boolean MesiboUI_onShowLocation(Context context, MesiboProfile profile) {
        return false;
    }

    @Override
    public void MesiboUI_onShowProfile(Context context, MesiboProfile mesiboProfile) {

    }

    @Override
    public int MesiboUI_onGetMenuResourceId(Context context, int type, MesiboProfile profile, Menu menu) {
        int id = 0;
        if (type == 0) { // Setting menu in userlist
            //id = R.menu.messaging_activity_menu;
            mUserListContext = context;
        }
        else {
            //id = R.menu.menu_messaging;
            mMessageContext = context;
        }

        ((Activity)context).getMenuInflater().inflate(id, menu);

        if(1 == type && null != profile && profile.isGroup()) {
//            MenuItem menuItem = menu.findItem(R.id.action_call);
//            if(!profile.isActive()) menuItem.setVisible(false);
//            menuItem.setIcon(R.drawable.ic_mesibo_groupcall_audio);
           // MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);

//            menuItem = menu.findItem(R.id.action_videocall);
//            menuItem.setIcon(R.drawable.ic_mesibo_groupcall_video);
//            if(!profile.isActive()) menuItem.setVisible(false);
            //MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        }

        return 0;
    }

    @Override
    public boolean MesiboUI_onMenuItemSelected(Context context, int type, MesiboProfile profile, int item) {
        if(null == context)
            return false;

        if (type == 0) { // from userlist
//            if (item == R.id.action_settings) {
//                UIManager.launchUserSettings(context);
//            } else if(item == R.id.action_conf) {
//                MesiboCall.getInstance().groupCallJoinRoomUi(context, "Mesibo Conferencing Demo");
//            } else if(item == R.id.action_calllogs) {
//                //MesiboCallUi.getInstance().launchCallLogs(context, 0);
//            } else if(item == R.id.action_menu_e2ee) {
//                MesiboUI.showEndToEndEncryptionInfoForSelf(context);
//            } else if(item == R.id.mesibo_share) {
//                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                sharingIntent.setType("text/plain");
//                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, AppConfig.getConfig().invite.subject);
//                sharingIntent.putExtra(Intent.EXTRA_TEXT, AppConfig.getConfig().invite.text);
//                context.startActivity(Intent.createChooser(sharingIntent, AppConfig.getConfig().invite.title));
//            }
        } 
	else { // from MessagingModule box
            if(null == profile) {
                return false;
            }
//            if(R.id.action_call == item) {
//                if(!MesiboCall.getInstance().callUi(context, profile, false))
//                    MesiboCall.getInstance().callUiForExistingCall(context);
//            }
//            else if(R.id.action_videocall == item) {
//                if(!MesiboCall.getInstance().callUi(context, profile, true))
//                    MesiboCall.getInstance().callUiForExistingCall(context);
//            }
//            else if(R.id.action_e2e == item) {
//                MesiboUI.showEndToEndEncryptionInfo(context, profile.getAddress(), profile.groupid);
//            }
        }

        return false;
    }

    //Note this is not in UI thread
    @Override
    public boolean Mesibo_onMessageFilter(MesiboMessage msg) {

        // using it for notifications
        if(1 != msg.type || msg.isCall())
            return true;

        return false;
    }

//    @Override
//    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile userProfile, boolean video) {
//        MesiboCall.CallProperties cc = MesiboCall.getInstance().createCallProperties(video);
//        cc.parent = getApplicationContext();
//        cc.user = userProfile;
//        return cc;
//    }

    @Override
    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile mesiboProfile, boolean b, boolean b1) {
        return null;
    }

    @Override
    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile profile, boolean video){
        // In this example, we use video as a filter to accept video calls only
        if(!video)
            return null; //Accept video calls only

        if(profile.address == null || profile.address.isEmpty())
            return null;

        // your app specific function to check if the caller is allowed

        // Define call properties
        MesiboCall.CallProperties cp = MesiboCall.getInstance().createCallProperties(true);

        // Define optional parameters
        cp.video.bitrate = 2000; //bitrate in kbps

        return cp;
    }

    @Override
    public boolean MesiboCall_OnShowUserInterface(MesiboCall.Call call, MesiboCall.CallProperties callProperties) {
        launchCustomCallActivity(callProperties.user.address, callProperties.video.enabled, true);
        return true;
    }


    protected void launchCustomCallActivity(String destination, boolean video, boolean incoming) {
        //Intent intent = new Intent(getApplicationContext(), MesiboDefaultCallActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //intent.putExtra("video", video);
        //intent.putExtra("address", destination);
        //intent.putExtra("incoming", incoming);
        //startActivity(intent);
    }

    @Override
    public void MesiboCall_OnError(MesiboCall.CallProperties callProperties, int error) {
    }

    @Override
    public boolean MesiboCall_onNotify(int i, @NonNull MesiboProfile mesiboProfile, boolean b, @NonNull MesiboDateTime mesiboDateTime) {
        return false;
    }


    @Override
    public void Mesibo_onGroupCreated(MesiboProfile groupProfile) {
        Log.d(TAG, "New group " + groupProfile.groupid);
    }

    @Override
    public void Mesibo_onGroupJoined(MesiboProfile groupProfile) {
        SampleAPI.notify(3, "Joined a group", "You have been added to the group " + groupProfile.getName());
    }

    @Override
    public void Mesibo_onGroupLeft(MesiboProfile groupProfile) {
        SampleAPI.notify(3, "Left a group", "You left the group " + groupProfile.getName());
    }

    @Override
    public void Mesibo_onGroupMembers(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersJoined(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersRemoved(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupSettings(MesiboProfile mesiboProfile, MesiboGroupProfile.GroupSettings groupSettings, MesiboGroupProfile.MemberPermissions memberPermissions, MesiboGroupProfile.GroupPin[] groupPins) {

    }

    @Override
    public void Mesibo_onGroupError(MesiboProfile mesiboProfile, long l) {

    }

    @Override
    public void Mesibo_onForeground(boolean foreground) {
        if(foreground && MesiboCall.getInstance().isCallInProgress() && null != mLastContext) {
            MesiboCall.getInstance().callUiForExistingCall(mLastContext);
        }
    }

    @Override
    public void Mesibo_onForeground(Context context, int screenId, boolean foreground) {


        //userlist is in foreground
        if(foreground && 0 == screenId) {
            //notify count clear
            SampleAPI.notifyClear();
        }

        // if app restarted
        if(foreground && MesiboCall.getInstance().isCallInProgress()  && null == mLastContext) {
            MesiboCall.getInstance().callUiForExistingCall(context);
        }

        if(foreground) mLastContext= context;
    }

    @Override
    public void Mesibo_onCrash(String crashLogs) {
        Log.e(TAG, "Mesibo_onCrash: " + ((null != crashLogs)?crashLogs:""));
        //restart application
        Intent i = new Intent(MainApplication.getAppContext(), StartUpActivity.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.putExtra(StartUpActivity.STARTINBACKGROUND, !Mesibo.isAppInForeground()); ////Maintain the state of the application
        MainApplication.getAppContext().startActivity(i);
    }





    /**@Override
    public boolean onLogin(Context context, String phone, String code, ILoginResultsInterface iLoginResultsInterface) {
        mLoginContext = context;
        mILoginResultsInterface = iLoginResultsInterface;
        mCode = code;
        mPhone = phone;
        mHandler.setContext(context);
        SampleAPI.login(phone, code, mHandler);
        return false;
    }**/
    public boolean onLogin(Context context, String phone, String code) {
        mLoginContext = context;
        mCode = code;
        mPhone = phone;
        mHandler.setContext(context);
//        MesiboAPI.login(phone, code, mHandler);
        return false;
    }

    private static MesiboListeners _instance = null;
    public static MesiboListeners getInstance() {
        if(null==_instance)
            synchronized(MesiboListeners.class) {
                if(null == _instance) {
                    _instance = new MesiboListeners();
                }
            }

        return _instance;
    }


    private static ArrayList<String> mContactsToSync = new ArrayList<String>();
    private static ArrayList<String> mDeletedContacts = new ArrayList<String>();
    private long mSyncTs = 0;
//    @Override
//    public boolean ContactUtils_onContact(String[] phoneNumbers, boolean deleted, String contacts, long ts) {
//        mSyncDone = true;
//        Mesibo.updateLookups();
//        if(null == phoneNumbers) return true;
//
//        int maxcount = 100; // we are limiting count to conserve memory
//        String[] phones = new String[maxcount];
//        for(int i=0; i < phoneNumbers.length; ) {
//            if((phoneNumbers.length - i) < maxcount) {
//                maxcount = (phoneNumbers.length - i);
//                phones = new String[maxcount];
//            }
//
//            for(int j=0; j < maxcount; j++)
//                phones[j] = phoneNumbers[i++];
//
//
//            Mesibo.syncContacts(phones, !deleted, true, 0, true);
//        }
//
//        if(phoneNumbers.length > 0)  Mesibo.syncContacts();
//
//        if(!deleted)  SampleAPI.saveLocalSyncedContacts((String) contacts,(long) ts);
//        return true;
//    }


    @Override
    public void Mesibo_onGCMToken(String token) {
        SampleAPI.setGCMToken(token);
    }

    @Override
    public void Mesibo_onGCMMessage(/*Bundle data, */boolean inService) {
        SampleAPI.onGCMMessage(inService);
    }
}
