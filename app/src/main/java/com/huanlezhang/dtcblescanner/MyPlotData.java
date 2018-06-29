package com.huanlezhang.dtcblescanner;

import android.util.Log;

import com.google.common.collect.EvictingQueue;

import java.util.ArrayList;

public class MyPlotData {

    public static final int N_PLOT_DOTS = 100;

    private int[] mPlotData = new int[N_PLOT_DOTS];
    private int index = 0;

    private String mDeviceAddr;

    private ArrayList<Integer> mRssiArray = new ArrayList<>();


    MyPlotData(String deviceAddr) {
        mDeviceAddr = deviceAddr;
    }

    public void add(int rssi) {
        mRssiArray.add(rssi);
    }

    private void addToPlot(int rssi){
        mPlotData[index] = rssi;
        index = (++index) %N_PLOT_DOTS;
    }

    public void click() {

        if (mRssiArray.size() == 0) {
            addToPlot(0);
            return;
        }

        double avg = 0.0;
        for (int i = 0; i < mRssiArray.size(); i++){
            avg += Math.pow(10, (mRssiArray.get(i)-30.0)/10.0);
        }
        avg /= mRssiArray.size();

        int avgRssi = (int) Math.round(10.0*Math.log10(avg) +30.0);

        addToPlot(avgRssi);

        mRssiArray.clear();
    }

    public int[] drawData() {
        int[] ret = new int[N_PLOT_DOTS];
        int temp_index = index + N_PLOT_DOTS;
        for (int i = 0; i < N_PLOT_DOTS; i++){
            ret[N_PLOT_DOTS-1-i] = mPlotData[((--temp_index) % N_PLOT_DOTS)];
        }
        return ret;
    }

}
