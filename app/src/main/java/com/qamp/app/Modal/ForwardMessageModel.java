package com.qamp.app.Modal;

import com.mesibo.api.MesiboProfile;

public class ForwardMessageModel {
    private MesiboProfile profile;
    private boolean isCheck;

    public ForwardMessageModel(MesiboProfile profile, boolean isCheck) {
        this.profile = profile;
        this.isCheck = isCheck;
    }

    public MesiboProfile getProfile() {
        return profile;
    }

    public void setProfile(MesiboProfile profile) {
        this.profile = profile;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
