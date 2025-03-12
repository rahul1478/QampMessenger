package com.qamp.app.sources;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qamp.app.R;

import java.io.File;
import java.util.ArrayList;


public class zoomVuPictureActivity extends AppCompatActivity {
    private static final String TAG = "zoomVuPictureActivity";
    public static String filePath;
    public ArrayList<String> mImageArraylist = null;
    ImageFragmentPagerAdapter mImageFragmentPagerAdapter = null;
    int mStartPosition = 0;
    ViewPager mViewPager = null;

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        getWindow().requestFeature(9);
        zoomVuPictureActivity.super.onCreate(savedInstanceState);
        setContentView(com.mesibo.mediapicker.R.layout.fragment_pager);
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        this.mImageArraylist = intent.getStringArrayListExtra("stringImageArray");
        this.mStartPosition = intent.getIntExtra("position", 0);
        if (this.mImageArraylist == null) {
            z = true;
        } else {
            z = false;
        }
        if (z && (filePath != null)) {
            this.mImageArraylist = new ArrayList<>();
            this.mImageArraylist.add(filePath);
            this.mStartPosition = 0;
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("title"));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(MediaPicker.getToolbarColor()));
        }
        this.mImageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager(), this.mImageArraylist);
        this.mViewPager = findViewById(com.mesibo.mediapicker.R.id.pager);
        this.mViewPager.setOffscreenPageLimit(1);
        this.mViewPager.setAdapter(this.mImageFragmentPagerAdapter);
        this.mViewPager.setCurrentItem(this.mStartPosition);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            default:
                return zoomVuPictureActivity.super.onOptionsItemSelected(item);
        }
    }

    public class ImageFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<String> mImageArray;

        public ImageFragmentPagerAdapter(FragmentManager fm, ArrayList<String> list) {
            super(fm);
            this.mImageArray = list;
        }

        public int getCount() {
            if (this.mImageArray == null) {
                return 0;
            }
            return this.mImageArray.size();
        }

        public Fragment getItem(int position) {
            SwipeFragment fragment = new SwipeFragment();
            return fragment.newInstance(this.mImageArray, position);
        }
    }

    public static class SwipeFragment extends Fragment {
        RelativeLayout mTouchLayout;

        public SwipeFragment() {
            // Required empty public constructor
        }

        @SuppressLint("RestrictedApi")
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.zoomvu_layout, container, false);
            ImageViewTouch imageView = (ImageViewTouch) swipeView.findViewById(R.id.imageViewz);
            Bundle bundle = getArguments();
            final String imageFileName = bundle.getStringArrayList("images").get(bundle.getInt("position"));
            if (SocialUtilities.isImageFile(imageFileName)) {
                ((LinearLayout) swipeView.findViewById(R.id.video_layer)).setVisibility(8);
                ImageViewTouch touchLayout = (ImageViewTouch) swipeView.findViewById(R.id.imageViewz);
                if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);
                }
                touchLayout.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ActionBar actionBar;
                        AppCompatActivity activity = (AppCompatActivity) SwipeFragment.this.getActivity();
                        if (activity != null && (actionBar = activity.getSupportActionBar()) != null) {
                            if (actionBar.isShowing()) {
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    }
                });
                imageView.setImageBitmap(BitmapFactory.decodeFile(imageFileName));
            } else if (SocialUtilities.isVideoFile(imageFileName)) {
                LinearLayout v = (LinearLayout) swipeView.findViewById(R.id.video_layer);
                v.setVisibility(0);
                Bitmap b = SocialUtilities.createThumbnailAtTime(imageFileName, 0);
                if (b != null) {
                    imageView.setImageBitmap(b);
                }
                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        File playFile = new File(imageFileName);
                        if (playFile.exists()) {
                            intent.setDataAndType(Uri.fromFile(playFile), "video/mp4");
                            SwipeFragment.this.startActivity(intent);
                            return;
                        }
                        Toast.makeText(SwipeFragment.this.getActivity(), "The file doesn´t exist!", 1).show();
                    }
                });
            }
            return swipeView;
        }

//        public void onResume() {
//            super.onResume();
//            zoomVuPictureActivity.super.onResume();
//            if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
//                ((AppCompatActivity)getActivity()).getSupportActionBar().show();
//            }
//        }

         SwipeFragment newInstance(ArrayList<String> ImageArray, int position) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("images", ImageArray);
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }
    }
}
