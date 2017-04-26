package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.StorageData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.applexis.aimos_android.network.model.StorageData.Status.ERROR;
import static com.applexis.aimos_android.network.model.StorageData.Status.OK;
import static com.applexis.aimos_android.network.model.StorageData.Status.SYNC;

/**
 * Created by applexis on 4/21/2017.
 * 0 - FILE
 * 1 - FOLDER
 */
public class StorageAdapter extends RecyclerView.Adapter {

    List<StorageData> mData;
    Context context;
    String[] monthList;

    public StorageAdapter(Context context, List<StorageData> mData) {
        this.mData = mData;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_file_item, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_folder_item, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ViewHolder vh = (ViewHolder) holder;
        StorageData data = mData.get(position);
        vh.nameText.setText(data.getName());
        vh.publicImage.setVisibility(data.isPublic() ? View.VISIBLE : View.INVISIBLE);
        switch (data.getStatus()) {
            case OK:
                vh.statusImage.setImageResource(R.drawable.ic_status_ok);
                vh.statusImage.setColorFilter(ContextCompat.getColor(context,R.color.status_ok));
                break;
            case SYNC:
                vh.statusImage.setImageResource(R.drawable.ic_status_sync);
                vh.statusImage.setColorFilter(ContextCompat.getColor(context,R.color.status_sync));
                break;
            case ERROR:
                vh.statusImage.setImageResource(R.drawable.ic_status_error);
                vh.statusImage.setColorFilter(ContextCompat.getColor(context,R.color.status_error));
                break;

        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(data.getCreateDatetime()));
        switch (data.getType()) {
            case FILE:
                Double byteSize = data.getSize().doubleValue();
                String size = byteSize < 1024 ? String.valueOf(byteSize) + " B" :
                        byteSize < 1024 * 1024 ? String.valueOf(byteSize / 1024) + "KB" :
                                byteSize < 1024 * 1024 * 1024 ? String.valueOf(byteSize / 1024 * 1024) + "MB" :
                                        String.valueOf(byteSize / 1024 * 1024 * 1024) + "MB";
                vh.infoText.setText(String.format(Locale.ENGLISH, "%d %s %d %d:%d:%d %s",
                        c.get(Calendar.DATE), monthList[c.get(Calendar.MONTH)].substring(0, 3),
                        c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                        c.get(Calendar.SECOND), size));
                break;
            case FOLDER:
                vh.infoText.setText(String.format(Locale.ENGLISH, "%d %s %d %d:%d:%d",
                        c.get(Calendar.DATE), monthList[c.get(Calendar.MONTH)].substring(0, 2),
                        c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                        c.get(Calendar.SECOND)));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.adapter_storage_name)
        TextView nameText;
        @BindView(R.id.adapter_storage_info_text)
        TextView infoText;
        @BindView(R.id.adapter_storage_item_public)
        ImageView publicImage;
        @BindView(R.id.adapter_storage_item_status)
        ImageView statusImage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
