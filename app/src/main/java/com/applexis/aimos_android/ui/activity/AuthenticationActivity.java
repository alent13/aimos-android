package com.applexis.aimos_android.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.AimosAPI;
import com.applexis.aimos_android.network.AimosAPIClient;
import com.applexis.aimos_android.network.KeyExchangeAPI;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationActivity extends AppCompatActivity implements KeyExchangeAPI.KeyExchangeListener {

    public static final int PERMISSION_REQUEST_CODE = 200;

    @BindView(R.id.auth_login)
    EditText loginText;
    @BindView(R.id.auth_password)
    TextInputEditText passwordText;
    @BindView(R.id.auth_login_load_indicator)
    View loadIndicator;
    @BindView(R.id.auth_login_btn)
    View loginButton;

    private AimosAPI aimosAPI;
    private KeyExchangeAPI keyExchange;

    private ProgressDialog progressDialog;

    private boolean loginWaitForKeys = false;
    private boolean checkTokenWaitForKeys = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        ButterKnife.bind(this);
        SharedPreferencesHelper.initialize(this);
        aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
        keyExchange = new KeyExchangeAPI();
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
        String aesKeyString = SharedPreferencesHelper.getGlobalAesKey();
        if (!aesKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            AESCrypto aes = new AESCrypto(aesKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = aes.encrypt(token);
            final Call<LoginResponse> ckeckTokenRequest = aimosAPI.checkToken(eToken, rsaPublic);
            sendCheckTokenRequest(ckeckTokenRequest);
        } else if (aesKeyString.equals("")) {
            checkTokenWaitForKeys = true;
            keyExchange.updateKeys();
        }
    }

    private void sendCheckTokenRequest(Call<LoginResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                if (response.body() != null && response.body().check(aes)) {
                    SharedPreferencesHelper.setId(response.body().getUserMinimalInfo().getId(aes));
                    SharedPreferencesHelper.setLogin(response.body().getUserMinimalInfo().getLogin(aes));
                    SharedPreferencesHelper.setName(response.body().getUserMinimalInfo().getName(aes));
                    SharedPreferencesHelper.setSurname(response.body().getUserMinimalInfo().getSurname(aes));
                    SharedPreferencesHelper.setToken(response.body().getToken(aes));
                    requestMultiplePermissions();
                    startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                    progressDialog.hide();
                    finish();
                } else {
                    if (response.body() != null) {
                        Toast.makeText(AuthenticationActivity.this, "Check Token Error: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            SharedPreferencesHelper.setGlobalAesKey("");
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
        String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
        if (!loginText.getText().toString().equals("") && !passwordText.getText().toString().equals("") && !desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            AESCrypto aes = new AESCrypto(desKeyString);
            String eLogin = aes.encrypt(loginText.getText().toString());
            String ePassword = aes.encrypt(passwordText.getText().toString());
            final Call<LoginResponse> loginRequest = aimosAPI.login(eLogin, ePassword, rsaPublic);
            loadIndicator.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            sendLoginRequest(loginRequest);
        } else if (desKeyString.equals("")) {
            loginWaitForKeys = true;
            keyExchange.updateKeys();
        }
    }

    private void sendLoginRequest(final Call<LoginResponse> loginRequest) {
        loginRequest.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                if (response.body() != null && response.body().check(aes)) {
                    if (response.body().getUserMinimalInfo() != null) {
                        SharedPreferencesHelper.setId(response.body().getUserMinimalInfo().getId(aes));
                        SharedPreferencesHelper.setLogin(response.body().getUserMinimalInfo().getLogin(aes));
                        SharedPreferencesHelper.setName(response.body().getUserMinimalInfo().getName(aes));
                        SharedPreferencesHelper.setSurname(response.body().getUserMinimalInfo().getSurname(aes));
                        SharedPreferencesHelper.setToken(response.body().getToken(aes));
                        requestMultiplePermissions();
                        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AuthenticationActivity.this, "Login Error: " + response.body().getErrorType(aes), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (response.body() != null) {
                        Toast.makeText(AuthenticationActivity.this, "Login Error: " + response.body().getErrorType(aes), Toast.LENGTH_LONG).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            loginWaitForKeys = true;
                            keyExchange.updateKeys();
                        }
                    } else {
                        Toast.makeText(AuthenticationActivity.this, "Login Error", Toast.LENGTH_LONG).show();
                    }
                }
                loadIndicator.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(AuthenticationActivity.this, "Login Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                loadIndicator.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            requestMultiplePermissions();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(this, R.string.keyUpdateSuccess, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, R.string.keyUpdateFailure, Toast.LENGTH_SHORT).show();
    }
}
