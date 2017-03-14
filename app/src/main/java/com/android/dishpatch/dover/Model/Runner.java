package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lenovo on 3/18/2016.
 */
public class Runner implements Parcelable{
    private String mDispatcherName;
    private String mDispatcherId;
    private double mDistance;
    private long mPhoneNumber;
    private int mAverageResponseTime;
    //private int


    public Runner(String id)
    {
        mDispatcherId = id;
    }

    public Runner()
    {
        mDispatcherName = "Muhammad Zafran";
        mDispatcherId="abc";
        mPhoneNumber = 0137446251;
        mAverageResponseTime = 20;
    }


    protected Runner(Parcel in) {
        mDispatcherName = in.readString();
        mDispatcherId = in.readString();
        mDistance = in.readDouble();
        mPhoneNumber = in.readLong();
        mAverageResponseTime = in.readInt();
    }

    public static final Creator<Runner> CREATOR = new Creator<Runner>() {
        @Override
        public Runner createFromParcel(Parcel in) {
            return new Runner(in);
        }

        @Override
        public Runner[] newArray(int size) {
            return new Runner[size];
        }
    };

    public String getDispatcherName() {
        return mDispatcherName;
    }

    public void setDispatcherName(String dispatcherName) {
        mDispatcherName = dispatcherName;
    }

    public String getDispatcherId() {
        return mDispatcherId;
    }

    public void setDispatcherId(String dispatcherId) {
        mDispatcherId = dispatcherId;
    }

    public long getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public int getAverageResponseTime() {
        return mAverageResponseTime;
    }

    public void setAverageResponseTime(int averageResponseTime) {
        mAverageResponseTime = averageResponseTime;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        mDistance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDispatcherName);
        dest.writeString(mDispatcherId);
        dest.writeDouble(mDistance);
        dest.writeLong(mPhoneNumber);
        dest.writeInt(mAverageResponseTime);
    }
}
