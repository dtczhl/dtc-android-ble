package com.huanlezhang.dtcblescanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<MyBleDeviceInfoStore> mItemList;

    public MyRecyclerViewAdapter(ArrayList<MyBleDeviceInfoStore> myDataset) {
        mItemList = myDataset;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MyBleDeviceInfoStore itemData = mItemList.get(position);
        holder.mDeviceNameView.setText(itemData.name);
        holder.mDeviceAddressView.setText(itemData.address);
        holder.mRssiView.setText(Integer.toString(itemData.rssi));

    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void add(int position, MyBleDeviceInfoStore item) {
        mItemList.add(position, item);
        notifyItemInserted(position);
    }

    public void update(MyBleDeviceInfoStore item) {

        for (int i = 0; i < mItemList.size(); i++){
            if (mItemList.get(i).address.equals(item.address)) {
                mItemList.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
        // not found
        this.add(mItemList.size(), item);

    }

    public void remove(int position) {
        mItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void clear(){
        int itemSize = mItemList.size();
        for (int i = 0; i < itemSize; i++){
            this.remove(0);
        }
        mItemList.clear();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDeviceNameView;
        public TextView mDeviceAddressView;
        public TextView mRssiView;

        public View mLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView;

            mDeviceNameView = itemView.findViewById(R.id.scan_row_device_name);
            mDeviceAddressView = itemView.findViewById(R.id.scan_row_device_address);
            mRssiView = itemView.findViewById(R.id.scan_row_rssi);

        }
    }

}
