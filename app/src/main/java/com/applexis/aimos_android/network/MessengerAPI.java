package com.applexis.aimos_android.network;

import com.applexis.aimos_android.network.model.AddContactResponse;
import com.applexis.aimos_android.network.model.ContactResponse;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.network.model.DialogResponse;
import com.applexis.aimos_android.network.model.GetMessageResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.MessageSendResponse;
import com.applexis.aimos_android.network.model.UserMinimalInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author applexis
 */

public interface MessengerAPI {


    @POST("/mobile-api/keyExchange")
    Call<String> keyExchange(@Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/login")
    Call<LoginResponse> login(@Query("eLogin") String eLogin,
                              @Query("ePassword") String ePassword,
                              @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/checkToken")
    Call<LoginResponse> checkToken(@Query("eToken") String eToken,
                                   @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/registration")
    Call<LoginResponse> registration(@Query("eLogin") String eLogin,
                                     @Query("ePassword") String ePassword,
                                     @Query("eName") String eName,
                                     @Query("eSurname") String eSurname,
                                     @Query("eEmail") String eEmail,
                                     @Query("ePhone") String ePhone,
                                     @Query("eAbout") String eAbout,
                                     @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/findContact")
    Call<ContactResponse> findContact(@Query("eLoginPart") String eLoginPart,
                                      @Query("eToken") String eToken,
                                      @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/getContacts")
    Call<ContactResponse> getContacts(@Query("eToken") String eToken,
                                      @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/addContact")
    Call<AddContactResponse> addContact(@Query("eIdUser") String eIdUser,
                                        @Query("eToken") String eToken,
                                        @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/getDialogs")
    Call<DialogListResponse> getDialogs(@Query("eToken") String eToken,
                                        @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/createDialog")
    Call<DialogResponse> createDialog(@Query("eIdUser") String eIdUser,
                                      @Query("eToken") String eToken,
                                      @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/createGroup")
    Call<DialogResponse> createGroup(@Query("eDialogName") String eDialogName,
                                     @Query("eToken") String eToken,
                                     @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/getLastMessages")
    Call<GetMessageResponse> getLastMessages(@Query("eCount") String eCount,
                                             @Query("eOffset") String eOffset,
                                             @Query("eIdDialog") String eIdDialog,
                                             @Query("eToken") String eToken,
                                             @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/sendMessageEncrypted")
    Call<MessageSendResponse> sendMessageEncrypted(@Query("eMessage") String eMessage,
                                               @Query("eKey") String eKey,
                                               @Query("eEds") String eEds,
                                               @Query("eEdsPublicKey") String eEdsPublicKey,
                                               @Query("eIdDialog") String eIdDialog,
                                               @Query("eToken") String eToken,
                                               @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/sendMessage")
    Call<MessageSendResponse> sendMessage(@Query("eMessage") String eMessage,
                                          @Query("eEds") String eEds,
                                          @Query("eEdsPublicKey") String eEdsPublicKey,
                                          @Query("eIdDialog") String eIdDialog,
                                          @Query("eToken") String eToken,
                                          @Query("base64PublicKey") String base64PublicKey);

}
