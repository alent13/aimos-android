package com.applexis.aimos_android.network;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.applexis.aimos_android.network.model.FileData;
import com.applexis.aimos_android.network.model.FileUploadResponse;
import com.applexis.aimos_android.network.model.FolderCreateResponse;
import com.applexis.aimos_android.network.model.GetFileKeyResponse;
import com.applexis.aimos_android.network.model.SyncResponse;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.HashHelper;
import com.applexis.utils.StringUtils;
import com.applexis.utils.crypto.AESCrypto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StorageAPI implements KeyExchangeAPI.KeyExchangeListener {

    public static final String SALT = "AIMOS";
    public static final String TAG = "StorageAPI";

    private boolean syncWaitForKeyExchange = false;
    private boolean syncUploadWaitForKeyExchange = false;
    private boolean syncDownloadForKeyExchange = false;
    private boolean fileUploadWaitForKeyExchange = false;
    private boolean folderCreateWaitForKeyExchange = false;
    private Context context;
    private OnStorageAPIListener onStorageAPIListener;
    private KeyExchangeAPI keyExchange;
    private AimosAPI aimosAPI;
    private List<FileData> lastSyncFileData;
    private File lastCacheFile;
    private boolean otherSyncStart;
    private boolean syncLoadingActive;
    private String tmpFolderCreateName;
    private String tmpFolderCreatePath;

    public StorageAPI(Context context) {
        this.context = context;
        keyExchange = new KeyExchangeAPI();
        keyExchange.setKeyExchangeListener(this);
        aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
        lastSyncFileData = new ArrayList<>();
    }

    public void sync() {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        List<FileData> result = new ArrayList<>();
        result.add(new FileData(aes));
        getFolderHash(result, 0, getUserRootDirectory(), aes);

        ObjectMapper mapper = new ObjectMapper();
        String dataString = null;
        try {
            dataString = mapper.writeValueAsString(result).replace("{", "%7B").replace("}", "%7D");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Call<SyncResponse> request = aimosAPI.storageSync(dataString,
                aes.encrypt(SharedPreferencesHelper.getToken()), SharedPreferencesHelper.getGlobalPublicKey());
        otherSyncStart = true;
        request.enqueue(new Callback<SyncResponse>() {
            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                if (response.body() != null) {
                    if (response.body().check(aes) && response.body().getFileDataList() != null) {
                        lastSyncFileData = response.body().getFileDataList();
                        lastSyncFileData.forEach(fileData -> {
                            if (fileData.getIsFolder(aes)) {
                                fileData.setStatus(FileData.OK, aes);
                            }
                        });
                        if (onStorageAPIListener != null) {
                            onStorageAPIListener.onSyncComplete(lastSyncFileData);
                        }
                        List<FileData> syncedFolders = Stream.of(lastSyncFileData)
                                .filter(value -> value.getIsFolder(aes))
                                .collect(Collectors.toList());
                        createSyncedFolders(syncedFolders, 0L, getUserRootDirectory(), aes);
                        lastSyncFileData = Stream.of(lastSyncFileData)
                                .filter(value -> !value.getIsFolder(aes) &&
                                        (value.getStatus(aes).equals(FileData.UPLOAD) ||
                                                value.getStatus(aes).equals(FileData.DOWNLOAD)))
                                .collect(Collectors.toList());
                        otherSyncStart = false;
                        syncNext();
                    } else {
                        Log.d(TAG, "Sync Error: " + response.body().getErrorType(aes));
                        if (response.body().getErrorType().equals(SyncResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            syncWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                        otherSyncStart = false;
                    }
                } else {
                    if (onStorageAPIListener != null) {
                        onStorageAPIListener.onSyncFailure();
                    }
                    otherSyncStart = false;
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                t.printStackTrace();
                if (onStorageAPIListener != null) {
                    onStorageAPIListener.onSyncFailure();
                }
                otherSyncStart = false;
            }
        });
    }

    public void createSyncedFolders(List<FileData> fdList, Long level, String levelPath, AESCrypto aes) {
        List<FileData> cLevelFolderList = Stream.of(fdList)
                .filter(value -> value.getParentId(aes).equals(level))
                .collect(Collectors.toList());
        cLevelFolderList.forEach(fileData -> {
            String fdLevelPath = levelPath + fileData.getName(aes) + File.separator;
            new File(fdLevelPath).mkdirs();
            createSyncedFolders(fdList, fileData.getId(aes), fdLevelPath, aes);
        });
    }

    public void syncNext() {
        syncLoadingActive = true;
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        if (!otherSyncStart) {
            if (lastSyncFileData.size() > 0) {
                if (lastSyncFileData.get(0).getStatus(aes).equals(FileData.UPLOAD)) {
                    syncUpload();
                } else if (lastSyncFileData.get(0).getStatus(aes).equals(FileData.DOWNLOAD)) {
                    syncDownload();
                }
            } else {
                syncLoadingActive = false;
            }
        }
    }

    public void syncUpload() {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        if (lastSyncFileData.size() > 0) {
            AESCrypto aesFileCrypto = new AESCrypto();
            FileData fData = lastSyncFileData.get(0);
            String fullPath = getUserRootDirectory() + fData.getPath(aes) + File.separator + fData.getName(aes);
            File uploadFile = new File(fullPath);
            if (uploadFile != null) {
                tryEncrypt(aes, aesFileCrypto, uploadFile);
                Uri fileUri = Uri.fromFile(lastCacheFile);

                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), getFileString(lastCacheFile));

                /*RequestBody requestFile = RequestBody.create(
                        MediaType.parse(context.getContentResolver().getType(fileUri)), lastCacheFile);
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("encryptedFile", fData.getName(aes), requestFile);*/

                Call<FileUploadResponse> request = aimosAPI.syncUpload(requestFile, fData, fData.getPath(),
                        aes.encrypt(aesFileCrypto.getKeyString()), aes.encrypt(SharedPreferencesHelper.getToken()),
                        SharedPreferencesHelper.getGlobalPublicKey());
                if (onStorageAPIListener != null) {
                    onStorageAPIListener.onFileUploadStart(fData.getName(aes));
                }
                request.enqueue(new Callback<FileUploadResponse>() {
                    @Override
                    public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                        if (response.body() != null) {
                            if (response.body().check(aes)) {
                                if (onStorageAPIListener != null) {
                                    if (onStorageAPIListener != null) {
                                        onStorageAPIListener.onFileUploadEnd(lastSyncFileData.get(0).getName(aes), false);
                                    }
                                    lastSyncFileData.remove(0);
                                    syncNext();
                                }
                            } else {
                                Log.d(TAG, response.body().getErrorType(aes));
                                if (response.body().getErrorType().equals(SyncResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                                    syncDownloadForKeyExchange = true;
                                    keyExchange.updateKeys();
                                }
                            }
                        }
                        lastCacheFile.delete();
                    }

                    @Override
                    public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                        lastCacheFile.delete();
                        t.printStackTrace();
                        if (onStorageAPIListener != null) {
                            if (lastSyncFileData.size() > 0) {
                                onStorageAPIListener.onFileUploadEnd(lastSyncFileData.get(0).getName(aes), true);
                            }
                        }
                    }
                });
            } else {
                lastSyncFileData.remove(0);
            }
        }
    }

    private void tryEncrypt(AESCrypto aes, AESCrypto aesFileCrypto, File uploadFile) {
        try {
            lastCacheFile = File.createTempFile(uploadFile.getName(), null, context.getCacheDir());
            FileOutputStream fileOutputStream = new FileOutputStream(lastCacheFile);
            byte[] fileData = new byte[(int) uploadFile.length()];
            FileInputStream inputStream = new FileInputStream(uploadFile);
            inputStream.read(fileData);
            fileOutputStream.write(aesFileCrypto.encrypt(fileData));
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncDownload() {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        if (lastSyncFileData.size() > 0) {
            FileData fData = lastSyncFileData.get(0);
            Call<ResponseBody> request = aimosAPI.syncDownload(aes.encrypt(fData.getPath(aes) + File.separator + fData.getName(aes)),
                    aes.encrypt(SharedPreferencesHelper.getToken()),
                    SharedPreferencesHelper.getGlobalPublicKey());
            request.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.body() != null) {
                        writeToCache(fData.getName(aes), fData.getPath(aes), response.body());
                        if (onStorageAPIListener != null) {
                            if (lastSyncFileData.size() > 0) {
                                onStorageAPIListener.onFileDownloadEnd(lastSyncFileData.get(0).getName(aes), false);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    if (onStorageAPIListener != null) {
                        if (lastSyncFileData.size() > 0) {
                            onStorageAPIListener.onFileDownloadEnd(lastSyncFileData.get(0).getName(aes), true);
                        }
                    }
                }
            });
        }
    }

    private boolean writeToCache(String fileName, String filePath, ResponseBody body) {
        if (onStorageAPIListener != null) {
            onStorageAPIListener.onFileDownloadStart(fileName);
        }
        try {
            lastCacheFile = File.createTempFile(fileName, null, context.getCacheDir());
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(lastCacheFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                    if (onStorageAPIListener != null) {
                        onStorageAPIListener.onFileDownloadProgress(
                                fileName, (int) ((float) fileSizeDownloaded / fileSize * 100));
                    }
                }
                outputStream.flush();
                getFileDecryptKey();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public void decryptSyncFile(String key, String fileName, String filePath) {
        if (onStorageAPIListener != null) {
            onStorageAPIListener.onFileDownloadStartDecrypt(fileName);
        }
        FileOutputStream fileOutputStream = null;
        try {
            AESCrypto aesFileCrypto = new AESCrypto(key);
            File stFile = new File(getUserRootDirectory() + File.separator + filePath + File.separator + fileName);

            fileOutputStream = new FileOutputStream(stFile);
            byte[] fileData = new byte[(int) lastCacheFile.length()];
            FileInputStream inputStream = new FileInputStream(lastCacheFile);
            inputStream.read(fileData);
            fileOutputStream.write(aesFileCrypto.decrypt(fileData));
            fileOutputStream.flush();
            if (onStorageAPIListener != null) {
                onStorageAPIListener.onFileDownloadEndDecrypt(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getFileDecryptKey() {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        if (lastSyncFileData.size() > 0) {
            FileData fData = lastSyncFileData.get(0);
            Call<GetFileKeyResponse> request = aimosAPI.getFileKey(fData, fData.getPath(),
                    aes.encrypt(SharedPreferencesHelper.getToken()),
                    SharedPreferencesHelper.getGlobalPublicKey());
            request.enqueue(new Callback<GetFileKeyResponse>() {
                @Override
                public void onResponse(Call<GetFileKeyResponse> call, Response<GetFileKeyResponse> response) {
                    if (response.body() != null) {
                        if (response.body().check(aes)) {
                            decryptSyncFile(response.body().getKey(aes), fData.getName(aes), fData.getPath(aes));
                        } else {
                            Log.d(TAG, response.body().getErrorType(aes));
                            if (response.body().getErrorType().equals(SyncResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                                syncDownloadForKeyExchange = true;
                                keyExchange.updateKeys();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetFileKeyResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    public void createFolder(String folderName, String folderPath) {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        Call<FolderCreateResponse> request = aimosAPI.createFolder(aes.encrypt(folderName),
                aes.encrypt(folderPath), aes.encrypt(HashHelper.getMD5String(folderName, SALT)),
                aes.encrypt(SharedPreferencesHelper.getToken()), SharedPreferencesHelper.getGlobalPublicKey());
        request.enqueue(new Callback<FolderCreateResponse>() {
            @Override
            public void onResponse(Call<FolderCreateResponse> call, Response<FolderCreateResponse> response) {
                if (response.body() != null) {
                    if (response.body().check(aes)) {
                        FileData fd = response.body().getFileData();
                        File folder = new File(getUserRootDirectory() +
                                folderPath + File.separator +
                                fd.getName(aes));
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                    } else {
                        Log.d(TAG, response.body().getErrorType(aes));
                        if (response.body().getErrorType().equals(SyncResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                            fileUploadWaitForKeyExchange = true;
                            keyExchange.updateKeys();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<FolderCreateResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void singleUpload(Long rootDir, File uploadFile) {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        AESCrypto aesFileCrypto = new AESCrypto();
        if (uploadFile != null) {
            tryEncrypt(aes, aesFileCrypto, uploadFile);

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), getFileString(lastCacheFile));

            Call<FileUploadResponse> request = aimosAPI.singleUpload(requestFile, aes.encrypt(uploadFile.getName()),
                    aes.encrypt(String.valueOf(uploadFile.length())), aes.encrypt(HashHelper.getFileMD5Hash(uploadFile)),
                    aes.encrypt(String.valueOf(rootDir)), aes.encrypt(aesFileCrypto.getKeyString()),
                    aes.encrypt(SharedPreferencesHelper.getToken()),
                    SharedPreferencesHelper.getGlobalPublicKey());
            if (onStorageAPIListener != null) {
                onStorageAPIListener.onFileUploadStart(uploadFile.getName());
            }
            request.enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    if (response.body() != null) {
                        if (response.body().check(aes)) {
                            if (onStorageAPIListener != null) {
                                if (onStorageAPIListener != null) {
                                    onStorageAPIListener.onSingleFileUploadComplete(response.body().getFileData());
                                }
                            }
                        } else {
                            if (response.body().getErrorType(aes) != null) {
                                Log.d(TAG, response.body().getErrorType(aes));
                            }
                            if (response.body().getErrorType().equals(SyncResponse.ErrorType.BAD_PUBLIC_KEY.name())) {
                                syncDownloadForKeyExchange = true;
                                keyExchange.updateKeys();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    t.printStackTrace();
                    if (onStorageAPIListener != null) {
                        if (lastSyncFileData.size() > 0) {
                            onStorageAPIListener.onFileUploadEnd(lastSyncFileData.get(0).getName(aes), true);
                        }
                    }
                }
            });
        }
    }

    public void singleUploadTest(File uploadFile) {
        AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);

        String result = getFileString(uploadFile);

        RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), result);

        Call<String> request = aimosAPI.fileTest(uploadFile.getName(), requestFile);
        request.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.body() != null) {
                    Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @NonNull
    private String getFileString(File uploadFile) {
        StringBuffer sb = new StringBuffer();

        FileInputStream fileOutputStream;
        try {
            fileOutputStream = new FileInputStream(uploadFile);
            int res;
            byte[] arr = new byte[1024];
            while ((res = fileOutputStream.read(arr)) >= 0) {
                sb.append(StringUtils.bytesToHex(Arrays.copyOfRange(arr, 0, res)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getFolderHash(List<FileData> result, int treeParent, String currentRootPath, AESCrypto aes) {
        File rootFile = new File(currentRootPath);
        File[] childList = rootFile.listFiles();

        StringBuilder currentDirHashString = new StringBuilder(currentRootPath.substring(currentRootPath.lastIndexOf(File.separator)));

        if (childList != null) {
            for (File f : childList) {
                if (f.isDirectory()) {
                    int cRootPosition = result.size();
                    result.add(new FileData(f, treeParent, aes));
                    String dirHash = getFolderHash(result, cRootPosition, currentRootPath + File.separator + f.getName(), aes);
                    result.get(cRootPosition).setHash(dirHash, aes);
                    currentDirHashString.append(dirHash);
                } else {
                    String fileHash = HashHelper.getFileMD5Hash(f);
                    result.add(new FileData(f, treeParent, fileHash, aes));
                    currentDirHashString.append(fileHash);
                }
            }
        }

        return HashHelper.getMD5String(currentDirHashString.toString(), SALT);
    }

    public String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    public static String getUserRootDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                SharedPreferencesHelper.getLogin() + File.separator;
    }

    public static String getPath(List<FileData> fileList, int treeParent, AESCrypto aes) {
        String result = "";
        FileData tmp = fileList.get(treeParent);
        while (tmp.getTreeParent(aes) != 0) {
            result = tmp.getName(aes) + "/" + result;
            tmp = fileList.get(tmp.getTreeParent(aes));
        }
        return result;
    }

    public static FileData getByParent(List<FileData> fileList, Long parentId, AESCrypto aes) {
        if (fileList != null) {
            for (FileData fd : fileList) {
                if (fd.getId(aes).equals(parentId)) {
                    return fd;
                }
            }
        }
        return null;
    }

    public void setOnStorageAPIListener(OnStorageAPIListener onStorageAPIListener) {
        this.onStorageAPIListener = onStorageAPIListener;
    }

    @Override
    public void onKeyExchangeSuccess() {
        if (syncWaitForKeyExchange || syncDownloadForKeyExchange || syncUploadWaitForKeyExchange) {
            sync();
        } else if (fileUploadWaitForKeyExchange) {

        } else if (folderCreateWaitForKeyExchange) {

        }
    }

    @Override
    public void onKeyExchangeFailure() {
        Log.d(TAG, "KeyExchangeFailure");
    }

    public interface OnStorageAPIListener {
        void onSyncComplete(List<FileData> fileDatas);

        void onSyncFailure();

        void onFileUploadStart(String fileName);

        void onFileUploadEnd(String fileName, boolean fail);

        void onFileDownloadStart(String fileName);

        void onFileDownloadProgress(String fileName, int progress);

        void onFileDownloadStartDecrypt(String fileName);

        void onFileDownloadEndDecrypt(String fileName);

        void onFileDownloadEnd(String fileName, boolean fail);

        void onSingleFileUploadComplete(FileData fd);

        void onSingleFileUploadFailure();

        void onFolderCreateComplete();

        void onFolderCreateFailure();

        void onFileRenameComplete();

        void onFileRenameFailure();

        void onFolderRenameComplete();

        void onFolderRenameFailure();
    }

}