package com.applexis.aimos_android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.security.Key;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity implements KeyExchange.KeyExchangeListener {

    @BindView(R.id.registration_extra_layout)
    LinearLayout extraInfoLayout;
    @BindView(R.id.registration_expand_img)
    ImageView expandImage;
    @BindView(R.id.registration_login)
    EditText evLogin;
    @BindView(R.id.registration_name)
    EditText evName;
    @BindView(R.id.registration_surname)
    EditText evSurname;
    @BindView(R.id.registration_password)
    EditText evPassword;
    @BindView(R.id.registration_email)
    EditText evEmail;
    @BindView(R.id.registration_phone)
    EditText evPhone;
    @BindView(R.id.registration_about)
    EditText evAbout;

    private boolean isExtraShowed = false;
    private boolean registrationWaitForKeyExchange = false;

    private MessengerAPI messengerAPI;
    private KeyExchange keyExchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);
    }

    @OnClick(R.id.registration_extra_btn)
    public void extraInfoButton() {
        if (isExtraShowed) {
            extraInfoLayout.setVisibility(View.GONE);
            isExtraShowed = !isExtraShowed;
            expandImage.setRotation(0);
        } else {
            extraInfoLayout.setVisibility(View.VISIBLE);
            isExtraShowed = !isExtraShowed;
            expandImage.setRotation(180);
        }
    }

    @OnClick(R.id.registration_btn)
    public void registrationBtnClick() {
        String login = evLogin.getText().toString();
        String name = evName.getText().toString();
        String surname = evSurname.getText().toString();
        String password = evPassword.getText().toString();
        String email = evEmail.getText().toString();
        String phone = evPhone.getText().toString();
        String about = evAbout.getText().toString();

        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();

        if (login != "" && password != "" && name != "" && surname != "" && desKeyString != "") {
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String eLogin = DESCryptoHelper.encrypt(DESKey, login);
            String eName = DESCryptoHelper.encrypt(DESKey, name);
            String eSurname = DESCryptoHelper.encrypt(DESKey, surname);
            String ePassword = DESCryptoHelper.encrypt(DESKey, password);
            String eEmail = DESCryptoHelper.encrypt(DESKey, email);
            String ePhone = DESCryptoHelper.encrypt(DESKey, phone);
            String eAbout = DESCryptoHelper.encrypt(DESKey, about);

            Call<LoginResponse> registrateRequest = messengerAPI.registration(
                    eLogin, ePassword, eName,
                    eSurname, eEmail, ePhone,
                    eAbout, rsaPublic
            );
            registrateRequest.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.body() != null && response.body().isSuccess()) {
                        SharedPreferencesHelper.setLogin(response.body().getLogin());
                        SharedPreferencesHelper.setName(response.body().getName());
                        SharedPreferencesHelper.setSurname(response.body().getSurname());
                        SharedPreferencesHelper.setToken(response.body().getToken());
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        if (response.body() != null) {
                            if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                                registrationWaitForKeyExchange = true;
                                keyExchange.updateKeys();
                            }
                            Toast.makeText(RegistrationActivity.this, "Ошибка регистрации: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(RegistrationActivity.this, String.format("Ошибка регистрации (%s)", t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (desKeyString == "") {
            registrationWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(this, R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (registrationWaitForKeyExchange) {
            registrationBtnClick();
            registrationWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(this, R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }
}
