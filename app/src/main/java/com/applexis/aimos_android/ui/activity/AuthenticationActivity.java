package com.applexis.aimos_android.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.utils.DESCryptoHelper;

import java.security.Key;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationActivity extends AppCompatActivity implements KeyExchange.KeyExchangeListener {

    @BindView(R.id.auth_login)
    EditText loginText;
    @BindView(R.id.auth_password)
    TextInputEditText passwordText;

    private MessengerAPI messengerAPI;
    private KeyExchange keyExchange;

    private ProgressDialog progressDialog;

    private boolean loginWaitForKeys = false;
    private boolean checkTokenWaitForKeys = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        ButterKnife.bind(this);
        SharedPreferencesHelper.initialize(this);
        messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);

        String token = SharedPreferencesHelper.getToken();
        if (!token.equals("")) {
            loginText.setText(SharedPreferencesHelper.getLogin());
            checkToken();
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Вход по сохраненным данным");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        passwordText.setTransformationMethod(new PasswordTransformationMethod());
    }

    @OnClick(R.id.auth_registration_btn)
    public void registrationClick() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.auth_login_btn)
    public void loginClick() {
        loginSend();
    }

    @OnClick(R.id.auth_key_update)
    public void keyUpdateTextClick() {
        keyExchange.updateKeys();
    }

    public void checkToken() {
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        if (!desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = DESCryptoHelper.encrypt(DESKey, token);
            final Call<LoginResponse> ckeckTokenRequest = messengerAPI.checkToken(eToken, rsaPublic);
            sendCheckTokenRequest(ckeckTokenRequest);
        } else if (desKeyString.equals("")) {
            checkTokenWaitForKeys = true;
            keyExchange.updateKeys();
        }
    }

    private void sendCheckTokenRequest(Call<LoginResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    SharedPreferencesHelper.setId(response.body().getId());
                    SharedPreferencesHelper.setLogin(response.body().getLogin());
                    SharedPreferencesHelper.setName(response.body().getName());
                    SharedPreferencesHelper.setSurname(response.body().getSurname());
                    SharedPreferencesHelper.setToken(response.body().getToken());
                    startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                    progressDialog.hide();
                    finish();
                } else {
                    if (response.body() != null) {
                        Toast.makeText(AuthenticationActivity.this, "Check Token Error: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            SharedPreferencesHelper.setGlobalDesKey("");
                            SharedPreferencesHelper.setGlobalPublicKey("");
                            SharedPreferencesHelper.setGlobalPrivateKey("");
                            checkTokenWaitForKeys = true;
                            keyExchange.updateKeys();
                        } else {
                            progressDialog.hide();
                        }
                    } else {
                        Toast.makeText(AuthenticationActivity.this, "Check Token Error", Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(AuthenticationActivity.this, "Check Token Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                progressDialog.hide();
            }
        });
    }

    private void loginSend() {
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        if (!loginText.getText().toString().equals("") && !passwordText.getText().toString().equals("") && !desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String eLogin = DESCryptoHelper.encrypt(DESKey, loginText.getText().toString());
            String ePassword = DESCryptoHelper.encrypt(DESKey, passwordText.getText().toString());
            final Call<LoginResponse> loginRequest = messengerAPI.login(eLogin, ePassword, rsaPublic);
            sendLoginRequest(loginRequest);
        } else if (desKeyString.equals("")) {
            loginWaitForKeys = true;
            keyExchange.updateKeys();
        }
    }

    private void sendLoginRequest(Call<LoginResponse> loginRequest) {
        loginRequest.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    SharedPreferencesHelper.setId(response.body().getId());
                    SharedPreferencesHelper.setLogin(response.body().getLogin());
                    SharedPreferencesHelper.setName(response.body().getName());
                    SharedPreferencesHelper.setSurname(response.body().getSurname());
                    SharedPreferencesHelper.setToken(response.body().getToken());
                    startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (response.body() != null) {
                        Toast.makeText(AuthenticationActivity.this, "Login Error: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            loginWaitForKeys = true;
                            keyExchange.updateKeys();
                        }
                    } else {
                        Toast.makeText(AuthenticationActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(AuthenticationActivity.this, "Login Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(this, R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (loginWaitForKeys) {
            loginSend();
            loginWaitForKeys = false;
        }
        if (checkTokenWaitForKeys) {
            checkToken();
            checkTokenWaitForKeys = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(this, R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }
}
