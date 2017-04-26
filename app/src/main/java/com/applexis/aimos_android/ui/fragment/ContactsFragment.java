package com.applexis.aimos_android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.ui.adapter.ContactsAdapter;
import com.applexis.aimos_android.ui.adapter.ContactsSearchedAdapter;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.model.ContactResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;

import java.security.Key;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsFragment extends Fragment implements KeyExchange.KeyExchangeListener, ContactsSearchedAdapter.ContactAddListener {

    private static MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
    private KeyExchange keyExchange;

    private LinearLayout contactsLoading;
    private LinearLayout contactsLoadError;
    private LinearLayout contactsListLayout;
    private RelativeLayout contactsLoadLayout;

    private ListView contactList;
    private ListView contactSearchedList;

    private Button updateContactList;

    private EditText searchEdit;

    private TextView searchText;

    private ContactsSearchedAdapter contactsSearchedAdapter;
    private ContactsAdapter contactsAdapter;

    private boolean getContactsWaitForKeyExchange = false;
    private boolean findContactsWaitForKeyExchange = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        keyExchange = new KeyExchange();
        keyExchange.setKeyExchangeListener(this);
        contactsLoading = (LinearLayout) view.findViewById(R.id.contacts_loading);
        contactsLoadError = (LinearLayout) view.findViewById(R.id.contacts_load_error_layout);
        contactsListLayout = (LinearLayout) view.findViewById(R.id.contacts_list_layout);
        contactsLoadLayout = (RelativeLayout) view.findViewById(R.id.contacts_load_layout);
        updateContactList = (Button) view.findViewById(R.id.contacts_update);
        contactList = (ListView) view.findViewById(R.id.contacts_list);
        contactSearchedList = (ListView) view.findViewById(R.id.contacts_list_searched);
        searchEdit = (EditText) view.findViewById(R.id.contact_search);
        searchText = (TextView) view.findViewById(R.id.contact_search_text);
        updateContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadContacts();
            }
        });
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 1) {
                    findContact(editable.toString());
                } else {
                    searchText.setVisibility(View.GONE);
                    contactSearchedList.setVisibility(View.GONE);
                }

            }
        });
        view.findViewById(R.id.contact_update_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadContacts();
            }
        });
        view.findViewById(R.id.contact_delete_search_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEdit.setText("");
            }
        });
        loadContacts();
    }

    private void findContact(String loginPart) {
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        if (!desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            Key DESKey = DESCryptoHelper.getKey(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = DESCryptoHelper.encrypt(DESKey, token);
            String eLoginPart = DESCryptoHelper.encrypt(DESKey, loginPart);
            final Call<ContactResponse> contactSearchedRequest = messengerAPI.findContact(eLoginPart, eToken, rsaPublic);
            sendFindContactsRequest(contactSearchedRequest);
        } else if (desKeyString == "") {
            findContactsWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendFindContactsRequest(Call<ContactResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getUserList().size() > 0) {
                        contactsSearchedAdapter = new ContactsSearchedAdapter(getContext(), response.body().getUserList());
                        contactsSearchedAdapter.setOnContactAddListener(ContactsFragment.this);
                        contactSearchedList.setAdapter(contactsSearchedAdapter);
                        searchText.setVisibility(View.VISIBLE);
                        contactSearchedList.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (response.body() != null) {
                        Toast.makeText(getContext(), "Find Contacts Error: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        if (response.body().getErrorType().equals(LoginResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            findContactsWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                    } else {
                        Toast.makeText(getContext(), "Find Contacts Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Check Token Request Error (" +
                        t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void loadContacts() {
        String token = SharedPreferencesHelper.getToken();
        String desKeyString = SharedPreferencesHelper.getGlobalDesKey();
        Key desKey = DESCryptoHelper.getKey(desKeyString);
        String eToken = DESCryptoHelper.encrypt(desKey, token);
        final Call<ContactResponse> request = messengerAPI.getContacts(eToken, SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        contactsAdapter = new ContactsAdapter(getContext(), response.body().getUserList());
                        contactList.setAdapter(contactsAdapter);
                        contactsLoadError.setVisibility(View.GONE);
                        contactsLoading.setVisibility(View.GONE);
                        contactsListLayout.setVisibility(View.VISIBLE);
                        contactsLoadLayout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки списка контактов: " + response.body().getErrorType(), Toast.LENGTH_SHORT).show();
                        contactsLoadError.setVisibility(View.VISIBLE);
                        contactsLoading.setVisibility(View.GONE);
                        contactsListLayout.setVisibility(View.GONE);
                        contactsLoadLayout.setVisibility(View.VISIBLE);
                        if (response.body().getErrorType().equals(ContactResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            getContactsWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки списка контактов", Toast.LENGTH_SHORT).show();
                    contactsLoadError.setVisibility(View.VISIBLE);
                    contactsLoading.setVisibility(View.GONE);
                    contactsListLayout.setVisibility(View.GONE);
                    contactsLoadLayout.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                Toast.makeText(ContactsFragment.this.getContext(), "Ошибка загрузки списка контактов", Toast.LENGTH_SHORT).show();
                contactsLoadError.setVisibility(View.VISIBLE);
                contactsLoading.setVisibility(View.GONE);
                contactsListLayout.setVisibility(View.GONE);
                contactsLoadLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onKeyExchangeSuccess() {
        Toast.makeText(getContext(), R.string.key_update_success, Toast.LENGTH_SHORT).show();
        if (getContactsWaitForKeyExchange) {
            loadContacts();
            getContactsWaitForKeyExchange = false;
        }
        if (findContactsWaitForKeyExchange) {
            findContact(searchEdit.getText().toString());
            findContactsWaitForKeyExchange = false;
        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Toast.makeText(getContext(), R.string.key_update_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnContactAddListener(UserMinimalInfo u) {
        if (contactsAdapter != null) {
            contactsAdapter.contactList.add(u);
            contactList.setAdapter(contactsAdapter);
            searchEdit.setText("");
        }
    }
}
