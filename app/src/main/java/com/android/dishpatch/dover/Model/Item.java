package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable{

    private int mItemId;
    private String mItemName;
    private double mPrice;
    private Boolean mAvailable;
    private String mCategory;
    private int mQuantity;
    private String mUnit;
    private String mPictureUrl;



    public Item()
    {

    }

    public Item(String itemName, double price, int id)
    {
        mItemId = id;
        mItemName = itemName;
        mPrice = price;
        mAvailable = true;
    }

    public Item(int itemId, String itemName, double price, Boolean available, String category, int quantity, String unit, String pictureUrl) {
        mItemId = itemId;
        mItemName = itemName;
        mPrice = price;
        mAvailable = available;
        mCategory = category;
        mQuantity = quantity;
        mUnit = unit;
        mPictureUrl = pictureUrl;
    }

    public Item(int itemId, String itemName, double price, String category, int quantity, String unit, String pictureUrl) {
        mItemId = itemId;
        mItemName = itemName;
        mPrice = price;
        mCategory = category;
        mQuantity = quantity;
        mUnit = unit;
        mPictureUrl = pictureUrl;
    }

    public Item(int mItemId, String itemName, double price, Boolean available, String pictureUrl) {
        this.mItemId = mItemId;
        mItemName = itemName;
        mPrice = price;
        mAvailable = available;
        mPictureUrl = pictureUrl;
    }


    protected Item(Parcel in) {
        mItemId = in.readInt();
        mItemName = in.readString();
        mPrice = in.readDouble();
        mCategory = in.readString();
        mPictureUrl = in.readString();
        mUnit = in.readString();
        mQuantity = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public int getItemId() {
        return mItemId;
    }

    public void setItemId(int itemId) {
        this.mItemId = itemId;
    }

    public String getItemName() {
        return mItemName;
    }

    public void setItemName(String itemName) {
        mItemName = itemName;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(float price) {
        mPrice = price;
    }

    public Boolean getAvailable() {
        return mAvailable;
    }

    public void setAvailable(Boolean available) {
        mAvailable = available;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        mPictureUrl = pictureUrl;
    }


    public void setPrice(double price) {
        mPrice = price;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mItemId);
        dest.writeString(mItemName);
        dest.writeDouble(mPrice);
        dest.writeString(mCategory);
        dest.writeString(mPictureUrl);
        dest.writeString(mUnit);
        dest.writeInt(mQuantity);
    }
}
