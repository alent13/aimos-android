package com.applexis.aimos_android.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.StorageAPI;
import com.applexis.aimos_android.network.model.FileData;
import com.applexis.aimos_android.ui.adapter.StorageAdapter;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class StorageFragment extends Fragment implements StorageAPI.OnStorageAPIListener, StorageAdapter.OnItemClick {

    public static int FILE_CHOOSE = 110;
    public static final String FOLDER_FIRST = "FOLDER_FIRST";

    @BindView(R.id.fragment_storage_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fragment_storage_sync_layout)
    View mSyncLayout;
    @BindView(R.id.fragment_storage_fab)
    FabSpeedDial mFabSpeedDial;
    @BindView(R.id.fragment_storage_sync_status_text)
    TextView mSyncStatusText;
    @BindView(R.id.fragment_storage_recycler)
    RecyclerView mStorageRecycler;
    ActionBar mActionBar;

    private StorageAPI mStorageAPI;
    private List<FileData> syncData;
    private Long currentDirectoryId;
    private StorageAdapter mStorageAdapter;
    private Unbinder unbinder;
    private boolean folderFirst;

    public StorageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        unbinder = ButterKnife.bind(this, view);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onNavigationBackPressed());
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        setHasOptionsMenu(true);
        createUserFolder();
        mStorageAPI = new StorageAPI(getContext());
        mStorageAPI.setOnStorageAPIListener(this);
        mFabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
                switch (menuItem.getItemId()) {
                    case R.id.menu_storage_add_file_action:
                        showFileSelectActivity();
                        break;
                    case R.id.menu_storage_add_folder_action:
                        showFolderNameDialog(aes);
                        break;
                }
                return true;
            }
        });
        List<FileData> mFileList = new ArrayList<>();
        SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        folderFirst = sharedPreferences.getBoolean(FOLDER_FIRST, false);
        mStorageAdapter = new StorageAdapter(getContext(), mFileList);
        mStorageAdapter.setFolderFirst(folderFirst);
        mStorageAdapter.setOnItemClick(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mStorageRecycler.setLayoutManager(mLayoutManager);
        mStorageRecycler.setItemAnimator(new DefaultItemAnimator());
        mStorageRecycler.setAdapter(mStorageAdapter);
        return view;
    }

    private void showFileSelectActivity() {
        new MaterialFilePicker()
                .withSupportFragment(this)
                .withPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .withRootPath("/")
                .withRequestCode(FILE_CHOOSE)
                .withHiddenFiles(true)
                .start();
    }

    private void onNavigationBackPressed() {
        mStorageAdapter.pageUp();
    }

    private void showFolderNameDialog(AESCrypto aes) {
        final EditText edittext = new EditText(getActivity());
        edittext.setTextColor(getResources().getColor(R.color.light_gray));

        new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom)
                .setMessage("Введите название папки")
                .setTitle("Создать папку")
                .setView(edittext).setPositiveButton(android.R.string.ok, (dialog, whichButton) -> createFolder(aes, edittext))
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {})
                .show();
    }

    private void createFolder(AESCrypto aes, EditText edittext) {
        String path = "";
        FileData fileByParent = StorageAPI.getByParent(syncData, currentDirectoryId, aes);
        if (fileByParent != null) {
            FileData fileData = syncData.get(fileByParent.getTreeParent(aes));
            path = StorageAPI.getPath(syncData, fileData.getTreeParent(aes), aes);
        }
        mStorageAPI.createFolder(edittext.getText().toString(), path);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mStorageAdapter.setFolderFirst(folderFirst);
        sync();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_storage_actions, menu);

        SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        folderFirst = sharedPreferences.getBoolean(FOLDER_FIRST, false);
        menu.findItem(R.id.menu_storage_folder_first_action).setChecked(folderFirst);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_storage_sync_action:
                sync();
                return true;
            case R.id.menu_storage_sort_action:

                return true;
            case R.id.menu_storage_folder_first_action:
                folderFirst = item.isChecked();
                item.setChecked(!folderFirst);
                SharedPreferences sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(FOLDER_FIRST, !folderFirst).commit();
                mStorageAdapter.setFolderFirst(!folderFirst);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        if (requestCode == FILE_CHOOSE && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            File uploadFile = new File(filePath);
            if (!mStorageAdapter.containsFile(uploadFile.getName(), aes)) {
                mStorageAPI.singleUpload(mStorageAdapter.getCurrentParentId(), uploadFile);
            } else {
                Toast.makeText(getActivity(), "Файл с таким именем уже существует", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sync() {
        mStorageAPI.sync();
        mFabSpeedDial.setVisibility(View.GONE);
        mSyncLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void createUserFolder() {
        File file = new File(StorageAPI.getUserRootDirectory() + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void onSyncComplete(List<FileData> fileDatas) {
        mSyncLayout.setVisibility(View.GONE);
        mFabSpeedDial.setVisibility(View.VISIBLE);
        mStorageAdapter.updateData(fileDatas);
    }

    @Override
    public void onSyncFailure() {
        Toast.makeText(getActivity(), "Ошибка синхронизации", Toast.LENGTH_SHORT).show();
        mSyncLayout.setVisibility(View.GONE);
        mFabSpeedDial.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFileUploadStart(String fileName) {

    }

    @Override
    public void onFileUploadEnd(String fileName, boolean fail) {

    }

    @Override
    public void onFileDownloadStart(String fileName) {

    }

    @Override
    public void onFileDownloadProgress(String fileName, int progress) {

    }

    @Override
    public void onFileDownloadStartDecrypt(String fileName) {

    }

    @Override
    public void onFileDownloadEndDecrypt(String fileName) {

    }

    @Override
    public void onFileDownloadEnd(String fileName, boolean fail) {

    }

    @Override
    public void onSingleFileUploadComplete(FileData fd) {
        mStorageAdapter.addToList(fd);
    }

    @Override
    public void onSingleFileUploadFailure() {

    }

    @Override
    public void onFolderCreateComplete() {

    }

    @Override
    public void onFolderCreateFailure() {

    }

    @Override
    public void onFileRenameComplete() {

    }

    @Override
    public void onFileRenameFailure() {

    }

    @Override
    public void onFolderRenameComplete() {

    }

    @Override
    public void onFolderRenameFailure() {

    }

    @Override
    public void onFolderLongClick(FileData fileData) {

    }

    @Override
    public void onFileLongClick(FileData fileData) {

    }

    @Override
    public void onFileClick(FileData fileData) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        if (fileData.getStatus(aes).equals(FileData.OK)) {
            String name = fileData.getName(aes);
            File file = new File(StorageAPI.getUserRootDirectory() + fileData.getPath(aes) +
                    File.separator + name);

            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(name.lastIndexOf(".")));

            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(getContext(), "File not ready", Toast.LENGTH_SHORT).show();
        }
    }


}
