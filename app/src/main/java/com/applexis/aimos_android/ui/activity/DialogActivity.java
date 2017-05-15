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
import com.applexis.aimos_android.network.KeyExchangeAPI;
import com.applexis.aimos_android.network.AimosAPI;
import com.applexis.aimos_android.network.AimosAPIClient;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.network.model.GetMessageResponse;
import com.applexis.aimos_android.network.model.MessageSendResponse;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;
import com.applexis.utils.crypto.DSASign;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogActivity extends AppCompatActivity implements KeyExchangeAPI.KeyExchangeListener {

    private DialogMinimal dialogInfo;

    @BindView(R.id.message_text)
    public EditText mMessageText;
    @BindView(R.id.message_list)
    public RecyclerView mMessageRecycleView;
    @BindView(R.id.message_dialog_name)
    public TextView mDialogName;

    private AimosAPI aimosAPI;
    private KeyExchangeAPI keyExchange;

    private MessageAdapter mAdapter;

    private boolean sendMessageWaitForKeys = false;
    private boolean getMessageWaitForKeys = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);

        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        dialogInfo = getIntent().getParcelableExtra(DialogsAdapter.DIALOG);

        aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
        keyExchange = new KeyExchangeAPI();
        keyExchange.setKeyExchangeListener(this);

        List<MessageMinimal> mMessageList = new ArrayList<>();
        mAdapter = new MessageAdapter(this, mMessageList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mMessageRecycleView.setLayoutManager(mLayoutManager);
        mMessageRecycleView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecycleView.setAdapter(mAdapter);

        mDialogName.setText(dialogInfo.getName(aes));
        getMessages();
    }

    @OnClick(R.id.message_back)
    public void backClick(View view) {
        finish();
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
        String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
        final AESCrypto aes = new AESCrypto(desKeyString);
        String eToken = aes.encrypt(token);
        String eCount = aes.encrypt(Long.toString(10));
        String eOffset = aes.encrypt(Long.toString(0));
        String eIdDialog = aes.encrypt(Long.toString(dialogInfo.getId(aes)));
        final Call<GetMessageResponse> request = aimosAPI.getLastMessages(eCount, eOffset, eIdDialog, eToken, SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<GetMessageResponse>() {
            @Override
            public void onResponse(Call<GetMessageResponse> call, Response<GetMessageResponse> response) {
                if (response.body() != null) {
                    if (response.body().check(aes)) {
                        if(response.body().getMessageMinimals() != null) {
                            mAdapter.updateMessageList(response.body().getMessageMinimals());
                            mMessageText.setText("");
                        }
                    } else {
                        Toast.makeText(DialogActivity.this, "Ошибка загрузки списка сообщений: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
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
        if(!message.equals("")) {
            String token = SharedPreferencesHelper.getToken();
            String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
            final AESCrypto aes = new AESCrypto(desKeyString);
            AESCrypto aesMessage = new AESCrypto();
            String eToken = aes.encrypt(token);
            String eIdDialog = aes.encrypt(Long.toString(dialogInfo.getId(aes)));
            KeyPair keyPair = DSASign.generateKeyPair();
            String eEdsPublicKey = aes.encrypt(DSASign.getPublicKeyString(keyPair.getPublic()));
            String eMessage = aesMessage.encrypt(message);
            String eKey = aes.encrypt(aesMessage.getKeyString());
            String eEds = Base64.encodeToString(DSASign.generateSignature(keyPair.getPrivate(), eMessage.getBytes()), Base64.DEFAULT);
            final Call<MessageSendResponse> request = aimosAPI.sendMessageEncrypted(eMessage, eKey, eEds, eEdsPublicKey, eIdDialog, eToken, SharedPreferencesHelper.getGlobalPublicKey());
            request.enqueue(new Callback<MessageSendResponse>() {
                @Override
                public void onResponse(Call<MessageSendResponse> call, Response<MessageSendResponse> response) {
                    if (response.body() != null) {
                        if (response.body().check(aes)) {
                            getMessages();
                            mMessageText.setText("");
                        } else {
                            Toast.makeText(DialogActivity.this, "Ошибка отправки сообщения: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, R.string.keyUpdateSuccess, Toast.LENGTH_SHORT).show();
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
