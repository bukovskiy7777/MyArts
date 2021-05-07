package com.company.art_and_culture.myarts.sign_in_fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.company.art_and_culture.myarts.Constants;
import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;
import com.company.art_and_culture.myarts.network.NetworkQuery;
import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;
import com.company.art_and_culture.myarts.pojo.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;

public class SignInFragment extends Fragment implements View.OnClickListener {

    private LottieAnimationView animationView;
    private ConstraintLayout btn_sign_in_google;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 11;
    private TextView privacy_policy_tv, terms_and_conditions_tv;
    private ProgressBar progress;
    private MainActivity activity;
    private SharedPreferences preferences;
    private android.content.res.Resources res;
    private String privacyPolicyUrl = "", termsAndConditionsUrl ="";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_in, container, false);

        btn_sign_in_google = root.findViewById(R.id.btn_sign_in_google);
        btn_sign_in_google.setOnClickListener(this);
        privacy_policy_tv = root.findViewById(R.id.privacy_policy_tv);
        privacy_policy_tv.setOnClickListener(this);
        terms_and_conditions_tv = root.findViewById(R.id.terms_and_conditions_tv);
        terms_and_conditions_tv.setOnClickListener(this);
        progress = root.findViewById(R.id.progress_bar);
        progress.setVisibility(View.INVISIBLE);

        res = getResources();

        activity = (MainActivity) getActivity();
        if (activity != null) preferences = activity.getSharedPreferences(Constants.TAG, 0);

        animationView = root.findViewById(R.id.animation_view);
        animationView.addLottieOnCompositionLoadedListener(composition -> {
            Timer timer = new Timer();
            final long DELAY = 500; // milliseconds
            final Handler handler = new Handler();
            timer.schedule(new TimerTask() {
                @Override
                public void run() { handler.post(() -> {
                    //animationView.playAnimation();
                });}
            }, DELAY);
        });

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        setOnBackPressedListener(root);

        getPolicyPages();

        return root;
    }

    private void setOnBackPressedListener(View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK ) {
                animationView.pauseAnimation();
                return false;
            }
            return false;
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btn_sign_in_google.getId()) {
            progress.setVisibility(View.VISIBLE);
            btn_sign_in_google.setEnabled(false);
            // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this); updateUI(account);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else if(v.getId() == privacy_policy_tv.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(privacyPolicyUrl));
        }  else if(v.getId() == terms_and_conditions_tv.getId()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setStartAnimations(getContext(), R.anim.enter_from_right, R.anim.exit_to_left);
            builder.setExitAnimations(getContext(), R.anim.enter_from_left, R.anim.exit_to_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(termsAndConditionsUrl));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
            if (acct != null) {
                String userDisplayName = acct.getDisplayName();
                String userGivenName = acct.getGivenName();
                String userFamilyName = acct.getFamilyName();
                String userEmail = acct.getEmail();
                String userProviderId = acct.getId();
                String userImageUrl = String.valueOf(acct.getPhotoUrl());
                String userAccountProvider = Constants.ACCOUNT_PROVIDER_GOOGLE;

                String userUniqueId = preferences.getString(Constants.USER_UNIQUE_ID,"");
                User user = new User(userDisplayName, userGivenName, userFamilyName,
                                                userEmail, userProviderId, userImageUrl, userAccountProvider, userUniqueId);
                signInProcess(user);
            } else {
                progress.setVisibility(View.INVISIBLE);
                btn_sign_in_google.setEnabled(true);
            }
        }
    }

    private void signInProcess(User user) {

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.SIGN_IN_OPERATION);
        request.setUser(user);

        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Toast.makeText(getContext(), resp.getMessage(), Toast.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.USER_DISPLAY_NAME, resp.getUser().getUserDisplayName());
                    editor.putString(Constants.USER_GIVEN_NAME, resp.getUser().getUserGivenName());
                    editor.putString(Constants.USER_FAMILY_NAME, resp.getUser().getUserFamilyName());
                    editor.putString(Constants.USER_EMAIL, resp.getUser().getUserEmail());
                    editor.putString(Constants.USER_PROVIDER_ID, resp.getUser().getUserProviderId());
                    editor.putString(Constants.USER_ACCOUNT_PROVIDER, resp.getUser().getUserAccountProvider());
                    editor.putString(Constants.USER_UNIQUE_ID, resp.getUser().getUserUniqueId());
                    editor.putString(Constants.USER_IMAGE_URL,resp.getUser().getUserImageUrl());
                    editor.apply();

                    animationView.pauseAnimation();
                    activity.getNavFragments().popBackStack();
                }
                progress.setVisibility(View.INVISIBLE);
                btn_sign_in_google.setEnabled(true);
                mGoogleSignInClient.signOut();
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), res.getString(R.string.network_is_unavailable), Toast.LENGTH_LONG).show();
                progress.setVisibility(View.INVISIBLE);
                btn_sign_in_google.setEnabled(true);
                mGoogleSignInClient.signOut();
            }
        });
    }

    private void getPolicyPages() {
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_POLICY_PAGES_OPERATION);
        Call<ServerResponse> response = NetworkQuery.getInstance().create(Constants.BASE_URL, request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if(resp.getResult().equals(Constants.SUCCESS)){
                    privacyPolicyUrl = resp.getPrivacyPolicyUrl();
                    termsAndConditionsUrl = resp.getTermsAndConditionsUrl();
                }
            }
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) { }
        });
    }

}
