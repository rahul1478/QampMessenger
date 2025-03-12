package com.qamp.app.CustomClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;


public class PdfThumbnailGenerator {
    public static Bitmap generateThumbnail(Context context, String pdfFilePath) {
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(pdfFilePath), ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            // Choose which page to render (0-based index)
            PdfRenderer.Page page = renderer.openPage(0);

            int width = 200; // Set your desired thumbnail width
            int height = (int) (width * (float) page.getHeight() / page.getWidth());

            Bitmap thumbnail = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Render the PDF page into the Bitmap
            page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();
            renderer.close();
            fileDescriptor.close();

            return thumbnail;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

