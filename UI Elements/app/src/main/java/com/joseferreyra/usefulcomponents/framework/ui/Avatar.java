package com.joseferreyra.usefulcomponents.framework.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.joseferreyra.usefulcomponents.R;
import com.joseferreyra.usefulcomponents.framework.data.ImageFileChache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This View draw a profile picture it also has a repository inside as part of an async task
 * Is in charge of validating  of
 */
public class Avatar extends android.support.v7.widget.AppCompatImageView {

    //Gradient colors for loading effect.
    private int startColorLoading, endColorLoading;
    private int placeholderId;
    private int loadingSize;
    private int borderSize;
    private int borderColor;

    //paint member for loading properties.
    private Paint loadingPaint;
    //paint member for cropping resources.
    private Paint cropPaint;
    //paint member for border image.
    private Paint borderPaint;


    private int loadingProgress = 0;

    public Avatar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
            R.styleable.Avatar, 0, 0);
        try {
            startColorLoading = a.getInteger(R.styleable.Avatar_startColorLoading, 0);
            endColorLoading = a.getInteger(R.styleable.Avatar_endColorLoading, 0);
            placeholderId = a.getResourceId(R.styleable.Avatar_placeHolderID, 0);
            loadingSize = a.getDimensionPixelSize(R.styleable.Avatar_loadingSize, 0);
            borderSize = a.getDimensionPixelSize(R.styleable.Avatar_borderSize, 0);
            borderColor = a.getInteger(R.styleable.Avatar_borderColor, 0);
        } finally {
            a.recycle();
        }

        initializePaints();
    }

    /**
     * Paints should never be declarated inside the onDraw, it's a common memory leak.
     * Becouse we are creating Objects on a repetetive piece of code.
     */
    private void initializePaints() {
        loadingPaint = new Paint();
        loadingPaint.setStyle(Paint.Style.STROKE);
        loadingPaint.setDither(true);
        loadingPaint.setAntiAlias(true);
        loadingPaint.setStrokeWidth(loadingSize);

        cropPaint = new Paint();
        cropPaint.setAntiAlias(true);
        cropPaint.setFilterBitmap(true);
        cropPaint.setDither(true);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setDither(true);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(borderSize);
        borderPaint.setColor(borderColor);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //This code should be here because, we need to scale the resource, but also we need
        // the view already measure in order to get the real dimension of the view


        if (this.getDrawable()==null) {
            Drawable myDrawable = getResources().getDrawable(placeholderId);
            Bitmap anImage = ((BitmapDrawable) myDrawable).getBitmap();
            this.setImageBitmap(getRoundedShape(anImage));
        }
    }



    public void setImageURL(String url) {
        loadingProgress = 0;
        new DownloadImageTask().execute(url);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBorder(canvas);

        drawLoading(canvas);
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawArc(new RectF(borderSize / 2, borderSize / 2, (this.getMeasuredWidth() - borderSize / 2), (this.getMeasuredHeight()) - borderSize / 2), 0, 360, false, borderPaint);
    }

    private void drawLoading(Canvas canvas) {

        int angleProgress = (loadingProgress) * 360 / 100;
        float viewWidthHalf = this.getMeasuredWidth() / 2;
        float viewHeightHalf = this.getMeasuredHeight() / 2;
        Matrix matrix = new Matrix();
        matrix.postRotate(-90, viewWidthHalf, viewHeightHalf);
        SweepGradient gradient = new SweepGradient(viewWidthHalf, viewHeightHalf, startColorLoading, endColorLoading);
        gradient.setLocalMatrix(matrix);
        loadingPaint.setShader(gradient);

        if (loadingProgress < 100) {
            canvas.drawArc(new RectF(loadingSize / 2, loadingSize / 2, (this.getMeasuredWidth() - loadingSize / 2), (this.getMeasuredHeight()) - loadingSize / 2), -90, angleProgress, false, loadingPaint);
        }
    }

    public Bitmap getRoundedShape(Bitmap originalBitmap) {
        if (originalBitmap == null) return null;
        int targetWidth = this.getMeasuredWidth();
        int targetHeight = this.getMeasuredHeight();
        Bitmap returnBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth) / 2, ((float) targetHeight) / 2,
            (Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = originalBitmap;

        Matrix matrix = new Matrix();
        int targetWidthResize, targetHeightResize = 0;

        // This conditions maintain the aspect ratio of the image.
        if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
            targetWidthResize = (int) (((float) sourceBitmap.getWidth() / sourceBitmap.getHeight()) * targetWidth);
            targetHeightResize = targetHeight;
        } else if (sourceBitmap.getWidth() < sourceBitmap.getHeight()) {
            targetWidthResize = targetWidth;
            targetHeightResize = (int) (((float) sourceBitmap.getHeight() / sourceBitmap.getWidth()) * targetHeight);
        } else {
            targetWidthResize = targetWidth;
            targetHeightResize = targetHeight;
        }

        //Scaling the image by applying transformation matrix.
        matrix.setRectToRect(new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new RectF(0, 0, targetWidthResize, targetHeightResize), Matrix.ScaleToFit.END);
        Bitmap scaledBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        canvas.drawBitmap(scaledBitmap, 0, 0, cropPaint);
        return returnBitmap;
    }


    /**
     * This is an asyncronous task that would be in charge of downloading the data.
     * <p>
     * 1 String the urls,
     * 2 Integer is the current progress of downloading 1 - 100
     * 3 Bitmap the bitmap downloaded that would be handle on the UI thread on onPostExecute.
     */
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        public DownloadImageTask() {
        }

        protected Bitmap doInBackground(String... urls) {

            Bitmap cacheBitmap = ImageFileChache.getInstance(getContext().getApplicationContext()).getCacheImage(urls[0]);
            if (cacheBitmap != null) {
                return cacheBitmap;
            } else {
                URL u = null;
                Bitmap avatarImage = null;
                InputStream in = null;
                try {
                    u = new URL(urls[0]);
                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) u.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    int lengthOfFile = urlConnection.getContentLength();

                    in = urlConnection.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    long total = 0;

                    while ((len1 = in.read(buffer)) > 0) {
                        total += len1;
                        publishProgress(+(int) ((total * 100) / lengthOfFile));
                        byteArrayOutputStream.write(buffer, 0, len1);
                    }
                    avatarImage = Avatar.this.getRoundedShape(BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(),
                        0, byteArrayOutputStream.size()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    in = null;
                }
                ImageFileChache.getInstance(getContext().getApplicationContext()).updateImage(urls[0], avatarImage);
                return avatarImage;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            loadingProgress = values[0];
            Avatar.this.invalidate();
        }

        protected void onPostExecute(Bitmap result) {
            Avatar.this.setImageBitmap(result);
            Avatar.this.invalidate();
        }
    }

}
