package com.company.art_and_culture.myarts.web_view_fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.company.art_and_culture.myarts.MainActivity;
import com.company.art_and_culture.myarts.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WebViewFragment extends Fragment {

    private WebView webview;
    private ProgressBar progressBar;
    private TextView text_url;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_web_view, container, false);

        webview = root.findViewById(R.id.webview);
        progressBar = root.findViewById(R.id.progress);
        text_url = root.findViewById(R.id.text_url);

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress); //Make the bar disappear after URL is loaded
            }
        });
        webview.setWebViewClient(new HelloWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        MainActivity activity = (MainActivity) getActivity();
        String url = activity.getNavFragments().getUrlForWebFragment();
        webview.loadUrl(url);
        text_url.setText(url);

        setOnBackPressedListener(webview);

        return root;
    }

    private class HelloWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            text_url.setText(request.getUrl().toString());
            return true;
        }
        // For old devises
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            text_url.setText(url);
            return true;
        }
    }

    private void setOnBackPressedListener(final View root) {
        //You need to add the following line for this solution to work; thanks skayred
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                Log.i("backPressedEvent", "webViewFragment"+(keyCode==KeyEvent.KEYCODE_BACK) +" "+webview.canGoBack());
                if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
                    webview.goBack();
                    return true;
                }
                return false;
            }
        } );
    }

}
