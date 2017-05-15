package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.KeyExchangeAPI;
import com.applexis.aimos_android.network.AimosAPI;
import com.applexis.aimos_android.network.AimosAPIClient;
import com.applexis.aimos_android.network.model.DialogResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;
import com.applexis.aimos_android.ui.activity.DialogActivity;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author applexis
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements KeyExchangeAPI.KeyExchangeListener {

    public List<UserMinimalInfo> contactList = new ArrayList<>();
    private Context ctx;
    private LayoutInflater lInflater;

    private static AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
    private KeyExchangeAPI keyExchange;

    private boolean createDialogWaitForKeyExchange = false;
    Long tmpId;

    public ContactsAdapter(Context context, List<UserMinimalInfo> userMinimalInfo) {
        this.contactList = userMinimalInfo;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyExchange = new KeyExchangeAPI();
        keyExchange.setKeyExchangeListener(this);
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        UserMinimalInfo u = contactList.get(position);

        holder.nameSurnameText.setText(u.getName(aes) + " " + u.getSurname(aes));
        holder.loginText.setText(String.format("@%s", u.getLogin(aes)));
        holder.sendMessageButton.setTag(u.getId(aes));
        holder.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog((Long) view.getTag());
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private void createDialog(Long id) {
        tmpId = id;
        String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
        if (!desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            AESCrypto aes = new AESCrypto(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = aes.encrypt(token);
            String eIdUser = aes.encrypt(Long.toString(id));
            final Call<DialogResponse> createDialogRequest = aimosAPI.createDialog(eIdUser, eToken, rsaPublic);
            sendCreateDialogRequest(createDialogRequest);
        } else {
            createDialogWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendCreateDialogRequest(Call<DialogResponse> createDialogRequest) {
        createDialogRequest.enqueue(new Callback<DialogResponse>() {
            @Override
            public void onResponse(Call<DialogResponse> call, Response<DialogResponse> response) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                if (response.body() != null && response.body().check(aes)) {
                    Intent intent = new Intent(ctx, DialogActivity.class);
                    intent.putExtra(DialogsAdapter.DIALOG, response.body().getDialog());
                    ctx.startActivity(intent);
                } else {
                    if (response.body() != null) {
                        Toast.makeText(ctx, "Create Dialog Error: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(ctx, R.string.keyUpdateSuccess, Toast.LENGTH_SHORT).show();
        if (createDialogWaitForKeyExchange) {
            createDialog(tmpId);
            createDialogWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(ctx, R.string.keyUpdateFailure, Toast.LENGTH_SHORT).show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.contact_adapter_name_surname)
        public TextView nameSurnameText;
        @BindView(R.id.contact_adapter_login)
        public TextView loginText;
        @BindView(R.id.contact_adapter_send_message)
        public View sendMessageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
