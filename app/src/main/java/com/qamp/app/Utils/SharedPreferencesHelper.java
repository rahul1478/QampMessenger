package com.qamp.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mesibo.api.MesiboProfile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "MesiboProfilePrefs";
    private static final String KEY_PROFILE_ADDRESSES = "profileAddresses";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveMesiboProfileAddresses(Set<String> profileAddresses) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_PROFILE_ADDRESSES, profileAddresses != null ? profileAddresses : new HashSet<>());
        editor.apply();
    }

    public Set<String> getMesiboProfileAddresses() {
        Set<String> profileAddresses = sharedPreferences.getStringSet(KEY_PROFILE_ADDRESSES, new HashSet<>());
        return profileAddresses;
    }

    public void addMesiboProfileAddress(String profileAddress) {
        Set<String> profileAddresses = getMesiboProfileAddresses();
        profileAddresses.add(profileAddress);
        saveMesiboProfileAddresses(profileAddresses);
    }

    public void removeMesiboProfileAddress(String profileAddress) {
        Set<String> profileAddresses = getMesiboProfileAddresses();
        profileAddresses.remove(profileAddress);
        saveMesiboProfileAddresses(profileAddresses);
    }


}