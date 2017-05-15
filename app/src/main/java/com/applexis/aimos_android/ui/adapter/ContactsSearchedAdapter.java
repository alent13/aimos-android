package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
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
import com.applexis.aimos_android.network.model.AddContactResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;
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

public class ContactsSearchedAdapter extends RecyclerView.Adapter<ContactsSearchedAdapter.ViewHolder> implements KeyExchangeAPI.KeyExchangeListener {

    public List<UserMinimalInfo> contactList = new ArrayList<>();
    private Context ctx;
    private LayoutInflater lInflater;

    private static AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
    private KeyExchangeAPI keyExchange;

    private boolean createDialogWaitForKeyExchange = false;
    private boolean addContactWaitForKeyExchange = false;
    private Long tmpId;

    private ContactAddListener contactAddListener;

    public ContactsSearchedAdapter(Context context, List<UserMinimalInfo> contactList) {
        this.contactList = contactList;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyExchange = new KeyExchangeAPI();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact_searched, parent, false);
        return new ContactsSearchedAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        UserMinimalInfo u = contactList.get(position);

        holder.nameSurnameText.setText(u.getName(aes) + " " + u.getSurname(aes));
        holder.loginText.setText(String.format("@%s", u.getLogin(aes)));
        holder.addContactButton.setTag(u.getId(aes));
        holder.addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact((Long) view.getTag());
            }
        });
        holder.sendMessageButton.setTag(u.getId(aes));
        holder.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    private void addContact(Long id) {
        tmpId = id;
        String aesKeyString = SharedPreferencesHelper.getGlobalAesKey();
        if (!aesKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            AESCrypto aes = new AESCrypto(aesKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = aes.encrypt(token);
            String eIdUser = aes.encrypt(Long.toString(id));
            final Call<AddContactResponse> addContactRequest = aimosAPI.addContact(eIdUser, eToken, rsaPublic);
            sendAddContactRequest(addContactRequest);
        } else if (aesKeyString.equals("")) {
            addContactWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendAddContactRequest(Call<AddContactResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<AddContactResponse>() {
            @Override
            public void onResponse(Call<AddContactResponse> call, Response<AddContactResponse> response) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                if (response.body() != null && response.body().check(aes)) {
                    if(contactAddListener != null) {
                        contactAddListener.OnContactAddListener(response.body().getUserMinimalInfo());
                    }
                } else {
                    if (response.body() != null) {
                        Toast.makeText(ctx, "Add Contact Error: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            addContactWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                    } else {
                        Toast.makeText(ctx, "Add Contact Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AddContactResponse> call, Throwable t) {
                Toast.makeText(ctx, "Add Contact Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(ctx, R.string.keyUpdateSuccess, Toast.LENGTH_SHORT).show();
        if (addContactWaitForKeyExchange) {
            addContact(tmpId);
            addContactWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(ctx, R.string.keyUpdateFailure, Toast.LENGTH_SHORT).show();
    }

    public void setOnContactAddListener(ContactAddListener contactAddListener) {
        this.contactAddListener = contactAddListener;
    }

    public interface ContactAddListener {
        void OnContactAddListener(UserMinimalInfo u);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.contact_adapter_searched_name_surname)
        public TextView nameSurnameText;
        @BindView(R.id.contact_adapter_searched_login)
        public TextView loginText;
        @BindView(R.id.contact_searched_adapter_add_contact)
        public View addContactButton;
        @BindView(R.id.contact_searched_adapter_send_message)
        public View sendMessageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
