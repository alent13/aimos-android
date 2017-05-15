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
import com.applexis.aimos_android.network.model.FileData;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by applexis on 4/21/2017.
 * 0 - FILE
 * 1 - FOLDER
 */
public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {

    private List<FileData> mData;
    private List<Integer> selectedItems;
    private Context context;
    private String[] monthList;

    private SortType sortBy = SortType.NAME;

    private OnItemSelect onItemSelect;

    enum SortType {
        NAME,
        DATE,
        SIZE
    }

    View.OnClickListener onSelectedItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedItems.remove(v.getTag());
            if (onItemSelect != null) {
                onItemSelect.itemUnselected(selectedItems.size());
            }
            v.setOnLongClickListener(onLongClickListener);
            v.setOnClickListener(null);
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final int selectedIndex = (Integer) v.getTag();
            selectedItems.add(selectedIndex);
            if (onItemSelect != null) {
                onItemSelect.itemSelected(selectedIndex, selectedItems.size());
            }
            v.setOnLongClickListener(null);
            v.setOnClickListener(onSelectedItemClick);
            return true;
        }
    };

    public StorageAdapter(Context context, List<FileData> mData) {
        this.mData = mData;
        monthList = new String[]{
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(StorageAdapter.ViewHolder holder, int position) {
        FileData data = mData.get(position);

        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        holder.nameText.setText(data.getName());
        holder.publicImage.setVisibility(data.isPublic(aes) ? View.VISIBLE : View.INVISIBLE);
        switch (data.getStatus()) {
            case FileData.OK:
                holder.statusImage.setImageResource(R.drawable.ic_status_ok);
                holder.statusImage.setColorFilter(ContextCompat.getColor(context, R.color.status_ok));
                break;
            case FileData.SYNC:
                holder.statusImage.setImageResource(R.drawable.ic_status_sync);
                holder.statusImage.setColorFilter(ContextCompat.getColor(context, R.color.status_sync));
                break;
            case FileData.ERROR:
                holder.statusImage.setImageResource(R.drawable.ic_status_error);
                holder.statusImage.setColorFilter(ContextCompat.getColor(context, R.color.status_error));
                break;

        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(data.getCreateDatetime()));

        int hour = c.get(Calendar.HOUR_OF_DAY);
        String hourString = String.format(Locale.ENGLISH, hour >= 10 ? "%d" : "0%d", hour);
        int minute = c.get(Calendar.MINUTE);
        String minuteString = String.format(Locale.ENGLISH, minute >= 10 ? "%d" : "0%d", minute);
        int second = c.get(Calendar.SECOND);
        String secondString = String.format(Locale.ENGLISH, second >= 10 ? "%d" : "0%d", second);

        if (data.isFolder(aes)) {
            holder.infoText.setText(String.format(Locale.ENGLISH, "%d %s %d %s:%s:%s",
                    c.get(Calendar.DATE), monthList[c.get(Calendar.MONTH)].substring(0, 2),
                    c.get(Calendar.YEAR), hourString, minuteString, secondString));
        } else {
            Double byteSize = data.getSize(aes).doubleValue();
            String size = byteSize < 1024 ? String.valueOf(byteSize) + " B" :
                    byteSize < 1024 * 1024 ? String.valueOf(byteSize / 1024) + "KB" :
                            byteSize < 1024 * 1024 * 1024 ? String.valueOf(byteSize / 1024 * 1024) + "MB" :
                                    String.valueOf(byteSize / 1024 * 1024 * 1024) + "MB";
            holder.infoText.setText(String.format(Locale.ENGLISH, "%d %s %d %s:%s:%s %s",
                    c.get(Calendar.DATE), monthList[c.get(Calendar.MONTH)].substring(0, 3),
                    c.get(Calendar.YEAR), hourString, minuteString, secondString, size));
        }
        holder.itemView.setTag(position);
        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
            holder.itemView.setOnLongClickListener(null);
            holder.itemView.setOnClickListener(onSelectedItemClick);
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.itemView.setOnLongClickListener(onLongClickListener);
            holder.itemView.setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void updateData(List<FileData> data, final AESCrypto aes) {
        mData = data;
        Collections.sort(mData, new Comparator<FileData>() {
            @Override
            public int compare(FileData o1, FileData o2) {
                if (o1.isFolder(aes) && !o2.isFolder(aes)) {
                    return -1;
                } else if (!o1.isFolder(aes) && o2.isFolder(aes)) {
                    return 1;
                } else if (o1.isFolder(aes) && o2.isFolder(aes)) {
                    return o1.getName(aes).compareTo(o2.getName(aes));
                } else {
                    switch (sortBy) {
                        case NAME:
                            return o1.getName(aes).compareTo(o2.getName(aes));
                        case SIZE:
                            return o1.getSize(aes).compareTo(o2.getSize(aes));
                        case DATE:
                            return o1.getCreateDatetime(aes).compareTo(o2.getCreateDatetime(aes));
                    }
                }
                return 0;
            }
        });
        selectedItems = new ArrayList<>();
        notifyDataSetChanged();
        notifyItemRangeInserted(0, data.size());
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

    public SortType getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortType sortBy) {
        this.sortBy = sortBy;
    }

    public void setOnItemSelect(OnItemSelect onItemSelect) {
        this.onItemSelect = onItemSelect;
    }

    interface OnItemSelect {
        public void itemSelected(int position, int selectedCount);

        public void itemUnselected(int selectedCount);
    }
}
