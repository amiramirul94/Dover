package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lenovo on 4/23/2016.
 */
public class Customer implements Parcelable {
    private String mUuid;
    private String mName;
    private String mPictureUrl;

    Customer(){
        mName = "Muhammad Zafran";
    }

    Customer(String name, String uuid)
    {
        mName = name;
        mUuid = uuid;
    }

    public Customer(String uuid, String name, String pictureUrl) {
        mUuid = uuid;
        mName = name;
        mPictureUrl = pictureUrl;
    }

    protected Customer(Parcel in) {
        mUuid = in.readString();
        mName = in.readString();
        mPictureUrl = in.readString();
    }

    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        mPictureUrl = pictureUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeString(mName);
        dest.writeString(mPictureUrl);
    }
}
