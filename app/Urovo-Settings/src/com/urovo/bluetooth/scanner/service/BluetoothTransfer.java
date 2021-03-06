package com.urovo.bluetooth.scanner.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;
public abstract class BluetoothTransfer {

    private BluetoothTransfer() {}

    public static void sendFile(OutputStream out, String file) throws IOException {
        if (out == null || file == null) {
            return;
        }
        byte[] fileBytes = getfileBytes(file);
        Log.i("urovo","11111111111fileBytes:"+fileBytes.length);
        out.write(fileBytes);//将消息字节发出
        out.flush();//确保所有数据已经被写出，否则抛出异常
        //out.close();
    }

    public static void readFile(InputStream in, String file) throws Exception {
        if (in == null || file == null) {
            return;
        }
        DataInputStream dataIn = new DataInputStream(in);
        byte[] fileBytes = inputStream2Bytes(dataIn, 1024);

        bytes2File(fileBytes, new File(file));
    }

    public static byte[] getfileBytes(String file) throws IOException {
        File publicKeyTemp = new File(file);
        DataInputStream in = new DataInputStream(new FileInputStream(publicKeyTemp));
        return inputStream2Bytes(in, 1024);
    }

    public static byte[] inputStream2Bytes(DataInputStream in, int dept) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[dept];
        int length = -1;
        while ((length = in.read(data, 0, dept)) != -1){
            out.write(data, 0, length);
            Log.i("", "inputStream2Bytes:!!!!!!!!!!!!!!!!!!!!!!!!");
}
        byte[] result = out.toByteArray();
        out.close();
        return result;
    }

    public static byte[] inputStream2Bytes(InputStream in, int dept) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[dept];
        int length = -1;
        while ((length = in.read(data, 0, dept)) != -1)
            out.write(data, 0, length);
        byte[] result = out.toByteArray();
        out.close();
        return result;
    }

    public static void bytes2File(byte[] buf, File file) throws Exception {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            throw new Exception("bytes2File Error:" + e.toString());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw new IOException("bytes2File Error:" + e.toString());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new IOException("bytes2File Error:" + e.toString());
                }
            }
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            