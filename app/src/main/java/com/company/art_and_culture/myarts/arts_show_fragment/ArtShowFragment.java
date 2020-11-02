package com.company.art_and_culture.myarts.arts_show_fragment;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.ui.home.HomeAnimations.downloadTranslation;

public class ArtShowFragment extends Fragment {

    private ArtShowViewModel artShowViewModel;
    private RecyclerView artRecyclerView;
    private ArtShowAdapter artShowAdapter;
    private SharedPreferences preferences;
    private Target target;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private ProgressBar download_progress;
    private MainActivity activity;
    private ArtShowEventListener artShowEventListener;
    private android.content.res.Resources res;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_show, container, false);

        artRecyclerView = root.findViewById(R.id.show_art_recycler_view);

        artShowViewModel =new ViewModelProvider(this).get(ArtShowViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(artShowViewModel, displayWidth, displayHeight);

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        if (activity != null) {
            Collection<Art> listArts = activity.getListArtsForArtShowFragment();
            int artClickPosition = activity.getClickPositionForArtShowFragment();

            if (listArts == null) {
                artShowAdapter.clearItems();
            } else {
                ArtShowDataInMemory.getInstance().addData((ArrayList<Art>) listArts);
                artShowAdapter.clearItems();
                artShowAdapter.setItems(listArts);
                artRecyclerView.scrollToPosition(artClickPosition);
            }
        }

        initDownloadViews(root);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void initDownloadViews(View root) {
        download_linear = root.findViewById(R.id.download_linear);
        add_view = root.findViewById(R.id.add_view);
        download_view = root.findViewById(R.id.download_view);
        done_view = root.findViewById(R.id.done_view);
        download_progress = root.findViewById(R.id.download_progress);

        download_linear.setVisibility(View.INVISIBLE);
        add_view.setVisibility(View.INVISIBLE);
        download_view.setVisibility(View.INVISIBLE);
        done_view.setVisibility(View.INVISIBLE);
        download_progress.setVisibility(View.INVISIBLE);

    }

    private void initRecyclerView(ArtShowViewModel artShowViewModel, int displayWidth, int displayHeight){

        ArtShowAdapter.OnArtClickListener onArtClickListener = new ArtShowAdapter.OnArtClickListener() {

            @Override
            public void onArtLikeClick(Art art, int position) {
                boolean networkState = ArtShowFragment.this.artShowViewModel.likeArt (art, position, preferences.getString(Constants.USER_UNIQUE_ID,""));
                if (!networkState) {
                    Toast.makeText(getContext(), R.string.network_is_unavailable, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onArtShareClick(Art art) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String text = art.getArtMaker()+" - "+art.getArtTitle()
                        + System.getProperty ("line.separator") + art.getArtImgUrl();
                //String text = art.getArtLink();
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }

            @Override
            public void onArtDownloadClick(final Art art, final int x, final int y, final int artWidth, final int artHeight) {

                ArrayList<String> arrPerm = checkPermission();
                if(arrPerm.isEmpty()) {

                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            final File file = getFile (art);
                            if (file.exists()) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), R.string.file_already_downloaded, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AnimatorSet set = startDownloadAnimation(x, y);
                                        set.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);

                                                Target target = getTarget(file);
                                                int downloadWidth, downloadHeight;
                                                if (((float)artWidth/(float)artHeight) > 1) {
                                                    downloadWidth = 1600; downloadHeight = (int) (downloadWidth/((float)artWidth/(float)artHeight));
                                                } else {
                                                    downloadHeight = 1600; downloadWidth = (int) (downloadHeight/((float)artHeight/(float)artWidth));
                                                }
                                                Picasso.get().load(art.getArtImgUrl()).resize(downloadWidth,downloadHeight).onlyScaleDown().into(target);
                                            }
                                        });
                                        set.start();
                                    }
                                });
                            }
                        }
                    }).start();

                } else {
                    requestPermissions (arrPerm);
                }
            }

            @Override
            public void onLogoClick(Art art) {

            }

            @Override
            public void onMakerClick(Art art, View view) {
                String artImgUrl;
                if (!art.getArtImgUrlSmall().equals(" ") && art.getArtImgUrlSmall().startsWith(getResources().getString(R.string.http))) {
                    artImgUrl = art.getArtImgUrlSmall();
                } else {
                    artImgUrl= art.getArtImgUrl();
                }
                Maker maker = new Maker(art.getArtMaker(), art.getArtistBio(), artImgUrl, art.getArtWidth(), art.getArtHeight(), art.getArtId(), art.getArtProviderId());
                //artShowEventListener.makerClickEvent(maker);
                showPopupMenu(art, view);
            }
        };

        artShowAdapter = new ArtShowAdapter(artShowViewModel,getContext(), onArtClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        artRecyclerView.setLayoutManager(linearLayoutManager);
        artRecyclerView.setAdapter(artShowAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(artRecyclerView);
    }

    private void showPopupMenu(final Art art, View view) {

        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.copy_text_pop_up_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.copy_text:
                        ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text_to_be_copied", art.getArtMaker());
                        clipboard.setPrimaryClip(clip);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private File getFile(Art art) {
        File pictureFolder = Environment.getExternalStorageDirectory();
        File mainFolder = new File(pictureFolder, res.getString(R.string.folder_my_arts_pictures));
        if (!mainFolder.exists()) {
            mainFolder.mkdirs();
        }
        return new File(mainFolder, art.getArtMaker()+" - "+art.getArtTitle()+".jpg");
    }

    private Target getTarget(final File file) {

        target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, ostream);
                            ostream.flush();
                            ostream.close();

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                            getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    stopDownloadAnimation();
                                    runDownLoadSuccessAnimation ();
                                }
                            });

                        } catch (IOException e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                                    stopDownloadAnimation();
                                }
                            });
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                stopDownloadAnimation();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        return target;
    }

    private void runDownLoadSuccessAnimation () {

        download_linear.setVisibility(View.VISIBLE);
        done_view.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeOut(download_linear, done_view, 0, 0)
        );
        set.start();
    }

    private void stopDownloadAnimation() {
        download_linear.setVisibility(View.INVISIBLE);
        add_view.setVisibility(View.INVISIBLE);
        download_view.setVisibility(View.INVISIBLE);
        done_view.setVisibility(View.INVISIBLE);
        download_progress.setVisibility(View.INVISIBLE);
    }

    private AnimatorSet startDownloadAnimation(int x, int y) {

        int actionBarHeight = 0;
        //if (activity != null) actionBarHeight = activity.getToolbarHeight();
        add_view.setX(x);
        add_view.setY(y - actionBarHeight);

        int[] location = new int[2];
        download_view.getLocationOnScreen(location);
        int x1 = location[0];
        int y1 = location[1];
        int targetX = x1 + download_view.getWidth()/2;
        int targetY = y1 + download_view.getHeight()/2;

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                downloadFadeIn(download_linear, add_view, download_view),
                downloadTranslation(add_view, targetX, targetY, download_progress)
        );

        return set;
    }

    private ArrayList<String> checkPermission() {

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return arrPerm;
    }

    private void requestPermissions (ArrayList<String> arrPerm) {
        String[] permissions = new String[arrPerm.size()];
        permissions = arrPerm.toArray(permissions);
        ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                Toast.makeText(getContext(), R.string.thanks_for_the_permissions_download_file_please, Toast.LENGTH_LONG).show();
                            }
                        }
                        if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                Toast.makeText(getContext(), R.string.thanks_for_the_permissions_download_file_please, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), R.string.application_does_not_have_permission_to_download_the_file, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }

    public ArtShowFragment finish() {
        artShowViewModel.finish ();
        return null;
    }

    public interface ArtShowEventListener {
        void makerClickEvent(Maker maker);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            artShowEventListener = (ArtShowEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }


}
