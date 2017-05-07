package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.MessageMinimal;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author applexis
 * 0 - curent user's message
 * 1 - non current user's message
 */
public class MessageAdapter extends RecyclerView.Adapter {

    private static final int DECRYPTED_TEXT_KEY = 1001;
    private static final int TEXT_VIEW_KEY = 1001;

    private String[] monthList;

    private List<MessageMinimal> messageList;
    private Context ctx;

    public MessageAdapter(Context context, List<MessageMinimal> messageList) {
        this.messageList = messageList;
        this.ctx = context;
        monthList = new String[] {
                context.getString(R.string.january),
                context.getString(R.string.february),
                context.getString(R.string.march),
                context.getString(R.string.april),
                context.getString(R.string.may),
                context.getString(R.string.june),
                context.getString(R.string.july),
                context.getString(R.string.august),
                context.getString(R.string.september),
                context.getString(R.string.october),
                context.getString(R.string.november),
                context.getString(R.string.december)
        };
    }

    public void updateMessageList(List<MessageMinimal> messageList) {
        this.messageList = messageList;
        notifyItemRangeChanged(0, messageList.size());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_message_my, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_message_from, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        MessageMinimal m = messageList.get(position);
        final ViewHolder vh = (ViewHolder) holder;

        Calendar c = Calendar.getInstance();
        c.setTime(m.getDatetime(aes));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String hourString = String.format(Locale.ENGLISH, hour >= 10 ? "%d" : "0%d", hour);
        int minute = c.get(Calendar.MINUTE);
        String minuteString = String.format(Locale.ENGLISH, minute >= 10 ? "%d" : "0%d", minute);
        int second = c.get(Calendar.SECOND);
        String secondString = String.format(Locale.ENGLISH, second >= 10 ? "%d" : "0%d", second);
        String dateFormat = String.format(Locale.ENGLISH, "%s:%s:%s %d %s %d", hourString,
                minuteString, secondString, c.get(Calendar.DAY_OF_MONTH),
                monthList[c.get(Calendar.MONTH)].substring(0, 3).toLowerCase(), c.get(Calendar.YEAR));

        String decryptedMessage = new AESCrypto(m.getKey(aes)).decrypt(m.geteText(aes));

        vh.datetime.setText(dateFormat);
        vh.text.setText(decryptedMessage);

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        return messageList.get(position).getIdUserFrom(aes).equals(SharedPreferencesHelper.getId()) ? 0 : 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.adapter_message_profile_photo)
        public ImageView photo;
        @BindView(R.id.adapter_message_datetime)
        public TextView datetime;
        @BindView(R.id.adapter_message_text)
        public TextView text;
        @BindView(R.id.adapter_message_content_layout)
        public View contentLayout;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
