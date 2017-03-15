package com.applexis.aimos_android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.adapter.DialogsAdapter;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.security.Key;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogsFragment extends Fragment implements KeyExchange.KeyExchangeListener {

    private static MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
    private KeyExchange keyExchange;

    private LinearLayout dialogsLoading;
    private LinearLayout dialogsLoadError;
    private RelativeLayout dialogsLoadLayout;

    private Button updateDialogList;

    private ListView dialogList;

    private EditText dialogSearch;

    private DialogsAdapter dialogsAdapter;

    private boolean getDialogsWaitForKeyExchange = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialogs, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);
        dialogSearch = (EditText) view.findViewById(R.id.dialog_search);
        dialogList = (ListView) view.findViewById(R.id.dialogs_list);
        dialogsLoading = (LinearLayout) view.findViewById(R.id.dialogs_loading);
        dialogsLoadError = (LinearLayout) view.findViewById(R.id.dialogs_load_error_layout);
        dialogsLoadLayout = (RelativeLayout) view.findViewById(R.id.dialogs_load_layout);
        updateDialogList = (Button) view.findViewById(R.id.dialogs_update);
        updateDialogList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialogs();
            }
        });
        view.findViewById(R.id.dialog_update_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialogs();
            }
        });
        view.findViewById(R.id.dialog_delete_search_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSearch.setText("");
            }
        });
        loadDialogs();
    }

    private void loadDialogs() {
        String token = SharedPreferencesHelper.getToken();
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        Key desKey = DESCryptoHelper.getKey(desKeyString);
        String eToken = DESCryptoHelper.encrypt(desKey, token);
        final Call<DialogListResponse> request = messengerAPI.getDialogs(eToken, SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<DialogListResponse>() {
            @Override
            public void onResponse(Call<DialogListResponse> call, Response<DialogListResponse> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        dialogsAdapter = new DialogsAdapter(getContext(), response.body().getDialogs());
                        dialogList.setAdapter(dialogsAdapter);
                        dialogsLoadError.setVisibility(View.GONE);
                        dialogsLoading.setVisibility(View.GONE);
                        dialogList.setVisibility(View.VISIBLE);
                        dialogsLoadLayout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки списка контактов: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(DialogListResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            getDialogsWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                        dialogsLoadError.setVisibility(View.GONE);
                        dialogsLoading.setVisibility(View.VISIBLE);
                        dialogList.setVisibility(View.GONE);
                        dialogsLoadLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки списка контактов", Toast.LENGTH_SHORT).show();
                    dialogsLoadError.setVisibility(View.VISIBLE);
                    dialogsLoading.setVisibility(View.GONE);
                    dialogList.setVisibility(View.GONE);
                    dialogsLoadLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<DialogListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка загрузки списка контактов", Toast.LENGTH_SHORT).show();
                dialogsLoadError.setVisibility(View.VISIBLE);
                dialogsLoading.setVisibility(View.GONE);
                dialogList.setVisibility(View.GONE);
                dialogsLoadLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(getContext(), R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (getDialogsWaitForKeyExchange) {
            loadDialogs();
            getDialogsWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(getContext(), R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }
}
