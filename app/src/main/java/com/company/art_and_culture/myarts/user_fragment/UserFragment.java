package com.company.art_and_culture.myarts.user_fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.art_search_fragment.SearchFragment;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;

public class UserFragment extends Fragment implements View.OnClickListener {

    private TextView user_given_name, user_family_name, tv_user_id_number;
    private ImageView user_image, account_back;
    private TextView tv_write_to_us, tv_share, tv_rate, tv_log_out, tv_delete;
    private ImageView ic_write_to_us, ic_share, ic_rate, ic_log_out, ic_delete;
    private AppCompatButton edit_account;
    private ProgressBar progress_bar;
    private MainActivity activity;
    private SharedPreferences preferences;
    private android.content.res.Resources res;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);

        user_image = root.findViewById(R.id.user_image);
        user_given_name = root.findViewById(R.id.user_given_name);
        user_family_name = root.findViewById(R.id.user_family_name);
        tv_user_id_number = root.findViewById(R.id.tv_user_id_number);
        progress_bar = root.findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.GONE);
        edit_account = root.findViewById(R.id.edit_account);
        edit_account.setOnClickListener(this);
        account_back = root.findViewById(R.id.account_back);
        account_back.setOnClickListener(this);

        tv_write_to_us = root.findViewById(R.id.tv_write_to_us);
        tv_write_to_us.setOnClickListener(this);
        tv_share = root.findViewById(R.id.tv_share);
        tv_share.setOnClickListener(this);
        tv_rate = root.findViewById(R.id.tv_rate);
        tv_rate.setOnClickListener(this);
        tv_log_out = root.findViewById(R.id.tv_log_out);
        tv_log_out.setOnClickListener(this);
        tv_delete = root.findViewById(R.id.tv_delete);
        tv_delete.setOnClickListener(this);

        ic_write_to_us = root.findViewById(R.id.ic_write_to_us);
        ic_write_to_us.setOnClickListener(this);
        ic_share = root.findViewById(R.id.ic_share);
        ic_share.setOnClickListener(this);
        ic_rate = root.findViewById(R.id.ic_rate);
        ic_rate.setOnClickListener(this);
        ic_log_out = root.findViewById(R.id.ic_log_out);
        ic_log_out.setOnClickListener(this);
        ic_delete = root.findViewById(R.id.ic_delete);
        ic_delete.setOnClickListener(this);

        res = getResources();

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        user_given_name.setText(preferences.getString(Constants.USER_GIVEN_NAME, ""));
        user_family_name.setText(preferences.getString(Constants.USER_FAMILY_NAME, ""));
        tv_user_id_number.setText(preferences.getString(Constants.USER_UNIQUE_ID, ""));
        String imageUrl = preferences.getString(Constants.USER_IMAGE_URL,"");
        if(imageUrl.isEmpty())
            user_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_account_circle_24));
        else
            Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(user_image);

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == account_back.getId()) {
            NavHostFragment.findNavController(UserFragment.this).popBackStack();

        } else if(v.getId() == edit_account.getId()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_userFragment_to_userEditFragment);

        }  else if(v.getId() == tv_write_to_us.getId() || v.getId() == ic_write_to_us.getId()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.SEND_TO_EMAIL});
            intent.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.artworks_email_from_user));
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivity(intent);
            }

        } else if(v.getId() == tv_share.getId() || v.getId() == ic_share.getId()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, Constants.GOOGLE_PLAY_BASE_URL + activity.getPackageName());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

        } else if(v.getId() == tv_rate.getId() || v.getId() == ic_rate.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(Constants.GOOGLE_PLAY_BASE_URL + activity.getPackageName()));

        } else if(v.getId() == tv_log_out.getId() || v.getId() == ic_log_out.getId()) {
            showDialogLogOut();

        } else if(v.getId() == tv_delete.getId() || v.getId() == ic_delete.getId()) {
            showDialogDeleteAccount();
        }
    }

    private void showDialogDeleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_delete_account, null);
        builder.setView(view);
        builder.setTitle(res.getString(R.string.want_to_delete_account));
        builder.setPositiveButton(res.getString(R.string.yes), (dialogInterface, which) -> {
            progress_bar.setVisibility(View.VISIBLE);
            deleteAccountProcess();
        });
        builder.setNegativeButton(res.getString(R.string.no), (dialogInterface, which) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAccountProcess() {
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.DELETE_ACCOUNT_OPERATION);
        request.setUserUniqueId(preferences.getString(Constants.USER_UNIQUE_ID, ""));

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Toast.makeText(getContext(), resp.getMessage(), Toast.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, false);
                    editor.putString(Constants.USER_DISPLAY_NAME, "");
                    editor.putString(Constants.USER_GIVEN_NAME, "");
                    editor.putString(Constants.USER_FAMILY_NAME, "");
                    editor.putString(Constants.USER_EMAIL, "");
                    editor.putString(Constants.USER_PROVIDER_ID, "");
                    editor.putString(Constants.USER_ACCOUNT_PROVIDER, "");
                    //editor.putString(Constants.USER_UNIQUE_ID, "");
                    editor.putString(Constants.USER_IMAGE_URL, "");
                    editor.apply();

                    NavHostFragment.findNavController(UserFragment.this).popBackStack();
                }
                progress_bar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), res.getString(R.string.network_is_unavailable), Toast.LENGTH_LONG).show();
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void showDialogLogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(res.getString(R.string.are_you_sure_you_want_to_log_out));
        builder.setPositiveButton(res.getString(R.string.yes), (dialog, which) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.IS_LOGGED_IN, false);
            editor.putString(Constants.USER_DISPLAY_NAME, "");
            editor.putString(Constants.USER_GIVEN_NAME, "");
            editor.putString(Constants.USER_FAMILY_NAME, "");
            editor.putString(Constants.USER_EMAIL, "");
            editor.putString(Constants.USER_PROVIDER_ID, "");
            editor.putString(Constants.USER_ACCOUNT_PROVIDER, "");
            editor.putString(Constants.USER_UNIQUE_ID, "");
            editor.putString(Constants.USER_IMAGE_URL, "");
            editor.apply();

            NavHostFragment.findNavController(UserFragment.this).popBackStack();
        });
        builder.setNegativeButton(res.getString(R.string.no), (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
