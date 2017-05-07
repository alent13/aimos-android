package com.applexis.aimos_android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.KeyExchangeAPI;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.ContactResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;
import com.applexis.aimos_android.ui.adapter.ContactsAdapter;
import com.applexis.aimos_android.ui.adapter.ContactsSearchedAdapter;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsFragment extends Fragment implements KeyExchangeAPI.KeyExchangeListener, ContactsSearchedAdapter.ContactAddListener {

    private static MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
    private KeyExchangeAPI keyExchange;

    @BindView(R.id.contacts_loading)
    public LinearLayout contactsLoading;
    @BindView(R.id.contacts_load_error_layout)
    public LinearLayout contactsLoadError;
    @BindView(R.id.contacts_list_layout)
    public LinearLayout contactsListLayout;
    @BindView(R.id.contacts_load_layout)
    public RelativeLayout contactsLoadLayout;

    @BindView(R.id.contacts_list)
    public RecyclerView contactList;
    @BindView(R.id.contacts_list_searched)
    public RecyclerView contactSearchedList;

    @BindView(R.id.contacts_update)
    public Button updateContactList;

    @BindView(R.id.contact_search)
    public EditText searchEdit;

    @BindView(R.id.contact_search_text)
    public TextView searchText;

    private ContactsSearchedAdapter contactsSearchedAdapter;
    private ContactsAdapter contactsAdapter;

    private boolean getContactsWaitForKeyExchange = false;
    private boolean findContactsWaitForKeyExchange = false;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        keyExchange = new KeyExchangeAPI();
        keyExchange.setKeyExchangeListener(this);
        loadContacts();
        unbinder = ButterKnife.bind(this, view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        contactsAdapter = new ContactsAdapter(getContext(), new ArrayList<UserMinimalInfo>());
        contactList.setLayoutManager(mLayoutManager);
        contactList.setItemAnimator(new DefaultItemAnimator());
        contactList.setAdapter(contactsAdapter);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        contactsSearchedAdapter = new ContactsSearchedAdapter(getContext(), new ArrayList<UserMinimalInfo>());
        contactSearchedList.setLayoutManager(mLayoutManager);
        contactSearchedList.setItemAnimator(new DefaultItemAnimator());
        contactSearchedList.setAdapter(contactsSearchedAdapter);
        contactsSearchedAdapter.setOnContactAddListener(ContactsFragment.this);

        return view;
    }

    @OnTextChanged(value = R.id.contact_search, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterSearchInput(Editable editable) {
        if (editable.toString().length() > 1) {
            findContact(editable.toString());
        } else {
            searchText.setVisibility(View.GONE);
            contactSearchedList.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.contacts_update)
    public void contactsUpdateClick(View view) {
        loadContacts();
    }

    @OnClick(R.id.contact_update_list)
    public void updateListClick(View view) {
        loadContacts();
    }

    @OnClick(R.id.contact_delete_search_text)
    public void deleteSearchTextClick(View view) {
        searchEdit.setText("");
    }

    private void findContact(String loginPart) {
        String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
        if (!desKeyString.equals("")) {
            String rsaPublic = SharedPreferencesHelper.getGlobalPublicKey();
            AESCrypto aes = new AESCrypto(desKeyString);
            String token = SharedPreferencesHelper.getToken();
            String eToken = aes.encrypt(token);
            String eLoginPart = aes.encrypt(loginPart);
            final Call<ContactResponse> contactSearchedRequest = messengerAPI.findContact(eLoginPart, eToken, rsaPublic);
            sendFindContactsRequest(contactSearchedRequest);
        } else if (desKeyString.equals("")) {
            findContactsWaitForKeyExchange = true;
            keyExchange.updateKeys();
        }
    }

    private void sendFindContactsRequest(Call<ContactResponse> checkTokenRequest) {
        checkTokenRequest.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                if (response.body() != null && response.body().check(aes)) {
                    if (response.body().getUserList().size() > 0) {
                        contactsSearchedAdapter.contactList.clear();
                        contactsSearchedAdapter.contactList.addAll(response.body().getUserList());
                        contactsSearchedAdapter.notifyDataSetChanged();
                        contactsSearchedAdapter.notifyItemRangeChanged(0, response.body().getUserList().size());
                        searchText.setVisibility(View.VISIBLE);
                        contactSearchedList.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (response.body() != null) {
                        Toast.makeText(getContext(), "Find Contacts Error: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
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
        String desKeyString = SharedPreferencesHelper.getGlobalAesKey();
        final AESCrypto aes = new AESCrypto(desKeyString);
        String eToken = aes.encrypt(token);
        final Call<ContactResponse> request = messengerAPI.getContacts(eToken, SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.body() != null) {
                    if (response.body().check(aes)) {
                        contactsLoadError.setVisibility(View.GONE);
                        contactsLoading.setVisibility(View.GONE);
                        contactsAdapter.contactList.clear();
                        contactsAdapter.contactList.addAll(response.body().getUserList());
                        contactsAdapter.notifyDataSetChanged();
                        contactsAdapter.notifyItemRangeChanged(0, response.body().getUserList().size());
                        contactsListLayout.setVisibility(View.VISIBLE);
                        contactsLoadLayout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки списка контактов: " + response.body().getErrorType(aes), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), R.string.keyUpdateSuccess, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), R.string.keyUpdateFailure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnContactAddListener(UserMinimalInfo u) {
        if (contactsAdapter != null) {
            contactsAdapter.contactList.add(u);
            contactsAdapter.notifyDataSetChanged();
            contactsAdapter.notifyItemInserted(contactsAdapter.contactList.size() - 1);
            searchEdit.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
