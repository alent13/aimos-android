package com.applexis.aimos_android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

public class StorageFragment extends Fragment {

    public StorageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        TextView tv = (TextView) view.findViewById(R.id.in_progress);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesHelper.setToken("");
            }
        });
        return view;
    }

}
