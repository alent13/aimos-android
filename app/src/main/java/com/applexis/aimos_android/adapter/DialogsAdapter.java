package com.applexis.aimos_android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.activity.DialogActivity;
import com.applexis.aimos_android.network.KeyExchange;
import com.applexis.aimos_android.network.MessengerAPI;
import com.applexis.aimos_android.network.MessengerAPIClient;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.network.model.UserMinimalInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author applexis
 */

public class DialogsAdapter extends BaseAdapter {

    public static final String DIALOG = "dialog";
    public static final String DIALOG_NAME = "dialog_name";

    public List<DialogMinimal> dialogList = new ArrayList<>();
    private Context ctx;
    private LayoutInflater lInflater;

    public DialogsAdapter(Context context, List<DialogMinimal> dialogList) {
        this.dialogList = dialogList;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dialogList.size();
    }

    @Override
    public Object getItem(int i) {
        return dialogList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.adapter_dialog, viewGroup, false);
        }

        final DialogMinimal d = dialogList.get(i);

        ((TextView) view.findViewById(R.id.dialog_name)).setText(d.getName());
        view.setTag(i);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogMinimal u = (DialogMinimal) getItem((int) view.getTag());
                Intent intent = new Intent(ctx, DialogActivity.class);
                intent.putExtra(DIALOG, d);
                ctx.startActivity(intent);
            }
        });

        return view;
    }
}
