package com.qamp.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qamp.app.Activity.OnBoardingProfile;
import com.qamp.app.R;

public class OnboardingBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private LinearLayout cameraButton, galleryButton;
    private ImageView removeButton;
    private LinearLayout delete_layout;
    private boolean isPhoto;
    private static final int CAMERA_VALUE = 1;
    private static final int GALLERY_VALUE = 2;
    private static final int REMOVE_VALUE = 3;


    public OnboardingBottomSheetFragment() {
        // Required empty public constructor
    }

    public OnboardingBottomSheetFragment(boolean isPhoto) {
        this.isPhoto = isPhoto;
    }

    public static OnboardingBottomSheetFragment newInstance(String param1, String param2) {
        OnboardingBottomSheetFragment fragment = new OnboardingBottomSheetFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_onboarding_bottom_sheet, container, false);
        cameraButton = (LinearLayout) v.findViewById(R.id.camera_container);
        galleryButton = (LinearLayout) v.findViewById(R.id.gallery_container);
        removeButton = (ImageView) v.findViewById(R.id.remove_button);
        delete_layout = (LinearLayout) v.findViewById(R.id.delete_layout);

        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);
        delete_layout.setVisibility(View.VISIBLE);

        if (isPhoto) {
            delete_layout.setVisibility(View.VISIBLE);
        } else {
            delete_layout.setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        int id = view.getId();

        if (id == R.id.camera_container) {
            intent.putExtra("buttonTapped", CAMERA_VALUE);
            this.dismiss();
            ((OnBoardingProfile) getActivity()).openCamera();
        } else if (id == R.id.gallery_container) {
            intent.putExtra("buttonTapped", GALLERY_VALUE);
            this.dismiss();
            ((OnBoardingProfile) getActivity()).openGallery();
        } else if (id == R.id.remove_button) {
            intent.putExtra("buttonTapped", REMOVE_VALUE);
            this.dismiss();
            ((OnBoardingProfile) getActivity()).removeProfilePic();
        }
    }


}