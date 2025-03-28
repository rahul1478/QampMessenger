/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 2:35 AM
 *
 */

package com.qamp.app.MessagingModule;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;

import java.util.LinkedList;
import java.util.Queue;

public class MesiboMapScreenshot implements OnMapReadyCallback {
    Context mContext = null;
    GoogleMap mMap = null;
    Queue<Job> mQueue = new LinkedList();

    public interface Listener {
        boolean onMapScreenshot(MesiboMessage mesiboMessage, Bitmap bitmap);
    }

    public class Job {
        Listener listener = null;
        MesiboMessage msg = null;

        public Job() {
        }
    }

    public void start(Context context, Bundle savedInstanceState) {
        synchronized (this) {
            if (this.mContext == null) {
                this.mContext = context;
                GoogleMapOptions liteMode = new GoogleMapOptions().liteMode(true);
                MapView mp = new MapView(this.mContext);
                mp.onCreate((Bundle) null);
                mp.getMapAsync(this);
            }
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        if (!this.mQueue.isEmpty()) {
            take_screenshot(this.mQueue.peek());
        }
    }

    /* access modifiers changed from: private */
    public synchronized void take_screenshot(final Job job) {
        if (job != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(job.msg.latitude, job.msg.longitude), 16.0f);
            if (cameraUpdate != null) {
                this.mMap.moveCamera(cameraUpdate);
                this.mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    public void onSnapshotReady(Bitmap bitmap) {

                    }
                });
            }
        }
    }

    public boolean screenshot(MesiboMessage msg, Listener listener) {
        Job job = new Job();
        job.msg = msg;
        job.listener = listener;
        synchronized (this) {
            if (this.mMap == null || !this.mQueue.isEmpty()) {
                this.mQueue.add(job);
            } else {
                take_screenshot(job);
            }
        }
        return true;
    }
}
