package com.applexis.aimos_android.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.applexis.aimos_android.ui.fragment.ContactsFragment;
import com.applexis.aimos_android.ui.fragment.DialogsFragment;
import com.applexis.aimos_android.ui.fragment.MyPageFragment;
import com.applexis.aimos_android.ui.fragment.NotificationsFragment;
import com.applexis.aimos_android.ui.fragment.StorageFragment;

/**
 * @author applexis
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 5;

    private FragmentManager fm;
    private MyPageFragment myPageFragment;
    private ContactsFragment contactsFragment;
    private DialogsFragment dialogsFragment;
    private NotificationsFragment notificationsFragment;
    private StorageFragment storageFragment;

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                if(myPageFragment == null){
                    myPageFragment = new MyPageFragment();
                }
                myPageFragment.updateTextValues();
                fragment = myPageFragment;
                break;
            case 1:
                if(contactsFragment == null){
                    contactsFragment = new ContactsFragment();
                }
                fragment = contactsFragment;
                break;
            case 2:
                if(dialogsFragment == null){
                    dialogsFragment = new DialogsFragment();
                }
                fragment = dialogsFragment;
                break;
            case 3:
                if(notificationsFragment == null){
                    notificationsFragment = new NotificationsFragment();
                }
                fragment = notificationsFragment;
                break;
            case 4:
                if(storageFragment == null){
                    storageFragment = new StorageFragment();
                }
                fragment = storageFragment;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }


}
