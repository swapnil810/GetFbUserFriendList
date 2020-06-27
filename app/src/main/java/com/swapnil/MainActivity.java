package com.swapnil;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*@NOTE
1) TO GET A USER_FRIEND_LIST FIRST OF ALL YOU HAVE TO CREATE FB DEVELOPER
   ACCOUNT AND CREATE A TEST USER IN "Roles"

2) AFTER ADD TEST USER "Testers" ALLOW PERMISSION FROM YOUR DEVICE*/


public class MainActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String fbId, fbName, fbEmail;
    private String userFriendFbName, userFriendFbNameFbId, url;
    private ImageView ivAddFriendImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        generatekeyhash();
        FacebookSdk.sdkInitialize(MainActivity.this);
        initView();
        fbView();
    }

    private void initView() {
        ivAddFriendImage = findViewById(R.id.ivAddFriendImage);
    }

    private void fbView() {
        loginButton = findViewById(R.id.login_button);

        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
        if (loggedOut) {
            LoginManager.getInstance().logOut();
        } else {
            loginButton.performClick();
        }

        loginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_friends"));
        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (AccessToken.getCurrentAccessToken() != null) {
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookException", "FacebookException" + error);
            }
        });
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        final GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        JSONObject json = response.getJSONObject();
                        try {
                            if (json != null) {
                                fbName = json.optString("first_name");
                                fbEmail = json.optString("email");
                                fbId = json.optString("id");

                                GraphRequest requesta = GraphRequest.newGraphPathRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        "/" + AccessToken.getCurrentAccessToken().getUserId() + "/friends",
                                        new GraphRequest.Callback() {
                                            @Override
                                            public void onCompleted(GraphResponse response) {
                                                // Insert your code here
                                                JSONObject friendlistObject = response.getJSONObject();
                                                JSONArray jsonArrayFb = friendlistObject.optJSONArray("data");
                                                for (int i = 0; i <= jsonArrayFb.length() - 1; i++) {
                                                    Log.e("dssdd", "onCompleted: " + jsonArrayFb.length());
                                                    JSONObject jsonObject1 = jsonArrayFb.optJSONObject(i);
                                                    userFriendFbNameFbId = jsonObject1.optString("id");
                                                    userFriendFbName = jsonObject1.optString("name");
                                                    url = "https://graph.facebook.com/" + userFriendFbNameFbId + "/picture?type=large";

                                                    try {
                                                        if (!"".equals("")) {
                                                            RequestOptions options = new RequestOptions()
                                                                    .centerCrop()
                                                                    .placeholder(R.mipmap.ic_launcher_round)
                                                                    .error(R.mipmap.ic_launcher_round);
                                                            String imgUrl = "";
                                                            Log.d("imgUrl", imgUrl);
                                                            Glide.with(MainActivity.this).load(imgUrl).apply(options).into(ivAddFriendImage);
                                                        } else {
                                                            Glide.with(MainActivity.this).load(R.mipmap.ic_launcher_round).into(ivAddFriendImage);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    /*after perfrom operation log out fb code*/
                                                    LoginManager.getInstance().logOut();
                                                }
                                            }
                                        });

                                requesta.executeAsync();

                            } else {
                                Toast.makeText(MainActivity.this, "fb object data not found", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("FB", "Fb error" + e);
                        }
                    }

                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * This method is used to get hash key
     */
    public void generatekeyhash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "NameNot" + e, Toast.LENGTH_SHORT).show();


        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(this, "oSuchAlgorithmE" + e, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * make live hash key for ubantu
     * keytool -exportcert -alias swapnil E -keystore path.jks | openssl sha1 -binary | openssl base64
     */

}
