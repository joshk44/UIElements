package com.joseferreyra.usefulcomponents.framework.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class is responsible for Handling a simple image cache on the local
 * Storage.
 */
public class ImageFileChache {

    //Singleton instance
    private static ImageFileChache instance;

    private final String STORAGE_PATH;


    //Since the operations need a context the constructor must have as a parameter.
    //Private to ensure that Singleton instance is respected.
    private ImageFileChache(Context context) {
        STORAGE_PATH = context.getFilesDir().getPath();
    }

    public static ImageFileChache getInstance(Context context) {
        if (instance == null) {
            instance = new ImageFileChache(context);
        }
        return instance;
    }

    /**
     * This method updates or save if not exist a Bitmap encoded on png in order to respect
     * the rounded shape. Also is agnostic about the kind of bitmap so we are able to reuse
     * this for other pourposes.
     * @param url The image url.
     * @param bitmap downloaded bitmap
     */
    public void updateImage(String url, Bitmap bitmap) {
        File file = new File(getFileName(url));
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request the bitmap image from the cache
     * @param url
     * @return the bitmap of the stored image
     */
    public Bitmap getCacheImage(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        Bitmap bitmap = BitmapFactory.decodeFile(getFileName(url), options);
        return bitmap;
    }


    /**
     * This method concat the folder destination with a MD5 hash
     * The idea is to ensure the unique fileName for every image.
     * @param url
     * @return
     */
    public String getFileName(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        String fileName = "";
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            fileName = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return STORAGE_PATH + "/" + fileName + ".png";
    }
}