package com.android.dishpatch.dover.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Store implements Parcelable{

    private int mId;
    private String uuid;
    private String mName;
    private String mCity;
    private String mAddress;
    private float mDistance;
    private float mRating;
    private Uri mPhotoUri;
    private double mLatitude;
    private double mLongitude;
    private String mDistanceString=null;
    private List<Item> mItem = new ArrayList<>();

    public Store()
    {
        mName = "Mak Ngah";
        for(int i=0;i<10;i++)
        {
            Item item = new Item();
            item.setItemName("Food #"+i);
            item.setPrice(i);
            mItem.add(item);
        }
    }

    public Store(int id, String uuid, String name, Uri photoUri) {
        mId = id;
        this.uuid = uuid;
        mName = name;
        mPhotoUri = photoUri;
    }

    public Store(int id, String name, Uri photoUri, double latitude, double longitude) {
        mId = id;
        mName = name;
        mPhotoUri = photoUri;
        mLatitude = latitude;
        mLongitude = longitude;
    }


    protected Store(Parcel in) {
        mId = in.readInt();
        uuid = in.readString();
        mName = in.readString();
        mCity = in.readString();
        mAddress = in.readString();
        mDistance = in.readFloat();
        mRating = in.readFloat();
        mPhotoUri = in.readParcelable(Uri.class.getClassLoader());
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mDistanceString = in.readString();
        mItem = in.createTypedArrayList(Item.CREATOR);
    }

    public static final Creator<Store> CREATOR = new Creator<Store>() {
        @Override
        public Store createFromParcel(Parcel in) {
            return new Store(in);
        }

        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public List<Item> getItem() {
        return mItem;
    }

    public void setItem(List<Item> item) {
        mItem = item;
    }

    public float getDistance() {
        return mDistance;
    }

    public void setDistance(float distance) {
        mDistance = distance;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float rating) {
        mRating = rating;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        mPhotoUri = photoUri;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDistanceString() {
        return mDistanceString;
    }

    public void setDistanceString(String distanceString) {
        mDistanceString = distanceString;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(uuid);
        dest.writeString(mName);
        dest.writeString(mCity);
        dest.writeString(mAddress);
        dest.writeFloat(mDistance);
        dest.writeFloat(mRating);
        dest.writeParcelable(mPhotoUri, flags);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mDistanceString);
        dest.writeTypedList(mItem);
    }
}
