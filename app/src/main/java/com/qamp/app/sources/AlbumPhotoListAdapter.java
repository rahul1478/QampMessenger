package com.qamp.app.sources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qamp.app.R;

import java.io.File;
import java.net.URL;
import java.util.List;


public class AlbumPhotoListAdapter extends RecyclerView.Adapter<AlbumPhotoListAdapter.AlbumsViewHolder> {
    private static Context mContext;
    /* access modifiers changed from: private */
    public static AlbumPhotoListClickListener mListListener;
    private List<AlbumListData> mAlbumDataList;

    public interface AlbumPhotoListClickListener {
        void onItemClick(int i, View view);
    }

    public AlbumPhotoListAdapter(Context context, List<AlbumListData> topicList) {
        this.mAlbumDataList = topicList;
        mContext = context;
    }

    public static class AlbumsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ProgressBar mAlbuPrrogress;
        protected ImageView mAlbumFrontPicture;
        protected TextView mAlbumName;
        protected Boolean mHasImageDownloaded = false;
        protected TextView mTotalPictures;

        public AlbumsViewHolder(View v) {
            super(v);
            this.mAlbumName = (TextView) v.findViewById(com.mesibo.messaging.R.id.album_name);
            this.mTotalPictures = (TextView) v.findViewById(com.mesibo.messaging.R.id.album_count);
            this.mAlbumFrontPicture = (ImageView) v.findViewById(com.mesibo.messaging.R.id.profile);
            this.mAlbuPrrogress = (ProgressBar) v.findViewById(com.mesibo.messaging.R.id.al_progress);
            v.setOnClickListener(this);
        }

        public void onClick(View v) {
            AlbumPhotoListAdapter.mListListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public AlbumsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new AlbumsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(com.mesibo.messaging.R.layout.facebook_album_rv_item, viewGroup, false));
    }

    public void onBindViewHolder(AlbumsViewHolder holder, int i) {
        Bitmap b;
        AlbumListData td = this.mAlbumDataList.get(i);
        holder.mAlbumName.setText(td.getmAlbumName());
        holder.mTotalPictures.setText(td.getmPhotoCount() + " Pictures");
        String s = td.getmAlbumPictureUrl();
        if (s == null) {
            holder.mAlbumFrontPicture.setImageBitmap((Bitmap) null);
            holder.mAlbuPrrogress.setVisibility(8);
        } else if (!s.toLowerCase().startsWith("http://") && !s.toLowerCase().startsWith("https://")) {
            holder.mAlbuPrrogress.setVisibility(8);
            if (!new File(s).exists()) {
                holder.mAlbumFrontPicture.setImageBitmap(td.getmAlbumCoverPicture());
            } else if (SocialUtilities.isImageFile(s)) {
                holder.mAlbumFrontPicture.setImageBitmap(BitmapFactory.decodeFile(s));
            } else if (SocialUtilities.isVideoFile(s) && (b = SocialUtilities.createThumbnailAtTime(s, 0)) != null) {
                holder.mAlbumFrontPicture.setImageBitmap(b);
            }
            td.setmHasImageDownloaded(true);
        } else if (!td.getmHasImageDownloaded().booleanValue()) {
            holder.mAlbuPrrogress.setVisibility(0);
            DownloadImage request = new DownloadImage();
            request.setAlbumData(td);
            request.setHandler(holder);
            request.execute(new String[]{td.getmAlbumPictureUrl()});
        } else {
            holder.mAlbuPrrogress.setVisibility(8);
            holder.mAlbumFrontPicture.setImageBitmap(td.getmAlbumCoverPicture());
        }
    }

    public void addItem(AlbumListData dataObj, int index) {
        this.mAlbumDataList.add(dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        this.mAlbumDataList.remove(index);
        notifyItemRemoved(index);
    }

    public void setListener(AlbumPhotoListClickListener ml) {
        mListListener = ml;
    }

    public int getItemCount() {
        return this.mAlbumDataList.size();
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        AlbumListData albumData;
        AlbumsViewHolder handler;

        private DownloadImage() {
        }

        public void setHandler(AlbumsViewHolder handler1) {
            this.handler = handler1;
        }

        public void setAlbumData(AlbumListData ad1) {
            this.albumData = ad1;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            String lowerCase = imageURL.trim().toLowerCase();
            try {
                return BitmapFactory.decodeStream(new URL(imageURL).openStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap result) {
            this.handler.mAlbuPrrogress.setVisibility(8);
            this.handler.mAlbumFrontPicture.setImageBitmap(result);
            this.albumData.setmHasImageDownloaded(true);
            this.albumData.setmAlbumCoverPicture(result);
        }
    }
}
