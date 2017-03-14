package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lenovo on 11/30/2016.
 */

public class RunnerTask implements Parcelable {

    private Customer mCustomer;
    private Order mOrder;

    public RunnerTask(Customer customer, Order order) {
        mCustomer = customer;
        mOrder = order;
    }

    protected RunnerTask(Parcel in) {
        mCustomer = in.readParcelable(Customer.class.getClassLoader());
        mOrder = in.readParcelable(Order.class.getClassLoader());
    }

    public static final Creator<RunnerTask> CREATOR = new Creator<RunnerTask>() {
        @Override
        public RunnerTask createFromParcel(Parcel in) {
            return new RunnerTask(in);
        }

        @Override
        public RunnerTask[] newArray(int size) {
            return new RunnerTask[size];
        }
    };

    public Customer getCustomer() {
        return mCustomer;
    }

    public void setCustomer(Customer customer) {
        mCustomer = customer;
    }

    public Order getOrder() {
        return mOrder;
    }

    public void setOrder(Order order) {
        mOrder = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mCustomer, flags);
        dest.writeParcelable(mOrder, flags);
    }
}
