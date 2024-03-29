package com.company.art_and_culture.myarts.arts_show_fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.ImageDownloader;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.bottom_menu.home.HomeFragment;
import com.company.art_and_culture.myarts.maker_fragment.MakerFragment;
import com.company.art_and_culture.myarts.pojo.Art;
import com.company.art_and_culture.myarts.pojo.Maker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.company.art_and_culture.myarts.CommonAnimations.downloadFadeIn;
import static com.company.art_and_culture.myarts.CommonAnimations.downloadFadeOut;
import static com.company.art_and_culture.myarts.CommonAnimations.downloadTranslation;
import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;

public class ArtShowFragment extends Fragment implements ImageDownloader.IDownLoadResult {

    public static final String ARTS = "arts";
    public static final String POSITION = "position";

    private ArtShowViewModel artShowViewModel;
    private RecyclerView artRecyclerView;
    private ArtShowAdapter artShowAdapter;
    private SharedPreferences preferences;
    private View download_view, done_view;
    private CircleImageView add_view;
    private ConstraintLayout download_linear;
    private ProgressBar download_progress;
    private MainActivity activity;
    private android.content.res.Resources res;
    private ImageDownloader imageDownloader;
    private Art downloadArt;
    private int downloadX, downloadY;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_art_show, container, false);

        artRecyclerView = root.findViewById(R.id.show_art_recycler_view);

        artShowViewModel =new ViewModelProvider(this).get(ArtShowViewModel.class);

        res = getResources();
        int displayWidth = res.getDisplayMetrics().widthPixels;
        int displayHeight = res.getDisplayMetrics().heightPixels;

        initRecyclerView(artShowViewModel, displayWidth, displayHeight);

        Collection<Art> listArts = (Collection<Art>) getArguments().getSerializable(ARTS);
        int artClickPosition = getArguments().getInt(POSITION);

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        if (listArts == null) {
            artShowAdapter.clearItems();
        } else {
            ArtShowDataInMemory.getInstance().addData((ArrayList<Art>) listArts);
            artShowAdapter.clearItems();
            artShowAdapter.setItems(listArts);
            artRecyclerView.scrollToPosition(artClickPosition);
        }

        artShowViewModel.setActivity(activity);

        initDownloadViews(root);

        //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        artShowViewModel.finish ();
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
            public void onArtDownloadClick(final Art art, final int x, final int y) {

                downloadArt = art; downloadX = x; downloadY = y;

                imageDownloader = ImageDownloader.getInstance(ArtShowFragment.this);
                ArrayList<String> arrPerm = imageDownloader.checkPermission(getContext());
                if(arrPerm.isEmpty()) { startDownloading(); }
                else { requestPermissions(arrPerm.toArray(new String[0]), PERMISSION_REQUEST_CODE); }
            }

            @Override
            public void onLogoClick(Art art) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
                builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse(art.getArtLink()));
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

                Bundle args = new Bundle();
                args.putSerializable(MakerFragment.MAKER, maker);
                NavHostFragment.findNavController(ArtShowFragment.this)
                        .navigate(R.id.action_artShowFragment_to_makerFragment, args);
            }

            @Override
            public void onSaveToFolderClick(Art art) {
                activity.showSaveToFolderView(art);
            }
        };

        artShowAdapter = new ArtShowAdapter(artShowViewModel,getContext(), onArtClickListener, displayWidth, displayHeight);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        artRecyclerView.setLayoutManager(linearLayoutManager);
        artRecyclerView.setAdapter(artShowAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(artRecyclerView);
    }

    private void startDownloading() {
        String folderName = res.getString(R.string.folder_my_arts_pictures);
        boolean isExists = imageDownloader.isFileExists(downloadArt, folderName);
        if (isExists)
            Toast.makeText(getContext(), R.string.file_already_downloaded, Toast.LENGTH_SHORT).show();
        else {
            AnimatorSet set = startDownloadAnimation(downloadX, downloadY);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    imageDownloader.downloadImage(downloadArt, folderName);
                }
            });
            set.start();
        }
    }

    @Override
    public void onDownloadSuccess(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        stopDownloadAnimation();
        runDownLoadSuccessAnimation ();
    }

    @Override
    public void onDownloadFailure() {
        Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
        stopDownloadAnimation();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // you now have permission
                startDownloading();
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                Toast.makeText(getContext(), R.string.application_does_not_have_permission_to_download_the_file, Toast.LENGTH_LONG).show();
            }
        }
    }

}
