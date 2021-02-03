package com.brightpattern.chatdemo.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttachmentUtils {

    private static Integer previewMaxSize = null;


    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public static boolean isImage(String filename) {
        File file = new File(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }

    public static void createPreview(Context applicationContext, String filePath, String fileId, String chatSessionId) {

        if (previewMaxSize == null) {
            WindowManager wm = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            previewMaxSize = Math.min(size.x, size.y)/4;
        }

        File file = new File(filePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        if (options.outWidth > 0 && options.outHeight > 0) {
            options.inSampleSize = calculateInSampleSize(options, previewMaxSize, previewMaxSize);

            options.inJustDecodeBounds = false;
            Bitmap image = BitmapFactory.decodeFile(file.getPath(), options);
            FileOutputStream fileStream = null;
            try {
                fileStream = applicationContext.openFileOutput(fileId, Context.MODE_PRIVATE);
                image.compress(Bitmap.CompressFormat.JPEG, 70, fileStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);//TODO fix
            } finally {
                if (fileStream != null) {
                    try {
                        fileStream.close();
                    } catch (IOException e) {}
                }
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            inSampleSize = Math.max(height/reqHeight, width/reqWidth);
        }
        return inSampleSize;
    }

    public static String getFileName(String imageFileId) {
        return imageFileId;
    }
}
