package com.company.art_and_culture.myarts.user_edit_fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.User;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

import static android.app.Activity.RESULT_OK;
import static com.company.art_and_culture.myarts.Constants.PERMISSION_REQUEST_CODE;

public class UserEditFragment extends Fragment implements View.OnClickListener {

    private EditText user_given_name, user_family_name;
    private ImageView user_image, account_back, ic_delete_photo, ic_change_photo;
    private AppCompatButton btn_save;
    private ProgressBar progress_bar;
    private MainActivity activity;
    private SharedPreferences preferences;
    private android.content.res.Resources res;

    private File temporary_holder;
    private Bitmap selectedImage;
    private Uri selectedImageUri;
    private final int PIC_IMAGE = 1;
    private final int PIC_CROP = 2;
    private int CHOSEN = 0; // 0 = no image, 1 = chosen image, 2 = previous image
    private String userImageBase64;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_edit, container, false);

        user_image = root.findViewById(R.id.user_image);
        user_given_name = root.findViewById(R.id.user_given_name);
        user_family_name = root.findViewById(R.id.user_family_name);

        progress_bar = root.findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.GONE);
        btn_save = root.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        account_back = root.findViewById(R.id.account_back);
        account_back.setOnClickListener(this);

        ic_delete_photo = root.findViewById(R.id.ic_delete_photo);
        ic_delete_photo.setOnClickListener(this);
        ic_change_photo = root.findViewById(R.id.ic_change_photo);
        ic_change_photo.setOnClickListener(this);

        res = getResources();

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        user_given_name.setText(preferences.getString(Constants.USER_GIVEN_NAME, ""));
        user_family_name.setText(preferences.getString(Constants.USER_FAMILY_NAME, ""));
        String imageUrl = preferences.getString(Constants.USER_IMAGE_URL,"");
        if(imageUrl.isEmpty())
            user_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_account_circle_24));
        else
            Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(user_image);
        if(imageUrl.isEmpty()) CHOSEN = 0; else CHOSEN = 2;

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == account_back.getId()) {
            NavHostFragment.findNavController(UserEditFragment.this).popBackStack();

        } else if(v.getId() == ic_change_photo.getId()) {
            checkPermission();

        }  else if(v.getId() == ic_delete_photo.getId()) {
            user_image.setImageResource(R.drawable.ic_outline_account_circle_24);
            CHOSEN = 0;

        } else if(v.getId() == btn_save.getId()) {
            String given_name = user_given_name.getText().toString();
            String family_name = user_family_name.getText().toString();

            if (!given_name.isEmpty()) {
                btn_save.setEnabled(false);
                progress_bar.setVisibility(View.VISIBLE);
                if (CHOSEN == 1) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] byteArray = stream.toByteArray();
                    userImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                } else if (CHOSEN == 2){
                    userImageBase64 = Constants.USER_IMAGE_PREVIOUS;
                } else {
                    userImageBase64 = null;
                }
                editAccountProcess(given_name, family_name);
            } else {
                Toast.makeText(getContext(), res.getString(R.string.specify_given_name_please), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void editAccountProcess(String given_name, String family_name) {

        String userUniqueId = preferences.getString(Constants.USER_UNIQUE_ID,"");
        String userEmail = preferences.getString(Constants.USER_EMAIL,"");
        User user = new User(userImageBase64, given_name, family_name, userUniqueId, userEmail);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.EDIT_USER_ACCOUNT_OPERATION);
        request.setUser(user);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Toast.makeText(getContext(), resp.getMessage(), Toast.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){
                    SharedPreferences.Editor editor = preferences.edit();
                    //editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    //editor.putString(Constants.USER_DISPLAY_NAME, resp.getUser().getUserDisplayName());
                    editor.putString(Constants.USER_GIVEN_NAME, resp.getUser().getUserGivenName());
                    editor.putString(Constants.USER_FAMILY_NAME, resp.getUser().getUserFamilyName());
                    //editor.putString(Constants.USER_EMAIL, resp.getUser().getUserEmail());
                    //editor.putString(Constants.USER_PROVIDER_ID, resp.getUser().getUserProviderId());
                    //editor.putString(Constants.USER_ACCOUNT_PROVIDER, resp.getUser().getUserAccountProvider());
                    //editor.putString(Constants.USER_UNIQUE_ID, resp.getUser().getUserUniqueId());
                    editor.putString(Constants.USER_IMAGE_URL,resp.getUser().getUserImageUrl());
                    editor.apply();

                    NavHostFragment.findNavController(UserEditFragment.this).popBackStack();
                }
                progress_bar.setVisibility(View.GONE);
                btn_save.setEnabled(true);
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), res.getString(R.string.network_is_unavailable), Toast.LENGTH_LONG).show();
                progress_bar.setVisibility(View.INVISIBLE);
                btn_save.setEnabled(true);
            }
        });
    }

    private void checkPermission() {
        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(arrPerm.isEmpty()) { addProfileImage(); }
        else { requestPermissions(arrPerm.toArray(new String[0]), PERMISSION_REQUEST_CODE); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // you now have permission
                addProfileImage();
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                Toast.makeText(getContext(), R.string.application_does_not_have_permission_to_choose_the_photo, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addProfileImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PIC_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if(requestCode==PIC_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = imageReturnedIntent.getData();
            cropImage();
        } else if (requestCode==PIC_CROP && resultCode == RESULT_OK){
            // Получим кадрированное изображение
            String filePath = temporary_holder.getPath();
            selectedImage = BitmapFactory.decodeFile(filePath);
            user_image.setImageBitmap(selectedImage);
            CHOSEN = 1;
        }
    }

    private void cropImage(){
        try {
            // Намерение для кадрирования. Не все устройства поддерживают его
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(selectedImageUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 100);
            cropIntent.putExtra("aspectY", 100);
            cropIntent.putExtra("outputX", 800);
            cropIntent.putExtra("outputY", 800);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            //cropIntent.putExtra("return-data", true);

            File pictureFolder = Environment.getExternalStorageDirectory();
            File mainFolder = new File(pictureFolder, res.getString(R.string.folder_my_arts_pictures));
            File tempFolder = new File(mainFolder, ".temp");
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }
            temporary_holder = new File(tempFolder, "temporary_holder.jpg");
            try { temporary_holder.createNewFile(); } catch (IOException ex) { }
            Uri uri = Uri.fromFile(temporary_holder);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException exception){
            String errorMessage = res.getString(R.string.photo_will_be_inserted_without_cropping);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }





}
