package com.applexis.aimos_android.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.MessageMinimal;
import com.applexis.aimos_android.utils.DESCryptoHelper;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;

import java.util.Date;
import java.util.List;

/**
 * @author applexis
 */

public class MessageAdapter extends BaseAdapter {

    String[] monthString = {"янв", "фев", "мар", "апр", "май",
            "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};

    List<MessageMinimal> messageList;
    View.OnTouchListener showOnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((TextView) view).setText((String) view.getTag());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ((TextView) view).setText(R.string.message_press_to_show_text);
                    /*Handler mHandler = new Handler(Looper.getMainLooper());
                    TextShowRunnable runnable = new TextShowRunnable();
                    runnable.setView(view);
                    mHandler.postDelayed(runnable, 3000);*/
                    break;
            }
            return false;
        }
    };
    private Context ctx;
    private LayoutInflater lInflater;

    public MessageAdapter(Context context, List<MessageMinimal> messageList) {
        this.messageList = messageList;
        this.ctx = context;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int i) {
        return messageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageMinimal m = messageList.get(i);

        View view = convertView;
        if (view == null) {
            if (m.getIdUserFrom().equals(SharedPreferencesHelper.getId())) {
                view = lInflater.inflate(R.layout.adapter_message_my, viewGroup, false);
            } else {
                view = lInflater.inflate(R.layout.adapter_message_from, viewGroup, false);
            }
        }

        Date date = m.getDatetime();
        String dateFormat = String.format("%d:%d:%d %d %s %d", date.getHours(), date.getMinutes(), date.getSeconds(), date.getDate(), monthString[date.getMonth()], date.getYear() + 1900);
        String decryptedMessage = DESCryptoHelper.decrypt(DESCryptoHelper.getKey(m.getKey()), m.geteText());

        TextView datetimeText = ((TextView) view.findViewById(R.id.datetime));
        datetimeText.setText(dateFormat);
        TextView messageText = ((TextView) view.findViewById(R.id.message));
        messageText.setTag(decryptedMessage);
        messageText.setText(R.string.message_press_to_show_text);
        messageText.setOnTouchListener(showOnTouch);

        return view;
    }

    class TextShowRunnable implements Runnable {
        private View view;

        @Override
        public void run() {
            ((TextView) view).setText(R.string.message_press_to_show_text);
        }

        public void setView(View view) {
            this.view = view;
        }
    }

}
