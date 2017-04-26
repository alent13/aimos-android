package com.applexis.aimos_android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.ui.activity.AuthenticationActivity;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

/**
 * @author applexis
 */

public class MyPageFragment extends Fragment implements KeyExchange.KeyExchangeListener {

    private KeyExchange keyExchange;

    private TextView nameSurnameText;
    private TextView loginText;
    private TextView tokenText;
    private TextView desKeyText;
    private TextView publicKeyText;
    private TextView privateKeyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);

        nameSurnameText = (TextView) view.findViewById(R.id.my_page_name_surname);
        loginText = (TextView) view.findViewById(R.id.my_page_login);
        tokenText = (TextView) view.findViewById(R.id.my_page_token);
        desKeyText = (TextView) view.findViewById(R.id.my_page_des_key);
        publicKeyText = (TextView) view.findViewById(R.id.my_page_public);
        privateKeyText = (TextView) view.findViewById(R.id.my_page_private);

        updateTextValues();

        view.findViewById(R.id.my_page_update_keys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyExchange.updateKeys();
            }
        });

        view.findViewById(R.id.my_page_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesHelper.setToken("");
                startActivity(new Intent(getContext(), AuthenticationActivity.class));
                getActivity().finish();
            }
        });
        return view;
    }

    public void updateTextValues() {
        if(nameSurnameText != null && loginText != null && tokenText != null && desKeyText != null && publicKeyText != null && privateKeyText != null) {
            nameSurnameText.setText(SharedPreferencesHelper.getName() + " " + SharedPreferencesHelper.getSurname());
            loginText.setText(SharedPreferencesHelper.getLogin());
            tokenText.setText(SharedPreferencesHelper.getToken());
            desKeyText.setText(SharedPreferencesHelper.getGlobalDesKey());
            publicKeyText.setText(SharedPreferencesHelper.getGlobalPublicKey());
            privateKeyText.setText(SharedPreferencesHelper.getGlobalPrivateKey());
        }
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(getContext(), R.string.key_update_success, Toast.LENGTH_SHORT).show();
        updateTextValues();
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(getContext(), R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }
}
