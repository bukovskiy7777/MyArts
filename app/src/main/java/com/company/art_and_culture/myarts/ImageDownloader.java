package com.company.art_and_culture.myarts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;

import com.company.art_and_culture.myarts.pojo.Art;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;

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

    public void downloadImage (final Art art, final int viewWidth, final int viewHeight, String folderName) {

        initTarget(getFile(art, folderName));
        int artWidth, artHeight;
        if (((float)viewWidth/(float)viewHeight) > 1) {
            artWidth = 1600; artHeight = (int) (artWidth/((float)viewWidth/(float)viewHeight));
        } else {
            artHeight = 1600; artWidth = (int) (artHeight/((float)viewHeight/(float)viewWidth));
        }
        Picasso.get().load(art.getArtImgUrl()).resize(artWidth,artHeight).onlyScaleDown().into(target);
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

    public void requestPermissions (ArrayList<String> arrPerm, MainActivity activity) {
        String[] permissions = new String[arrPerm.size()];
        permissions = arrPerm.toArray(permissions);
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
    }

}
