package com.example.josuegonzalez.loginp1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.example.josuegonzalez.loginp1.R;
import com.example.josuegonzalez.loginp1.util.IntentUtil;
import com.example.josuegonzalez.loginp1.util.PrefUtil;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private TextView info;
    private ImageView profileImgView;
    private LoginButton loginButton;

    private PrefUtil prefUtil;
    private IntentUtil intentUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        prefUtil = new PrefUtil(this);
        intentUtil = new IntentUtil(this);

        info = (TextView) findViewById(R.id.info);
        profileImgView = (ImageView) findViewById(R.id.profile_img);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
               Profile profile = Profile.getCurrentProfile();
               info.setText(message(profile));

               String userId = loginResult.getAccessToken().getUserId();
               String accessToken = loginResult.getAccessToken().getToken();

//                // save accessToken to SharedPreference
                prefUtil.saveAccessToken(accessToken);

               String profileImgUrl = "https://graph.facebook.com/" + userId + "/picture?type=large";


                Glide.with(MainActivity.this)
                        .load(profileImgUrl)
                        .into(profileImgView);
               // intentUtil.showAccessToken();

            }

            @Override
            public void onCancel() {
                info.setText("Intento de login cancelado");
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                info.setText("Fallo el intento de login");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_show_access_token:
                intentUtil.showAccessToken();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        deleteAccessToken();
        Profile profile = Profile.getCurrentProfile();
        info.setText(message(profile));

        if(Profile.getCurrentProfile()!=null){
        String profileImgUrl = "https://graph.facebook.com/" + profile.getId() + "/picture?type=large";
        Glide.with(MainActivity.this)
                .load(profileImgUrl)
                .into(profileImgView);}
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private String message(Profile profile) {
        StringBuilder stringBuffer = new StringBuilder();
        if (profile != null) {
            stringBuffer.append("Bienvenido ").append(profile.getFirstName());
        }
        return stringBuffer.toString();
    }

    private void deleteAccessToken() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    //User logged out
                    prefUtil.clearToken();
                    clearUserArea();
                }
            }
        };
    }

    private void clearUserArea() {
        info.setText("");
        profileImgView.setImageDrawable(null);
    }
}
