package com.applexis.aimos_android.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.adapter.DialogsAdapter;
import com.applexis.aimos_android.adapter.MessageAdapter;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.network.model.GetMessageResponse;
import com.applexis.aimos_android.network.model.MessageSendResponse;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.DSACryptoHelper;
import com.applexis.aimos_android.utils.RSACryptoHelper;
import com.applexis.aimos_android.utils.SHA2Helper;
import com.applexis.aimos_android.utils.SHAHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.security.Key;
import java.security.KeyPair;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogActivity extends AppCompatActivity implements KeyExchange.KeyExchangeListener {

    @BindView(R.id.message_list)
    ListView extraInfoLayout;

    private DialogMinimal dialogInfo;

    private ImageView sendMessageBtn;
    private ImageView updateMessageList;
    private EditText messageText;
    private ListView messageList;
    private TextView dialogName;

    private MessengerAPI messengerAPI;
    private KeyExchange keyExchange;

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

        dialogName = (TextView) findViewById(R.id.message_dialog_name);
        dialogName.setText(dialogInfo.getName());
        messageList = (ListView) findViewById(R.id.message_list);
        messageText = (EditText) findViewById(R.id.message_text);
        updateMessageList = (ImageView) findViewById(R.id.message_update_list);
        updateMessageList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages();
            }
        });
        sendMessageBtn = (ImageView) findViewById(R.id.message_send);
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        getMessages();
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
                            MessageAdapter adapter = new MessageAdapter(DialogActivity.this, response.body().getMessageMinimals());
                            messageList.setAdapter(adapter);
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
        String message = messageText.getText().toString();
        if(message != "") {
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
                            messageText.setText("");
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
