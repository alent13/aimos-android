package com.applexis.aimos_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.activity.DialogActivity;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.network.model.DialogResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author applexis
 */

public class ContactsAdapter extends BaseAdapter implements KeyExchange.KeyExchangeListener {

    public List<UserMinimalInfo> contactList = new ArrayList<>();
    private Context ctx;
    private LayoutInflater lInflater;

    private static MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
    private KeyExchange keyExchange;

    private boolean createDialogWaitForKeyExchange = false;
    Long tmpId;

    public ContactsAdapter(Context context, List<UserMinimalInfo> userMinimalInfo) {
        this.contactList = userMinimalInfo;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int i) {
        return contactList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.adapter_contact, viewGroup, false);
        }

        UserMinimalInfo u = contactList.get(i);

        ((TextView) view.findViewById(R.id.contact_adapter_name_surname)).setText(u.getName() + " " + u.getSurname());
        ((TextView) view.findViewById(R.id.contact_adapter_login)).setText(String.format("@%s", u.getLogin()));
        ImageView sendMessageImage = ((ImageView) view.findViewById(R.id.contact_adapter_send_message));
        sendMessageImage.setTag(u.getId());
        sendMessageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog((Long) view.getTag());
            }
        });

        return view;
    }

    private void createDialog(Long id) {
        tmpId = id;
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        if (desKeyString != "") {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = DESCryptoHelper.encrypt(DESKey, token);
            String eIdUser = DESCryptoHelper.encrypt(DESKey, Long.toString(id));
            final Call<DialogResponse> createDialogRequest = messengerAPI.createDialog(eIdUser, eToken, rsaPublic);
            sendCreateDialogRequest(createDialogRequest);
        } else if (desKeyString == "") {
            createDialogWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendCreateDialogRequest(Call<DialogResponse> createDialogRequest) {
        createDialogRequest.enqueue(new Callback<DialogResponse>() {
            @Override
            public void onResponse(Call<DialogResponse> call, Response<DialogResponse> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    Intent intent = new Intent(ctx, DialogActivity.class);
                    intent.putExtra(DialogsAdapter.DIALOG, response.body());
                    ctx.startActivity(intent);
                } else {
                    if (response.body() != null) {
                        Toast.makeText(ctx, "Create Dialog Error: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            createDialogWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                    } else {
                        Toast.makeText(ctx, "Create Dialog Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DialogResponse> call, Throwable t) {
                Toast.makeText(ctx, "Create Dialog Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(ctx, R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (createDialogWaitForKeyExchange) {
            createDialog(tmpId);
            createDialogWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(ctx, R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }
}
