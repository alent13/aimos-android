package com.applexis.aimos_android.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.model.FileData;
import com.applexis.aimos_android.utils.SharedPreferencesHelper;
import com.applexis.utils.crypto.AESCrypto;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author applexis
 * 0 - FILE
 * 1 - FOLDER
 */
public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {

    private List<FileData> mData;
    private List<FileData> displayData;
    private Context context;
    private boolean folderFirst;
    private Stack<Long> lastParentId;
    private Long cParentId = 0L;
    private String[] monthList;

    private SortType sortBy = SortType.NAME;

    private OnItemClick onItemClick;

    enum SortType {
        NAME,
        DATE,
        SIZE
    }

    public StorageAdapter(Context context, List<FileData> displayData) {
        this.displayData = displayData;
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
        lastParentId = new Stack<>();
    }



    @Override
    public int getItemViewType(int position) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        return displayData.get(position).getIsFolder(aes) ? 1 : 0;
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
        FileData data = displayData.get(position);

        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());

        holder.nameText.setText(data.getName(aes));
        holder.publicImage.setVisibility(data.getIsPublic(aes) ? View.VISIBLE : View.INVISIBLE);
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
        holder.infoText.setText("");

        if (!data.getIsFolder(aes)) {
            Calendar c = Calendar.getInstance();
            c.setTime(data.getLastModificationDatetime(aes));

            int hour = c.get(Calendar.HOUR_OF_DAY);
            String hourString = String.format(Locale.ENGLISH, hour >= 10 ? "%d" : "0%d", hour);
            int minute = c.get(Calendar.MINUTE);
            String minuteString = String.format(Locale.ENGLISH, minute >= 10 ? "%d" : "0%d", minute);
            int second = c.get(Calendar.SECOND);
            String secondString = String.format(Locale.ENGLISH, second >= 10 ? "%d" : "0%d", second);

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
        holder.itemView.setOnClickListener(v -> {
            FileData clicked = displayData.get((int) v.getTag());
            if (clicked.getIsFolder(aes)) {
                lastParentId.push(cParentId);
                cParentId = clicked.getId(aes);
                displayData = Stream.of(mData)
                        .filter(fd -> fd.getParentId(aes).equals(cParentId))
                        .collect(Collectors.toList());
                notifyDataSetChanged();
                notifyItemRangeInserted(0, displayData.size());
            } else {
                if (onItemClick != null) {
                    onItemClick.onFileClick(clicked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayData.size();
    }

    public void updateData(List<FileData> data) {
        final AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        mData = data;
        displayData = Stream.of(mData)
                .filter(fd -> fd.getParentId(aes).equals(cParentId))
                .collect(Collectors.toList());
        if (folderFirst) {
            sortFolderFirst(aes);
        } else {
            sortSimple(aes);
        }
        notifyDataSetChanged();
        notifyItemRangeInserted(0, data.size());
    }

    private void sortFolderFirst(final AESCrypto aes) {
        Collections.sort(displayData, (o1, o2) -> {
            if (o1.getIsFolder(aes) && !o2.getIsFolder(aes)) {
                return -1;
            } else if (!o1.getIsFolder(aes) && o2.getIsFolder(aes)) {
                return 1;
            } else if (o1.getIsFolder(aes) && o2.getIsFolder(aes)) {
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
        });
    }

    public void pageUp() {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        cParentId = lastParentId.empty() ? 0L : lastParentId.pop();
        displayData = Stream.of(mData)
                .filter(fd -> fd.getParentId(aes).equals(cParentId))
                .collect(Collectors.toList());
        if (folderFirst) {
            sortFolderFirst(aes);
        } else {
            sortSimple(aes);
        }
        notifyDataSetChanged();
        notifyItemRangeInserted(0, displayData.size());
    }

    public void setFolderFirst(boolean folderFirst) {
        this.folderFirst = folderFirst;
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        if (folderFirst) {
            sortFolderFirst(aes);
        } else {
            sortSimple(aes);
        }
        notifyDataSetChanged();
        notifyItemRangeInserted(0, displayData.size());
    }

    private void sortSimple(final AESCrypto aes) {
        Collections.sort(displayData, (o1, o2) -> {
            switch (sortBy) {
                case NAME:
                    return o1.getName(aes).compareTo(o2.getName(aes));
                case SIZE:
                    return o1.getSize(aes).compareTo(o2.getSize(aes));
                case DATE:
                    return o1.getCreateDatetime(aes).compareTo(o2.getCreateDatetime(aes));
            }
            return 0;
        });
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

    public Long getCurrentParentId() {
        return cParentId;
    }

    public boolean containsFile(String name, AESCrypto aes) {
        for (FileData fileData : displayData) {
            if (fileData.getName(aes).equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addToList(FileData fileData) {
        AESCrypto aes = new AESCrypto(SharedPreferencesHelper.getGlobalAesKey());
        mData.add(fileData);
        displayData = Stream.of(mData)
                .filter(fd -> fd.getParentId(aes).equals(cParentId))
                .collect(Collectors.toList());
        if (folderFirst) {
            sortFolderFirst(aes);
        } else {
            sortSimple(aes);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public SortType getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortType sortBy) {
        this.sortBy = sortBy;
    }

    public interface OnItemClick {
        void onFolderLongClick(FileData fileData);

        void onFileLongClick(FileData fileData);

        void onFileClick(FileData fileData);
    }
}
