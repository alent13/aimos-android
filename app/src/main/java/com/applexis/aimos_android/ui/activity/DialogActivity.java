package com.applexis.aimos_android.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.MessageMinimal;
import com.applexis.aimos_android.ui.adapter.DialogsAdapter;
import com.applexis.aimos_android.ui.adapter.MessageAdapter;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.network.model.GetMessageResponse;
import com.applexis.aimos_android.network.model.MessageSendResponse;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.DSACryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogActivity extends AppCompatActivity implements KeyExchange.KeyExchangeListener {

    private DialogMinimal dialogInfo;

    @BindView(R.id.message_text)
    public EditText mMessageText;
    @BindView(R.id.message_list)
    public RecyclerView mMessageRecycleView;
    @BindView(R.id.message_dialog_name)
    public TextView mDialogName;

    private MessengerAPI messengerAPI;
    private KeyExchange keyExchange;

    private MessageAdapter mAdapter;

    private boolean sendMessageWaitForKeys = false;
    private boolean getMessageWaitForKeys = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);
        dialogInfo = getIntent().getParcelableExtra(DialogsAdapter.DIALOG);

        messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);

        List<MessageMinimal> mMessageList = new ArrayList<>();
        mAdapter = new MessageAdapter(this, mMessageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mMessageRecycleView.setLayoutManager(mLayoutManager);
        mMessageRecycleView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecycleView.setAdapter(mAdapter);

        mDialogName.setText(dialogInfo.getName());
        getMessages();
    }

    @OnClick(R.id.message_update_list)
    public void updateMessageListClick(View view) {
        getMessages();
    }

    @OnClick(R.id.message_send)
    public void sendMessageClick(View view) {
        sendMessage();
    }

    private void getMessages() {
        String token = SharedPreferencesHelper.getToken();
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        Key desKey = DESCryptoHelper.getKey(desKeyString);
        String eToken = DESCryptoHelper.encrypt(desKey, token);
        String eCount = DESCryptoHelper.encrypt(desKey, Long.toString(10));
        String eOffset = DESCryptoHelper.encrypt(desKey, Long.toString(0));
        String eIdDialog = DESCryptoHelper.encrypt(desKey, Long.toString(dialogInfo.getId()));
        final Call<GetMessageResponse> request = messengerAPI.getLastMessages(eCount, eOffset, eIdDialog, eToken, SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<GetMessageResponse>() {
            @Override
            public void onResponse(Call<GetMessageResponse> call, Response<GetMessageResponse> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        if(response.body().getMessageMinimals() != null) {
                            mAdapter.updateMessageList(response.body().getMessageMinimals());
                        }
                    } else {
                        Toast.makeText(DialogActivity.this, "Ошибка загрузки списка сообщений: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(DialogListResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            getMessageWaitForKeys = true;
                            keyExchange.updateKeys();
                        }
                    }
                } else {
                    Toast.makeText(DialogActivity.this, "Ошибка загрузки списка сообщений", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetMessageResponse> call, Throwable t) {
                Toast.makeText(DialogActivity.this, "Ошибка загрузки списка сообщений", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String message = mMessageText.getText().toString();
        if(message.equals("")) {
            String token = SharedPreferencesHelper.getToken();
            String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
            Key desKey = DESCryptoHelper.getKey(desKeyString);
            Key desMessageKey = DESCryptoHelper.generateKey();
            String eToken = DESCryptoHelper.encrypt(desKey, token);
            String eIdDialog = DESCryptoHelper.encrypt(desKey, Long.toString(dialogInfo.getId()));
            KeyPair keyPair = DSACryptoHelper.generateKeyPair();
            String eEdsPublicKey = DESCryptoHelper.encrypt(desKey, DSACryptoHelper.getPublicKeyString(keyPair.getPublic()));
            String eMessage = DESCryptoHelper.encrypt(desMessageKey, message);
            String eKey;
            eKey = DESCryptoHelper.encrypt(desKey, DESCryptoHelper.getKeyString(desMessageKey));
            String eEds = Base64.encodeToString(DSACryptoHelper.generateSignature(keyPair.getPrivate(), eMessage.getBytes()), Base64.DEFAULT);
            final Call<MessageSendResponse> request = messengerAPI.sendMessageEncrypted(eMessage, eKey, eEds, eEdsPublicKey, eIdDialog, eToken, SharedPreferencesHelper.getGlobalPublicKey());
            request.enqueue(new Callback<MessageSendResponse>() {
                @Override
                public void onResponse(Call<MessageSendResponse> call, Response<MessageSendResponse> response) {
                    if (response.body() != null) {
                        if (response.body().isSuccess()) {
                            getMessages();
                            mMessageText.setText("");
                        } else {
                            Toast.makeText(DialogActivity.this, "Ошибка отправки сообщения: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                            if (response.body().getErrorType().equals(DialogListResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                                getMessageWaitForKeys = true;
                                keyExchange.updateKeys();
                            }
                        }
                    } else {
                        Toast.makeText(DialogActivity.this, "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageSendResponse> call, Throwable t) {
                    Toast.makeText(DialogActivity.this, "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(this, R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (getMessageWaitForKeys) {
            getMessages();
            getMessageWaitForKeys = false;
        }
        if (sendMessageWaitForKeys) {
            sendMessage();
            sendMessageWaitForKeys = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {

    }
}