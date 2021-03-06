package com.ubx.keyremap.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ubx.keyremap.R;

public class ListItem {
    public static class Common {
        public int iconResId = -1;
        public int arrowResId = R.mipmap.arrow;
        public String titleText = "test text";
        public boolean deliverHide = true;
        public String clickKey = null;
    }

    public static class IntentExtra implements Parcelable {

        public String key = null;
        public String value = null;
        public boolean deliverHide = true;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isDeliverHide() {
            return deliverHide;
        }

        public void setDeliverHide(boolean deliverHide) {
            this.deliverHide = deliverHide;
        }

        public static final Creator<IntentExtra> CREATOR = new Creator<IntentExtra>() {
            @Override
            public IntentExtra createFromParcel(Parcel in) {
                IntentExtra ec =  new IntentExtra();
                ec.setKey(in.readString());
                ec.setValue(in.readString());
                ec.setDeliverHide(in.readByte() != 0);
                return ec;
            }

            @Override
            public IntentExtra[] newArray(int size) {
                return new IntentExtra[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(key);
            parcel.writeString(value);
            parcel.writeByte((byte) (deliverHide ? 1 : 0));
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               