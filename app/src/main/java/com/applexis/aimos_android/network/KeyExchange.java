package com.applexis.aimos_android.network;

import android.util.Base64;

import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;
import com.applexis.utils.crypto.RSACrypto;

import java.security.KeyPair;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author applexis
 */

public class KeyExchange {

    private KeyExchangeListener listener;

    public void setKeyExchangeListener(KeyExchangeListener listener) {
        this.listener = listener;
    }

    public void updateKeys() {
        MessengerAPI messengerAPI = MessengerAPIClient.getClient().create(MessengerAPI.class);
        final KeyPair RSAKey = RSACrypto.generateKeyPair();
        final String pKey = RSACrypto.getPublicKeyString(RSAKey.getPublic());
        final Call<String> request = messengerAPI.keyExchange(pKey);
        request.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body() != null) {
                    byte[] keyResponse = null;
                    keyResponse = RSACrypto.decrypt(RSAKey.getPrivate(), Base64.decode(response.body(), Base64.DEFAULT));

                    byte[] keyBytes = new byte[8];
                    System.arraycopy(keyResponse, keyResponse.length - 8, keyBytes, 0, 8);
                    String desKey = new AESCrypto(keyBytes).getKeyString();
                    SharedPreferencesHelper.setGlobalAesKey(desKey);
                    SharedPreferencesHelper.setGlobalPublicKey(RSACrypto.getPublicKeyString(RSAKey.getPublic()));
                    SharedPreferencesHelper.setGlobalPrivateKey(RSACrypto.getPrivateKeyString(RSAKey.getPrivate()));
                    if(listener != null) {
                        listener.onKeyExchangeSuccess();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //Toast.makeText(null, "Key Exchange Failed", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                if(listener != null) {
                    listener.onKeyExchangeFailure();
                }
            }
        });
    }

    public interface KeyExchangeListener {
        void onKeyExchangeSuccess();
        void onKeyExchangeFailure();
    }

}
