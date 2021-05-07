package com.company.art_and_culture.myarts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageDownloader {

    private Target target;
    private IDownLoadResult iDownLoadResult;
    private static ImageDownloader instance;

    private ImageDownloader(IDownLoadResult iDownLoadResult) {
        this.iDownLoadResult = iDownLoadResult;
    }

    public static ImageDownloader getInstance(IDownLoadResult iDownLoadResult){
        if(instance == null){
            instance = new ImageDownloader(iDownLoadResult);
        } else {
            instance.setIDownLoadResult(iDownLoadResult);
        }
        return instance;
    }

    private void setIDownLoadResult(IDownLoadResult iDownLoadResult) {
        this.iDownLoadResult = iDownLoadResult;
    }

    public interface IDownLoadResult {
        void onDownloadSuccess(File file);
        void onDownloadFailure();
    }

    public void downloadImage (final Art art, String folderName) {

        initTarget(getFile(art, folderName));

        int artWidth = art.getArtWidth(); int artHeight = art.getArtHeight();
        if (artWidth > 0 && artHeight > 0) {
            int width, height;
            if (((float)artWidth/(float)artHeight) > 1) {
                width = 1600; height = (int) (width/((float)artWidth/(float)artHeight));
            } else {
                height = 1600; width = (int) (height/((float)artHeight/(float)artWidth));
            }
            Picasso.get().load(art.getArtImgUrl()).resize(width,height).onlyScaleDown().into(target);
        } else {
            Picasso.get().load(art.getArtImgUrl()).into(target);
        }

    }

    public boolean isFileExists(Art art, String folderName) {
        final File file = getFile (art, folderName);
        return file.exists();
    }

    private File getFile(Art art, String folderName) {
        File pictureFolder = Environment.getExternalStorageDirectory();
        File mainFolder = new File(pictureFolder, folderName);
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }
        return new File(mainFolder, art.getArtMaker()+" - "+art.getArtTitle()+".jpg");
    }

    private void initTarget(final File file) {

        target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

                final Handler handler = new Handler();
                new Thread(() -> {
                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, ostream);
                        ostream.flush();
                        ostream.close();

                        handler.post(() -> iDownLoadResult.onDownloadSuccess(file));

                    } catch (IOException e) {
                        handler.post(() -> iDownLoadResult.onDownloadFailure());
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                iDownLoadResult.onDownloadFailure();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
    }

    public ArrayList<String> checkPermission(Context context) {

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return arrPerm;
    }

}
