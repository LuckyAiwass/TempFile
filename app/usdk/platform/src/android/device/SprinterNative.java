/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * {@hide}
 */
package android.device;
import java.io.FileDescriptor;
import android.graphics.*;
import android.util.Log;

public class SprinterNative {

	   static {
        Log.d("SprinterNative","Loading JNI Library");
        System.loadLibrary("printer_jni");
    }

	public native static void prn_paperForWard(int len);

	public native static void prn_paperBack(int len);

	public native static void prn_setHue(int hue);
	public native static void prn_setFactoryTest(int onoff);
	public native static void prn_setSpeed(int speed);

	public native static int prn_getTemp();

	public native static int prn_setupPage(int width, int height);

	public native static int prn_clearPage();

	public native static int prn_printPage(int rotate); // 0 no rotate; 1 roate
														// 90

	public native static int prn_drawLine(int x0, int y0, int x1, int y1,
			int lineWidth);

	public native static int prn_drawText(String data, int x, int y,
			String fontname, int fontsize, boolean bold, boolean italic,
			int rotate);
	public native static int prn_drawTextEx(String data, int x, int y, int width, int height,
			String fontname, int fontsize, int rotate, int style, int format);

	public native static int prn_drawBarcode(String data, int x, int y,
			int barcodetype, int linewidth, int height, int rotate);


	public native static int prn_drawBitmap(long nativeBitmap, int xDest, int yDest,
			int widthDest, int heightDest);
    public native static int prn_drawBitmapEx(byte[] pBitmap, int xDest, int yDest, 
            int widthDest, int heightDest);
    public native static int prn_drawBitmapObject(Bitmap bitmap, int xDest, int yDest);
    public native static int prn_printRGB8888Pixels(int[] bitmapPixels, int width, int height, int xDest, int yDest);
	public native static int prn_getStatus();
    public native static int prn_hueCalibration(int value);
    public native static void prn_setHueType(int type); //0: user default value ;  1: use default value+ APP setting value
    public native static int prn_getHueStatus();
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          