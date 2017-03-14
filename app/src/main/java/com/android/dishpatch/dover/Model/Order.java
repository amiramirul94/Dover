package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Lenovo on 2/28/2016.
 */
public class Order implements Parcelable {
    private int mOrderId;
    private Store mStore;
    private Runner mRunner;
    private Date mDate;
    private String mStatus;
    private float mTotalPrice;

    public Order(String status)
    {
        mStatus= status;
        mDate= new Date();
    }

    public Order(int orderId, Store store, Runner runner, Date date) {
        mOrderId = orderId;
        mStore = store;
        mRunner = runner;
        mDate = date;
    }

    public Order(int orderId, Store store, Date date) {
        mOrderId = orderId;
        mStore = store;
        mDate = date;
    }

    public Order(int orderId, Store store, Date date, float totalPrice) {
        mOrderId = orderId;
        mStore = store;
        mDate = date;
        mTotalPrice = totalPrice;
    }

    protected Order(Parcel in) {
        mOrderId = in.readInt();
        mStore = in.readParcelable(Store.class.getClassLoader());
        mRunner = in.readParcelable(Runner.class.getClassLoader());
        mStatus = in.readString();
        mTotalPrice = in.readFloat();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }



    public void setDate(Date date) {
        mDate = date;
    }




    public Date getDate() {
        return mDate;
    }


    public int getOrderId() {
        return mOrderId;
    }

    public void setOrderId(int orderId) {
        mOrderId = orderId;
    }

    public Store getStore() {
        return mStore;
    }

    public void setStore(Store store) {
        mStore = store;
    }

    public Runner getRunner() {
        return mRunner;
    }

    public void setRunner(Runner runner) {
        mRunner = runner;
    }

    public float getTotalPrice() {
        return mTotalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        mTotalPrice = totalPrice;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOrderId);
        dest.writeParcelable(mStore, flags);
        dest.writeParcelable(mRunner, flags);
        dest.writeString(mStatus);
        dest.writeFloat(mTotalPrice);
    }
}
