package com.android.dishpatch.dover.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Lenovo on 4/23/2016.
 */
public class ItemDescription implements Parcelable {

    private int mOrderItemId;
    private Item mItem;
    private String mRemarks;
    private int mQuantity;
    private static int id=0;




    public ItemDescription(Item item, int quantity)
    {
        mItem = item;
        mQuantity = quantity;
    }

    public ItemDescription(Item item, int quantity, String remarks)
    {
        mItem = item;
        mQuantity = quantity;
        mRemarks = remarks;
    }

    public ItemDescription(int orderItemId, Item item, String remarks, int quantity) {
        mOrderItemId = orderItemId;
        mItem = item;
        mRemarks = remarks;
        mQuantity = quantity;
    }


    protected ItemDescription(Parcel in) {
        mOrderItemId = in.readInt();
        mItem = in.readParcelable(Item.class.getClassLoader());
        mRemarks = in.readString();
        mQuantity = in.readInt();
    }

    public static final Creator<ItemDescription> CREATOR = new Creator<ItemDescription>() {
        @Override
        public ItemDescription createFromParcel(Parcel in) {
            return new ItemDescription(in);
        }

        @Override
        public ItemDescription[] newArray(int size) {
            return new ItemDescription[size];
        }
    };

    public Item getItem() {
        return mItem;
    }

    public void setItem(Item item) {
        mItem = item;
    }



    public String getRemarks() {
        return mRemarks;
    }

    public void setRemarks(String remarks) {
        mRemarks = remarks;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public int getOrderItemId() {
        return mOrderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        mOrderItemId = orderItemId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOrderItemId);
        dest.writeParcelable(mItem, flags);
        dest.writeString(mRemarks);
        dest.writeInt(mQuantity);
    }
}
