package com.qamp.app.sources;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.mediapicker.RecyclerItemClickListener;
import com.qamp.app.R;

import java.util.ArrayList;
import java.util.List;


public class GalleryPhotoGridFragment extends BaseFragment {
    /* access modifiers changed from: private */
    public static List<AlbumPhotosData> mPhotoList;
    private GalleryPhotoGridAdapter mAdapter;
    private OnFragmentInteractionListener mListener = null;
    private RecyclerView mRecyclerView;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public interface facebookPicturecallback {
        void facebookPictureSelected(String str);
    }

    public void passPhotoListData(List<AlbumPhotosData> photoList) {
        mPhotoList = photoList;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.mesibo.messaging.R.layout.content_facebook_photogrid, container, false);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(MediaPicker.getToolbarColor()));
        ab.setTitle("Select picture");
        this.mRecyclerView = view.findViewById(com.mesibo.messaging.R.id.photogrid_rv);
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        this.mAdapter = new GalleryPhotoGridAdapter(getActivity(), mPhotoList);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.SimpleOnItemClickListener() {
            public void onItemClick(View view, int position) {
                String checkUrl = ((AlbumPhotosData) GalleryPhotoGridFragment.mPhotoList.get(position)).getmSourceUrl();
                if (checkUrl.startsWith("http://") || checkUrl.startsWith("https://")) {
                    AlbumStartActivity activity = (AlbumStartActivity) requireActivity();
                    activity.facebookPictureSelected(SocialUtilities.createImageFromBitmap(activity.getApplicationContext(), ((AlbumPhotosData) GalleryPhotoGridFragment.mPhotoList.get(position)).getmGridPicture()));
                }
                ArrayList<String> stringImageArray = new ArrayList<>();
                for (int i = 0; i < GalleryPhotoGridFragment.mPhotoList.size(); i++) {
                    stringImageArray.add(((AlbumPhotosData) GalleryPhotoGridFragment.mPhotoList.get(i)).getmSourceUrl());
                }
                Intent intent = new Intent(GalleryPhotoGridFragment.this.getActivity(), zoomVuPictureActivity.class);
                intent.putExtra("position", position);
                intent.putStringArrayListExtra("stringImageArray", stringImageArray);
                GalleryPhotoGridFragment.this.startActivity(intent);
            }
        }));
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }
}
