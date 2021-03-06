package com.applexis.aimos_android.network;

import com.applexis.aimos_android.network.model.AddContactResponse;
import com.applexis.aimos_android.network.model.ContactResponse;
import com.applexis.aimos_android.network.model.DialogListResponse;
import com.applexis.aimos_android.network.model.DialogResponse;
import com.applexis.aimos_android.network.model.FileData;
import com.applexis.aimos_android.network.model.FileUploadResponse;
import com.applexis.aimos_android.network.model.FolderCreateResponse;
import com.applexis.aimos_android.network.model.GetFileKeyResponse;
import com.applexis.aimos_android.network.model.GetMessageResponse;
import com.applexis.aimos_android.network.model.LoginResponse;
import com.applexis.aimos_android.network.model.MessageSendResponse;
import com.applexis.aimos_android.network.model.SyncResponse;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * @author applexis
 */

public interface AimosAPI {


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

    @POST("/mobile-api/sync")
    Call<SyncResponse> storageSync(@Query("fileDataList") String fileDataList,
                                   @Query("eToken") String eToken,
                                   @Query("base64PublicKey") String base64PublicKey);

    @Multipart
    @POST("/mobile-api/syncUpload")
    Call<FileUploadResponse> syncUpload(@Part("file\"; filename=\"pp.rr\" ") RequestBody file,
                                        @Query("eFileData") FileData eFileData,
                                        @Query("eFilePath") String eFilePath,
                                        @Query("eKey") String eKey,
                                        @Query("eToken") String eToken,
                                        @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/syncDownload")
    Call<ResponseBody> syncDownload(@Query("eFilePath") String eFilePath,
                                    @Query("eToken") String eToken,
                                    @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/getFileKey")
    Call<GetFileKeyResponse> getFileKey(@Query("eFileData") FileData eFileData,
                                        @Query("eFilePath") String eFilePath,
                                        @Query("eToken") String eToken,
                                        @Query("base64PublicKey") String base64PublicKey);

    @POST("/mobile-api/createFolder")
    Call<FolderCreateResponse> createFolder(@Query("eFolderName") String eFolderName,
                                            @Query("eFolderPath") String eFolderPath,
                                            @Query("eHash") String eHash,
                                            @Query("eToken") String eToken,
                                            @Query("base64PublicKey") String base64PublicKey);

    @Multipart
    @POST("/mobile-api/singleUpload")
    Call<FileUploadResponse> singleUpload(@Part("file") RequestBody file,
                                          @Query("eName") String eName,
                                          @Query("eSize") String eSize,
                                          @Query("eHash") String eHash,
                                          @Query("eParentDir") String eParentDir,
                                          @Query("eKey") String eKey,
                                          @Query("eToken") String eToken,
                                          @Query("base64PublicKey") String base64PublicKey);

    @Multipart
    @POST("/mobile-api/fileTest")
    Call<String> fileTest(@Part("name") String name, @Part("file") RequestBody file);

}
