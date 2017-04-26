package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.AddContactResponse;
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

public class ContactsSearchedAdapter extends BaseAdapter implements KeyExchange.KeyExchangeListener {

    public List<UserMinimalInfo> contactList = new ArrayList<>();
    private Context ctx;
    private LayoutInflater lInflater;

    private static MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
    private KeyExchange keyExchange;

    private boolean createDialogWaitForKeyExchange = false;
    private boolean addContactWaitForKeyExchange = false;
    private Long tmpId;

    private ContactAddListener contactAddListener;

    public ContactsSearchedAdapter(Context context, List<UserMinimalInfo> contactList) {
        this.contactList = contactList;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyExchange = new KeyExchange();
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
            view = lInflater.inflate(R.layout.adapter_contact_searched, viewGroup, false);
        }

        UserMinimalInfo u = contactList.get(i);

        ((TextView) view.findViewById(R.id.contact_adapter_searched_name_surname)).setText(u.getName() + " " + u.getSurname());
        ((TextView) view.findViewById(R.id.contact_adapter_searched_login)).setText(String.format("@%s", u.getLogin()));
        ImageView addContactImage = ((ImageView) view.findViewById(R.id.contact_searched_adapter_add_contact));
        addContactImage.setTag(u.getId());
        addContactImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact((Long) view.getTag());
            }
        });
        ImageView sendMessageImage = ((ImageView) view.findViewById(R.id.contact_searched_adapter_send_message));
        sendMessageImage.setTag(u.getId());
        sendMessageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    private void addContact(Long id) {
        tmpId = id;
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        if (desKeyString != "") {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = DESCryptoHelper.encrypt(DESKey, token);
            String eIdUser = DESCryptoHelper.encrypt(DESKey, Long.toString(id));
            final Call<AddContactResponse> addContactRequest = messengerAPI.addContact(eIdUser, eToken, rsaPublic);
            sendAddContactRequest(addContactRequest);
        } else if (desKeyString == "") {
            addContactWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendAddContactRequest(Call<AddContactResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<AddContactResponse>() {
            @Override
            public void onResponse(Call<AddContactResponse> call, Response<AddContactResponse> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if(contactAddListener != null) {
                        contactAddListener.OnContactAddListener(response.body().getUserMinimalInfo());
                    }
                } else {
                    if (response.body() != null) {
                        Toast.makeText(ctx, "Add Contact Error: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(ctx, R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (addContactWaitForKeyExchange) {
            addContact(tmpId);
            addContactWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(ctx, R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }

    public void setOnContactAddListener(ContactAddListener contactAddListener) {
        this.contactAddListener = contactAddListener;
    }

    public interface ContactAddListener {
        void OnContactAddListener(UserMinimalInfo u);
    }
}
