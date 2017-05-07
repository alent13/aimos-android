package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.DialogMinimal;
import com.applexis.aimos_android.ui.activity.DialogActivity;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author applexis
 */

public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_dialog, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DialogMinimal dialogMinimal = dialogList.get(position);
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        holder.dialogName.setText(dialogMinimal.getName(aes));
        holder.dialogSenderNameText.setText(dialogMinimal.getLastSender().getId(aes) == SharedPreferencesHelper.getId() ?
            ctx.getString(R.string.dialogLastMyMessageText) :
                ctx.getString(R.string.dialogLastMessageNameText, dialogMinimal.getLastSender().getName(aes)));

        holder.dialogLastMessageText.setText(
                new AESCrypto(dialogMinimal.getLastMessage().getKey(aes))
                        .decrypt(dialogMinimal.getLastMessage().geteText(aes)));

        holder.itemView.setTag(dialogMinimal);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, DialogActivity.class);
                intent.putExtra(DIALOG, (DialogMinimal) v.getTag());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dialogList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.adapter_dialog_logo)
        public ImageView dialogLogo;
        @BindView(R.id.adapter_dialog_name)
        public TextView dialogName;
        @BindView(R.id.adapter_dialog_last_sender)
        public TextView dialogSenderNameText;
        @BindView(R.id.adapter_dialog_last_message)
        public TextView dialogLastMessageText;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
