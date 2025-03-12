package com.qamp.app.sources;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qamp.app.R;


public class AlbumPhotoListFragment extends BaseFragment {
    private AlbumPhotoListAdapter mAdapter;
    private OnFragmentInteractionListener mListener = null;
    private RecyclerView mRecyclerView;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.mesibo.messaging.R.layout.content_facebook_album, container, false);
        ImagePicker instance = ImagePicker.getInstance();
        this.mRecyclerView = view.findViewById(com.mesibo.messaging.R.id.album_rv);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mAdapter = new AlbumPhotoListAdapter(getActivity(), MediaPicker.getAlbumList());
        this.mRecyclerView.setAdapter(this.mAdapter);
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onResume() {
        super.onResume();
        this.mAdapter.setListener(new AlbumPhotoListAdapter.AlbumPhotoListClickListener() {
            public void onItemClick(int position, View v) {
                Log.i("LOG_TAG", " Clicked on Item " + position);
                FragmentTransaction ft = AlbumPhotoListFragment.this.getFragmentManager().beginTransaction();
                new GalleryPhotoGridFragment().passPhotoListData(MediaPicker.getAlbumList().get(position).getmPhotosList());
                ft.replace(R.id.fb_fragment, new GalleryPhotoGridFragment());
                ft.addToBackStack("AlbumPhotoListFragment");
                ft.commit();
            }
        });
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }
}
