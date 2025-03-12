package com.qamp.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.Adapter.ForwardMessageAdapter;
import com.qamp.app.MesiboImpModules.ContactSyncClass;
import com.qamp.app.MessagingModule.MesiboImages;
import com.qamp.app.MessagingModule.MesiboUIManager;
import com.qamp.app.Modal.ForwardMessageModel;
import com.qamp.app.Modal.QampContactScreenModel;
import com.qamp.app.R;
import com.qamp.app.Utils.AppUtils;
import com.qamp.app.sources.MediaPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ForwardActivity extends AppCompatActivity implements ForwardMessageAdapter.ForwardMessageClickListener, MediaPicker.ImageEditorListener {

    private RecyclerView recyclerView;
    private ForwardMessageAdapter adapter;
    private RelativeLayout forward_layout;
    private TextView names;
    private ImageButton caption_send;
    private String  videosPath;
    private Long message;
    private ArrayList<Long> messagesId = new ArrayList<>();
    private String pdfFilePath;
    private String  imageBitmap;
    private String audioFilePath;
    private ImageView backButton;
    private ArrayList<MesiboProfile> mesiboProfiles = new ArrayList<>();

    private ArrayList<ForwardMessageModel> model = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setStatusBarColor(ForwardActivity.this, R.color.colorAccent);
        setContentView(R.layout.activity_forward);

        Intent intent = getIntent();

        // Check if the intent action is SEND and the data type is an image
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if (intent.getType().startsWith("image/")) {
                // Handle the received image data
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    String imageBitmapPath = getImagePathFromWhatsAppUri(imageUri);
                    Log.e("ImageBitmap",imageBitmapPath.toString());
                    if (imageBitmapPath != null) {
                       imageBitmap = imageBitmapPath;

                    }
                }
            } else if (intent.getType().startsWith("video/")) {
                // Handle the received video data
                Uri videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (videoUri != null) {
                    String videoPath = getVideoPathFromUri(videoUri);
                    Log.e("VideoPath", videoPath != null ? videoPath : "Video path is null");

                    if (videoPath != null) {
                        videosPath = videoPath;
                        // Now, videosPath contains the actual file path of the video.
                        // You can use this path to play or process the video.
                    }
                }
            } else if (intent.getType().startsWith("application/pdf")) {
                // Debug information to log the received MIME type
                Log.e("Received MIME Type", intent.getType());

                Uri pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (pdfUri != null) {
                    String pdfPath = getPdfPathFromUri(pdfUri);
                    Log.e("PdfPath", pdfPath != null ? pdfPath : "PDF path is null");

                    if (pdfPath != null) {
                        pdfFilePath = pdfPath;
                        // Now, pdfFilePath contains the actual file path of the PDF.
                        // You can use this path to process the PDF.
                    }
                }
            }else if (intent.getType().startsWith("audio/")) {
                // Handle the received audio data
                Uri audioUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (audioUri != null) {
                    String audioPath = getAudioPathFromUri(audioUri);
                    Log.e("AudioPath", audioPath != null ? audioPath : "Audio path is null");

                    if (audioPath != null) {
                        audioFilePath = audioPath;
                        // Now, audioFilePath contains the actual file path of the audio.
                        // You can use this path to play or process the audio.
                    }
                }
            }
        }

        this.message = getIntent().getLongExtra("message",0L);

        try {
            this.messagesId =  (ArrayList<Long>) getIntent().getSerializableExtra("messagesID");

        }catch (NullPointerException e){
            this.messagesId = null;
        }


//        this.messagesId.forEach(aLong -> {
//            Log.e("messageId",aLong.toString());
//        });


        forward_layout = findViewById(R.id.forward_layout);
        names = findViewById(R.id.names);
        caption_send = findViewById(R.id.caption_send);
        backButton = findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.forward_recycler_list);
        RecyclerView.LayoutManager mLayoutInflator = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutInflator);

        ArrayList<QampContactScreenModel> profile = ContactSyncClass.groupContacts;
        profile.forEach(profile1 -> {
            MesiboProfile profile2 = Mesibo.getProfile(profile1.getMes_rv_phone());
           ForwardMessageModel model1 = new ForwardMessageModel(profile2,false);
           model.add(model1);

        });



        adapter = new ForwardMessageAdapter(getApplicationContext(),model,message,this);
        recyclerView.setAdapter(adapter);
    }

    private String getAudioPathFromUri(Uri uri) {
        String audioPath = null;
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                if (columnIndex != -1) {
                    audioPath = cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur during the query.
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return audioPath;
    }

    public Bitmap uriToBitmap(Uri uri) throws IOException {
        // Use ContentResolver to open an input stream from the URI
        InputStream imageStream = getContentResolver().openInputStream(uri);

        // Decode the input stream into a Bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

        // Close the input stream
        if (imageStream != null) {
            imageStream.close();
        }

        return bitmap;
    }


    @Override
    public void onItemClicked(ArrayList<MesiboProfile> profiles,Long message) {
        if (!profiles.isEmpty()) {
            forward_layout.setVisibility(View.VISIBLE);

            StringBuilder namesStringBuilder = new StringBuilder();
            for (MesiboProfile mesiboProfile : profiles) {
                namesStringBuilder.append(mesiboProfile.getName()).append(", ");
            }

            // Remove the trailing comma and space
            String namesText = namesStringBuilder.toString().trim();
            if (namesText.endsWith(",")) {
                namesText = namesText.substring(0, namesText.length() - 1);
            }

            mesiboProfiles.clear();
            profiles.forEach(mesiboProfile -> {
                mesiboProfiles.add(mesiboProfile);
            });

            names.setText(namesText);

            caption_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    profiles.forEach(mesiboProfile -> {
                        if (imageBitmap != null){
//
                            int drawableid = -1;
                            MesiboUIManager.launchImageEditor(ForwardActivity.this, 10000, drawableid, (String) null, imageBitmap, true, true, false, false, 1280, ForwardActivity.this);

                        }
                        else if (videosPath != null){
                            int drawableid = -1;
                            MesiboUIManager.launchImageEditor(ForwardActivity.this, 10002, drawableid, (String) null, videosPath, true, true, false, false, 1280, ForwardActivity.this);
                        }else if (pdfFilePath != null){
                            int drawableid = -1;
                            drawableid = MesiboImages.getFileDrawable(pdfFilePath);
                            MesiboUIManager.launchImageEditor(ForwardActivity.this, 10005, drawableid, (String) null, pdfFilePath, true, true, false, false, 1280, ForwardActivity.this);

                        }else if (audioFilePath != null){
                            int drawableid = -1;
                            drawableid = R.drawable.file_audio;
                            MesiboUIManager.launchImageEditor(ForwardActivity.this, 10006, drawableid, (String) null, audioFilePath, true, true, false, false, 1280, ForwardActivity.this);

                        }
                        else {
                            if (messagesId == null){
                                    MesiboMessage msg = mesiboProfile.newMessage();
                                    msg.setForwarded(message);
                                    msg.send();
                                    onBackPressed();
                                }else {
                                    messagesId.forEach(aLong -> {
                                        MesiboMessage msg = mesiboProfile.newMessage();
                                        msg.setForwarded(aLong);
                                        msg.send();
                                    });

                                    onBackPressed();
                                }


                        }

                    });

                }
            });
        } else {
            forward_layout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onImageEdit(int type, String caption, String filePath, Bitmap bmp, int result) {
        Log.e("interface","00");
        if (0 == result) {
            this.sendFile(type, caption, filePath, bmp, result);
        }
    }

    private void sendFile(int type, String caption, String filePath, Bitmap bmp, int result) {
       mesiboProfiles.forEach(mesiboProfile -> {
           MesiboMessage newMessage = mesiboProfile.newMessage();
           if (null == bmp || MediaPicker.TYPE_FILEIMAGE != type && MediaPicker.TYPE_CAMERAIMAGE != type) {
               newMessage.setContent(filePath);
           } else {
               newMessage.setContent(bmp);
           }
           newMessage.message = caption;
           newMessage.send();
           Intent intent = new Intent(getApplicationContext(),MainActivity.class);
           startActivity(intent);
       });
    }

    private String getImagePathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(column_index);
            }
        }
        return null; // Return null if the path cannot be determined.
    }

    private String getImagePathFromContentUri(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (columnIndex != -1) {
                return cursor.getString(columnIndex);
            }
        }

        return null;
    }

    private String getVideoPathFromUri(Uri uri) {
        // Check if the URI scheme is "content"
        if ("content".equals(uri.getScheme())) {
            // Create a new file in your app's private storage
            File privateDir = getFilesDir();
            File videoFile = new File(privateDir, "shared_video.mp4");

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    // Copy the shared video to the private file
                    OutputStream outputStream = new FileOutputStream(videoFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Now, videoFile contains the path to the copied video.
                    return videoFile.getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null; // Return null if the path cannot be determined.
    }

    private String getImagePathFromWhatsAppUri(Uri uri) {
        // Check if the URI scheme is "content"
        if ("content".equals(uri.getScheme())) {
            // Create a new file in your app's private storage
            File privateDir = getFilesDir();
            File imageFile = new File(privateDir, "shared_image.jpg");

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    // Copy the shared image to the private file
                    OutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Now, imageFile contains the path to the copied image.
                    return imageFile.getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null; // Return null if the path cannot be determined.
    }


    private String getMediaPathFromUri(Uri uri) {
        // Check if the URI scheme is "content"
        if ("content".equals(uri.getScheme())) {
            // Create a new file in your app's private storage
            File privateDir = getFilesDir();
            File mediaFile = new File(privateDir, "shared_media");

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    // Copy the shared media to the private file
                    OutputStream outputStream = new FileOutputStream(mediaFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Now, mediaFile contains the path to the copied media.
                    return mediaFile.getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null; // Return null if the path cannot be determined.
    }

    private String getVideoPathFromDocumentUri(Uri uri) {
        String documentId = DocumentsContract.getDocumentId(uri);
        String[] split = documentId.split(":");
        if (split.length > 1) {
            String fileId = split[1];
            String[] projection = {MediaStore.Video.Media.DATA};
            String selection = MediaStore.Video.Media._ID + "=?";
            String[] selectionArgs = {fileId};
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex);
                }
            }
        }
        return null; // Return null if the path cannot be determined.
    }

    private String getPdfPathFromUri(Uri uri) {
        // Check if the URI scheme is "content"
        if ("content".equals(uri.getScheme())) {
            // Create a new file in your app's private storage
            File privateDir = getFilesDir();
            File pdfFile = new File(privateDir, "shared_pdf.pdf");

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    // Copy the shared PDF to the private file
                    OutputStream outputStream = new FileOutputStream(pdfFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Now, pdfFile contains the path to the copied PDF.
                    return pdfFile.getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null; // Return null if the path cannot be determined.
    }
}