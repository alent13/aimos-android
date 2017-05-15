package com.applexis.aimos_android.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.StorageAPI;
import com.applexis.aimos_android.network.model.FileData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.yavski.fabspeeddial.FabSpeedDial;

public class StorageFragment extends Fragment implements StorageAPI.OnStorageAPIListener {

    @BindView(R.id.fragment_storage_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fragment_storage_sync_layout)
    View mSyncLayout;
    @BindView(R.id.fragment_storage_fab)
    FabSpeedDial mFabSpeedDial;
    Menu mToolbarMenu;
    MenuItem itemSync;
    MenuItem itemCopy;
    MenuItem itemDelete;
    MenuItem itemCut;
    MenuItem itemSelectAll;
    MenuItem itemProperties;
    MenuItem itemSort;
    MenuItem itemFolderFirst;
    MenuItem itemSettings;

    private StorageAPI mStorageAPI;
    private Unbinder unbinder;

    public StorageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        unbinder = ButterKnife.bind(this, view);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);
        mStorageAPI = new StorageAPI(getContext());
        mSyncLayout.animate()
                .setDuration(400)
                .translationY(mSyncLayout.getHeight());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_storage_actions, menu);
        mToolbarMenu = menu;

        menu.setGroupVisible(R.id.menu_storage_select_group, false);

        itemSync = menu.findItem(R.id.menu_storage_sync_action);
        itemCopy = menu.findItem(R.id.menu_storage_copy_action);
        itemDelete = menu.findItem(R.id.menu_storage_del_action);
        itemCut = menu.findItem(R.id.menu_storage_move_action);
        itemSelectAll = menu.findItem(R.id.menu_storage_select_all_action);
        itemProperties = menu.findItem(R.id.menu_storage_properties_action);
        itemSort = menu.findItem(R.id.menu_storage_sort_action);
        itemFolderFirst = menu.findItem(R.id.menu_storage_folder_first_action);
        itemSettings = menu.findItem(R.id.menu_storage_folder_first_action);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void sync() {
        mStorageAPI.sync();
    }

    public void hideView(View view) {
        view.animate()
                .translationY(0)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public void showView(View view) {
        view.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSyncComplete(List<FileData> fileDatas) {

    }

    @Override
    public void onSyncFailure() {

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
    public void onSingleFileUploadComplete(String name) {

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
}
