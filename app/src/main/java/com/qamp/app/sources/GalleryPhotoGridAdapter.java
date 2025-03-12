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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.mediapicker.GalleryPhotoGridFragment;
import com.qamp.app.R;

import java.io.File;
import java.net.URL;
import java.util.List;


public class GalleryPhotoGridAdapter extends RecyclerView.Adapter<GalleryPhotoGridAdapter.PhotosViewHolder> {
    private static Context mContext;
    private List<AlbumPhotosData> mPhotoList;

    public GalleryPhotoGridAdapter(Context context, List<AlbumPhotosData> photoList) {
        this.mPhotoList = photoList;
        mContext = context;
    }

    public static class PhotosViewHolder extends RecyclerView.ViewHolder {
        ImageView mAlbumFrontPicture;
        ProgressBar mPhotoProgress;
        ImageView mVideoLayer;

        public PhotosViewHolder(View v) {
            super(v);
            this.mAlbumFrontPicture = (ImageView) v.findViewById(com.mesibo.messaging.R.id.photogrid_picture);
            this.mPhotoProgress = (ProgressBar) v.findViewById(com.mesibo.messaging.R.id.pl_progress);
            this.mVideoLayer = (ImageView) v.findViewById(com.mesibo.messaging.R.id.photogrid_video_layer);
        }
    }

    public PhotosViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new PhotosViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(com.mesibo.messaging.R.layout.gallery_photogrid_rv_item, viewGroup, false));
    }


    public void onBindViewHolder(PhotosViewHolder holder, int i) {
        Bitmap b;
        AlbumPhotosData td = this.mPhotoList.get(i);
        holder.mVideoLayer.setVisibility(8);
        String s = td.getmSourceUrl();
        if (!s.toLowerCase().startsWith("http://") && !s.toLowerCase().startsWith("https://")) {
            holder.mPhotoProgress.setVisibility(8);
            if (s.toLowerCase().endsWith(".pdf")) {
                holder.mAlbumFrontPicture.setImageResource(com.mesibo.messaging.R.drawable.file_pdf);
            } else if (SocialUtilities.isImageFile(s)) {
                holder.mAlbumFrontPicture.setImageBitmap(BitmapFactory.decodeFile(s));
            } else if (SocialUtilities.isVideoFile(s) && (b = SocialUtilities.createThumbnailAtTime(s, 0)) != null) {
                holder.mAlbumFrontPicture.setImageBitmap(b);
                holder.mVideoLayer.setVisibility(0);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "working", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (!td.getmHasDownloadedGP().booleanValue()) {
            DownloadImage request = new DownloadImage();
            request.setAlbumData(td);
            request.setHandler(holder);
            request.execute(new String[]{td.getmSourceUrl()});
        } else {
            holder.mPhotoProgress.setVisibility(8);
            holder.mAlbumFrontPicture.setImageBitmap(td.getmGridPicture());
        }

    }

    public void addItem(AlbumPhotosData dataObj, int index) {
        this.mPhotoList.add(dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        this.mPhotoList.remove(index);
        notifyItemRemoved(index);
    }

    public int getItemCount() {
        return this.mPhotoList.size();
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        AlbumPhotosData albumData;
        PhotosViewHolder handler;

        private DownloadImage() {
        }

        public void setHandler(PhotosViewHolder handler1) {
            this.handler = handler1;
        }

        public void setAlbumData(AlbumPhotosData ad1) {
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
            this.handler.mAlbumFrontPicture.setImageBitmap(result);
            this.handler.mPhotoProgress.setVisibility(8);
            this.albumData.setmHasDownloadedGP(true);
            this.albumData.setmGridPicture(result);
        }
    }


}
