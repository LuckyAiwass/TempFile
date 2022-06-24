package com.android.server.scanner;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Copyright (C) 2019, Urovo Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @Author: rocky
 * @Date: 20-5-11下午7:25
 */
public class DecodeData implements Parcelable {
    private byte[] barcodeByteData = null;
    private int length = 0;
    private long decodeTime = 0L;
    //private int[] bounds = null;
    /** This variable will contain the Honeywell Code ID for the decoded symbology */
    private byte codeId = 0;
    /** This variable will contain the AIM ID for the decoded symbology */
    private byte aimCodeLetter = 0;
    /** This variable will contain the code modifier for the decoded symbology */
    private byte aimModifier = 0;
    private int symbologyId = 0;
    //private long symbologyExId = 0L;
    public void setBarcodeDataBytes(byte[] data)
    {
        this.barcodeByteData = data;
    }
    public byte[] getBarcodeDataBytes()
    {
        return this.barcodeByteData;
    }
    public int getBarcodeDataLength()
    {
        return this.length;
    }
    public void setBarcodeDataLength(int size)
    {
        this.length = size;
    }
    public long getDecodeTime()
    {
        return this.decodeTime;
    }
    public void setDecodeTime(long time) {
        decodeTime = time;
    }
    /*public int[] getBarcodeBounds()
    {
        return this.bounds;
    }

    public void setBarcodeBounds(int[] barcodeBounds)
    {
        this.bounds =barcodeBounds;
    }*/

    public byte getCodeId()
    {
        return this.codeId;
    }
    public void setCodeId(byte symCodeId)
    {
        this.codeId = symCodeId;
    }
    public byte getAIMCodeLetter()
    {
        return this.aimCodeLetter;
    }
    public void setAIMCodeLetter(byte aimId)
    {
        aimCodeLetter = aimId;
    }
    public byte getAIMModifier()
    {
        return this.aimModifier;
    }
    public void setAIMModifier(byte modifier)
    {
        aimModifier = modifier;
    }
    public int getSymbologyId(){
        return symbologyId;
    }
    public void setSymbologyId(int symId){
        symbologyId = symId;
    }
    /*public void setSymbologyIdEx(long idEx)
    {
        symbologyExId = idEx;
    }*/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.barcodeByteData);
        dest.writeInt(this.length);
        dest.writeLong(this.decodeTime);
        //dest.writeIntArray(this.bounds);
        dest.writeByte(this.codeId);
        dest.writeByte(this.aimCodeLetter);
        dest.writeByte(this.aimModifier);
        dest.writeInt(this.symbologyId);
        //dest.writeLong(this.symbologyExId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DecodeData> CREATOR = new Creator<DecodeData>() {
        @Override
        public DecodeData createFromParcel(Parcel source) {
            DecodeData eap = new DecodeData();
            source.readByteArray(eap.barcodeByteData);
            eap.length = source.readInt();
            eap.decodeTime = source.readLong();
            //source.readIntArray(eap.bounds);
            eap.codeId = source.readByte();
            eap.aimCodeLetter = source.readByte();
            eap.aimModifier = source.readByte();
            eap.symbologyId = source.readInt();
            //eap.symbologyExId = source.readLong();
            return eap;
        }

        @Override
        public DecodeData[] newArray(int size) {
            return new DecodeData[size];
        }
    };
}


