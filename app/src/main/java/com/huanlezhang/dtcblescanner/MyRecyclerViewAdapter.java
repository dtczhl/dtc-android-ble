package com.huanlezhang.dtcblescanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<MyBleDeviceInfoStore> mItemList;
    private Calendar mCalendar;

    public SparseBooleanArray mCheckBoxArray = new SparseBooleanArray();


    public MyRecyclerViewAdapter(ArrayList<MyBleDeviceInfoStore> myDataset) {
        mItemList = myDataset;

        mCalendar = Calendar.getInstance(Locale.ENGLISH);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.scan_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final MyBleDeviceInfoStore itemData = mItemList.get(position);
        holder.mDeviceNameView.setText(itemData.name);
        holder.mDeviceAddressView.setText(itemData.address);
        holder.mRssiView.setText(Integer.toString(itemData.rssi));

        mCalendar.setTimeInMillis(itemData.timestamp);
        String date = DateFormat.format("hh:m:ss", mCalendar).toString();
        Log.d("DTC", TimeZone.getDefault() + date );
        holder.mTimestampView.setText(date);

        // checkbox
        if (!mCheckBoxArray.get(position, false)) {
            holder.mCheckBoxView.setChecked(false);
        } else {
            holder.mCheckBoxView.setChecked(true);
        }

    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private void add(int position, MyBleDeviceInfoStore item) {
        mItemList.add(position, item);
        notifyItemInserted(position);
    }

    private void remove(int position) {
        mItemList.remove(position);
        notifyItemRemoved(position);
    }

    public int update(MyBleDeviceInfoStore item) {

        for (int i = 0; i < mItemList.size(); i++){
            if (mItemList.get(i).address.equals(item.address)) {
                mItemList.set(i, item);
                notifyItemChanged(i);
                return i;
            }
        }
        // not found
        this.add(mItemList.size(), item);
        return mItemList.size() - 1;

    }

    public void clear(){
        int itemSize = mItemList.size();
        for (int i = 0; i < itemSize; i++){
            this.remove(0);
        }
        mItemList.clear();
        mCheckBoxArray.clear();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mDeviceNameView;
        public TextView mDeviceAddressView;
        public TextView mRssiView;
        public TextView mTimestampView;
        public CheckBox mCheckBoxView;

        public View mLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView;

            mDeviceNameView = itemView.findViewById(R.id.scan_row_device_name);
            mDeviceAddressView = itemView.findViewById(R.id.scan_row_device_address);
            mRssiView = itemView.findViewById(R.id.scan_row_rssi);
            mTimestampView = itemView.findViewById(R.id.scan_row_timestamp);
            mCheckBoxView = itemView.findViewById(R.id.scan_row_checkbox);

            mCheckBoxView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            CheckBox checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                mCheckBoxArray.put(position, true);
            } else {
                mCheckBoxArray.put(position, false);
            }
        }
    }

}
