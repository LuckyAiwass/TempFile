package com.ubx.decoder;

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
 * @Date: 19-9-30上午11:02
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static byte[] CreateCroppedGreyscaleJpg(byte[] baYuvData, int fullwidth, int fullheight, BarcodeBounds bounds, int iJpgQuality) {
        try {
            int iCropLeft = Math.min(Math.min(Math.min(bounds.getTopLeft().x, bounds.getTopRight().x), bounds.getBottomRight().x), bounds.getBottomLeft().x);
            int iCropRight = Math.max(Math.max(Math.max(bounds.getTopLeft().x, bounds.getTopRight().x), bounds.getBottomRight().x), bounds.getBottomLeft().x);
            int iCropTop = Math.min(Math.min(Math.min(bounds.getTopLeft().y, bounds.getTopRight().y), bounds.getBottomRight().y), bounds.getBottomLeft().y);
            int iCropBottom = Math.max(Math.max(Math.max(bounds.getTopLeft().y, bounds.getTopRight().y), bounds.getBottomRight().y), bounds.getBottomLeft().y);

            iCropLeft = Math.max(iCropLeft, 0);
            iCropRight = Math.min(iCropRight, fullwidth);
            iCropTop = Math.max(iCropTop, 0);
            iCropBottom = Math.min(iCropBottom, fullheight);

            int iCropWidth = iCropRight - iCropLeft;
            int iCropHeight = iCropBottom - iCropTop;

            int[] pixels = ExtractPixelData(baYuvData, fullwidth, iCropTop, iCropLeft, iCropWidth, iCropHeight);

            Bitmap bitmap = Bitmap.createBitmap(iCropWidth, iCropHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, iCropWidth, 0, 0, iCropWidth, iCropHeight);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, iJpgQuality, baos);
            baos.flush();
            baos.close();
            byte[] baImg = baos.toByteArray();
            bitmap.recycle();
            return baImg;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap CreateCroppedGreyscalebitmap(byte[] baYuvData, int fullwidth, int fullheight, BarcodeBounds bounds, int iJpgQuality) {
        try {
            int iCropLeft = Math.min(Math.min(Math.min(bounds.getTopLeft().x, bounds.getTopRight().x), bounds.getBottomRight().x), bounds.getBottomLeft().x);
            int iCropRight = Math.max(Math.max(Math.max(bounds.getTopLeft().x, bounds.getTopRight().x), bounds.getBottomRight().x), bounds.getBottomLeft().x);
            int iCropTop = Math.min(Math.min(Math.min(bounds.getTopLeft().y, bounds.getTopRight().y), bounds.getBottomRight().y), bounds.getBottomLeft().y);
            int iCropBottom = Math.max(Math.max(Math.max(bounds.getTopLeft().y, bounds.getTopRight().y), bounds.getBottomRight().y), bounds.getBottomLeft().y);

            iCropLeft = Math.max(iCropLeft, 0);
            iCropRight = Math.min(iCropRight, fullwidth);
            iCropTop = Math.max(iCropTop, 0);
            iCropBottom = Math.min(iCropBottom, fullheight);

            int iCropWidth = iCropRight - iCropLeft;
            int iCropHeight = iCropBottom - iCropTop;

            int[] pixels = ExtractPixelData(baYuvData, fullwidth, iCropTop, iCropLeft, iCropWidth, iCropHeight);

            Bitmap bitmap = Bitmap.createBitmap(iCropWidth, iCropHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, iCropWidth, 0, 0, iCropWidth, iCropHeight);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap CreateGreyscaleBitmap(byte[] baYuvData, int fullwidth, int fullheight) {
        try {
            int[] pixels = ExtractPixelData(baYuvData, fullwidth, 0, 0, fullwidth, fullheight);

            Bitmap bitmap = Bitmap.createBitmap(fullwidth, fullheight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, fullwidth, 0, 0, fullwidth, fullheight);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap CreateGreyscaleBitmap(byte[] baYuvData, int fullwidth, int fullheight, Bitmap.Config config) {
        try {
            int[] pixels = ExtractPixelData(baYuvData, fullwidth, 0, 0, fullwidth, fullheight);

            Bitmap bitmap = Bitmap.createBitmap(fullwidth, fullheight, config);
            bitmap.setPixels(pixels, 0, fullwidth, 0, 0, fullwidth, fullheight);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean SaveImageAsPNG(String sFileName, byte[] baImage, int iWidth, int iHeight) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            Bitmap oBMP = CreateGreyscaleBitmap(baImage, iWidth, iHeight);
            OutputStream outStream = new FileOutputStream(file);
            oBMP.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean SaveImageAsJPG(String sFileName, byte[] baImage, int iWidth, int iHeight, int jpgQuality) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            Bitmap oBMP = CreateGreyscaleBitmap(baImage, iWidth, iHeight);
            OutputStream outStream = new FileOutputStream(file);
            oBMP.compress(Bitmap.CompressFormat.JPEG, jpgQuality, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean YTOSaveImageAsJPG(String dirName, String fileName, Bitmap image) {
        try {
            File fileDir = new File(dirName);
            if(!fileDir.exists())
                fileDir.mkdirs();
            File file = new File(String.format("%s/%s.jpg", dirName, fileName));
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean SaveImageAsPNG(String sFileName, Bitmap image) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean saveImageAsPNG(String filePath, Bitmap image) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean SaveImageAsJPG(String sFileName, Bitmap image) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean saveRawImage(String sFileName, byte[] baImage) {
        try {
            File file = new File(sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            outStream.write(baImage);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean SaveRawImage(String sFileName, byte[] baImage) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), sFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            outStream.write(baImage);
            outStream.flush();
            outStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int[] ExtractPixelData(byte[] baYuvData, int fullwidth, int iCropTop, int iCropLeft, int iCropWidth, int iCropHeight) {
        int[] pixels = new int[iCropWidth * iCropHeight];
        int inputOffset = iCropTop * fullwidth + iCropLeft;

        for (int y = 0; y < iCropHeight; y++) {
            int outputOffset = y * iCropWidth;
            for (int x = 0; x < iCropWidth; x++) {
                int grey = baYuvData[(inputOffset + x)] & 0xFF;
                pixels[(outputOffset + x)] = (0xFF000000 | grey * 65793);
            }

            inputOffset += fullwidth;
        }

        return pixels;
    }

    private byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private byte[] rotateYUVDegree270(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }// Rotate the U and V color components
        i = imageWidth * imageHeight;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
            }
        }
        return yuv;
    }

    private byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
        // Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return yuv;
    }

    /*byte[] RGBbyteArray = frameData.array();
    // allocate buffer to store bitmap RGB pixel data
    int[] rgb = convertByteToIntArray(RGBbyteArray);*/
    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }


    // 将纯RGB数据数组转化成int像素数组
    public static int[] convertByteToIntArray(byte[] data) {
        int size = data.length;
        if (size == 0) return null;

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }
        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size / 3 + arg];
        int r, g, b;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                r = convertByteToInt(data[i * 3]);
                g = convertByteToInt(data[i * 3 + 1]);
                b = convertByteToInt(data[i * 3 + 2]);
                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (r << 16) | (g << 8) | b | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                r = convertByteToInt(data[i * 3]);
                g = convertByteToInt(data[i * 3 + 1]);
                b = convertByteToInt(data[i * 3 + 2]);
                color[i] = (r << 16) | (g << 8) | b | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    private int[] rgb24ToPixel(byte[] rgb24, int width, int height) {
        int[] pix = new int[rgb24.length / 3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int idx = width * i + j;
                int rgbIdx = idx * 3;
                int red = rgb24[rgbIdx];
                int green = rgb24[rgbIdx + 1];
                int blue = rgb24[rgbIdx + 2];
                int color = (blue & 0x000000FF) | (green << 8 & 0x0000FF00) | (red << 16 & 0x00FF0000);
                pix[idx] = color;
            }
        }
        return pix;
    }

    private byte[] pixelToRgb24(int[] pix, int width, int height) {
        byte[] rgb24 = new byte[width * height * 3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int idx = width * i + j;
                int color = pix[idx]; //获取像素
                int red = ((color & 0x00FF0000) >> 16);
                int green = ((color & 0x0000FF00) >> 8);
                int blue = color & 0x000000FF;

                int rgbIdx = idx * 3;
                rgb24[rgbIdx] = (byte) red;
                rgb24[rgbIdx + 1] = (byte) green;
                rgb24[rgbIdx + 2] = (byte) blue;
            }
        }
        return rgb24;
    }

    /*# １．坐标处理函数封装
    # 图像镜像后坐标(height,width=原图高度,原图宽度,x,y=原图上某点的坐标(x,y),
    # 返回经过纵轴镜像后的坐标(x,y))*/
    public static Point mirrorPosition(int height, int width, int x, int y) {
        int x_center =width/2;
        int y_center = height/2;
        int y0 = y;
        int x0 = 2 * x_center - x;
        return new Point(Math.round(x0),Math.round(y0));
    }

    //# 图像裁剪后坐标(height,width=原图高度,原图宽度,x,y=原图上某点的坐标(x,y),
    //# pro=裁剪比例,返回经过比例裁剪后的坐标(x,y))
    public static Point cropPosition(int height, int width, int x, int y, int pro) {
        int crop_size_x = width/pro;
        int crop_size_y = height/pro;
        int x0 = x-crop_size_x;
        int y0 = y-crop_size_y;
        return new Point(Math.round(x0),Math.round(y0));
    }

    //# 图像裁剪后坐标(height,width=原图高度,原图宽度,ori_x,ori_y=原图上某点的坐标(x,y),
    //# crop_x,crop_y=x轴裁剪大小,y轴裁剪大小,返回经过比例裁剪后的坐标(x,y))
    public static Point cropPositionMap(int ori_x, int ori_y,int crop_x,int crop_y) {
        int new_x = ori_x - crop_x;
        int new_y = ori_y - crop_y;
        return new Point(Math.round(new_x),Math.round(new_y));
    }

    //# 图像镜像后坐标(h,w=原图高度,原图宽度,angle旋转角度(注意顺逆时针旋转),
    //# x1,y1=原图上某点的坐标(x,y),x2,y2=原图中点坐标(x,y),返回经过纵轴镜像后的坐标(x,y))
    public static Point rotate_position(int w,int h,int angle,int x1,int y1,int x2,int y2) {
        y1 = h - y1;
        y2 = h - y2;
        double x = (x1 - x2) * Math.cos(Math.PI / 180.0 * angle) - (y1 - y2) * Math.sin(Math.PI / 180.0 * angle) + x2;
        double y = (x1 - x2) * Math.sin(Math.PI / 180.0 * angle) + (y1 - y2) * Math.cos(Math.PI / 180.0 * angle) + y2;
        y = h - y;
        return new Point(Math.round((int)x),Math.round((int)y));
    }

}
