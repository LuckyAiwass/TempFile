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
 */
package android.device;

import android.util.Log;
import android.graphics.*;
import java.nio.ByteBuffer;

/**
 *The android.device.PriterManager provides support for printer
 *
 */
public class PrinterManager {
	private static final String TAG = "PrinterManager";

    /**
     * Opens the printer
     * 
     * @return Returns 0 if open successful. Returns -1 if failed
     */
    public int open() {
        // qcom platfrom do nothing, compatible to samsung platfrom
        return 0;
    }

    /**
     * Close the printer
     */
    public int close() {
        // qcom platfromdo nothing,compatible to samsung platfrom
        return 0;
    }

    /**
     * Set print gray level
     * 
     * @param level value is 0 to 30, default 15.
     */
    public void setGrayLevel(int level) {
        SprinterNative.prn_setHue(level);
    }

    /**
     * Paper feed
     * 
     * @param len value is -100 to 100, 1= 0.1cm
     */
    public void paperFeed(int len) {
        if (len > 0) {
            SprinterNative.prn_paperForWard(len);
        } else {
            SprinterNative.prn_paperBack(-len);
        }
    }

    /**
     * Set print speed level
     * 
     * @param level value is 50 to 80,default 62
     */
    public void setSpeedLevel(int level) {
        SprinterNative.prn_setSpeed(level);
    }

    /**
     * Set the page size. Unit is in pixel. 8 pixels is equivalent to 1 mm.
     * 
     * @param width Page width, -1 means largest possible width (width = 384)。
     * @param height Page height. -1 means printer driver to manage the page
     *            height.
     * @return 0 when success, and -1 when failed.
     */
    public int setupPage(int width, int height) {
        return SprinterNative.prn_setupPage(width, height);
    }

    /**
     * Clear the current page.
     * 
     * @return Returns 0 if successful. Returns -1 if failed.
     */
    public int clearPage() {
        return SprinterNative.prn_clearPage();
    }

    /**
     * Print the current page.
     * 
     * @param rotate The rotation angle, currently supports only 0
     *            (non-rotating)
     * @return Returns 0 if success. Returns -1 if failed.
     */
    public int printPage(int rotate) { // 0 no rotate; 1 roate 90
        return SprinterNative.prn_printPage(rotate);
    }

	/**
	 * Draw a line in the current page.<br>
	 * (0,0) point axis: On the upper left corner of the screen
	 * 
	 * @param x0 start point X axis, 
	 * @param y0 start point Y axis 
	 * @param x1 end point X axis,
	 * @param y1 end point Y axis 
	 * @param lineWidth in pixel.
	 * @return Returns 0 if successful.  Returns -1 if failed.
	 */
    public int drawLine(int x0, int y0, int x1, int y1, int lineWidth) {
        return SprinterNative.prn_drawLine(x0, y0, x1, y1, lineWidth);
    }

	/**Draw text on the current page
	 * 
	 * @param data The string to be draw
	 * @param x Start point X axis,
	 * @param y Start point Y axis 
	 * @param fontname Font to be used, otherwise, default system font is used.  Custom fonts can be specified, for example, specifying the full path /mnt/sdcard/xxx.ttf.
	 * @param fontsize The font size, in pixel
	 * @param bold The font bold style
	 * @param italic The font italic style
	 * @param rotate The text direction.  0 no rotation, 1 rotate 90 degree, 2 rotate 180 degree, 3 rotate 270 degree.
	 * @return If successful, returns actual printing height.  Returns -1 when failed.
	 */
    public int drawText(String data, int x, int y, String fontname,
            int fontsize, boolean bold, boolean italic, int rotate) {
        return SprinterNative.prn_drawText(data, x, y, fontname, fontsize,
                bold, italic, rotate);
    }
	/**
	 *  
	 * @param data The string to be draw 
	 * @param x Start point X axis,
     * @param y Start point Y axis 
	 * @param width Text is printed to the width of the rectangle on the page
	 * @param height Text is printed to the height of the rectangle on the page
	 * @param fontname  font to be use, otherwise, default system font is used. Or custom fonts i.e. /mnt/sdcard/xxx.ttf the path.
	 * @param fontsize the font size, in pixel
	 * @param rotate print the text degree,  0 no rotation, 1 rotate 90 degree, 2 rotate 180 degree, 3 rotate 270 degree.
	 * @param style Font style (0x0001 - underline, 0x0002  - italic, 0x0004 - bold 0x0008 reverse effect, 0x0010 - strike out), you can mix the style by using the or operator,  style= 0x0002|0x0004
	 * @param format Set to 0 means word wrap at the specified width range 0-384,  Set to 1 means no word wrap
	 * @return Returns actual printing height if successful.  Returns -1 if failed.
	 */
    public int drawTextEx(String data, int x, int y, int width, int height, String fontname,
            int fontsize, int rotate, int style, int format) {
        return SprinterNative.prn_drawTextEx(data, x, y, width, height, fontname, fontsize,
                rotate, style, format);
    }

	/**
	 * 
	 * @param data The barcode text
	 * @param x Start point at X axis,
     * @param y Start point at Y axis 
	 * @param barcodetype Following Table shows the supported symbology 
	 * <table border=2>
	 * <tr><td>BARCODE_CODE11<td>1</td><tr>
	 * <tr><td>BARCODE_C25MATRIX</td><td>2</td><tr>
	 * <tr><td>BARCODE_C25INTER</td><td>3</td><tr>
	 * <tr><td>BARCODE_C25IATA</td><td>4</td></tr>
	 * <tr><td>BARCODE_C25LOGIC</td><td>6<td></tr> 
	 * <tr><td>BARCODE_C25IND</td><td>7</td></tr>
	 * <tr><td>BARCODE_CODE39</td><td>8</td></tr>
	 * <tr><td>BARCODE_EXCODE39</td><td>9</td></tr>
	 * <tr><td>BARCODE_EANX</td><td>13</td></tr> 
	 * <tr><td>BARCODE_EAN128</td><td>16</td></tr>
	 * <tr><td>BARCODE_CODABAR</td><td>18</td></tr>
	 * <tr><td>BARCODE_CODE128</td><td>20</td></tr>
	 * <tr><td>BARCODE_DPLEIT</td><td>21</td></tr>
	 * <tr><td>BARCODE_DPIDENT</td><td>22</td></tr> 
	 * <tr><td>BARCODE_CODE16K</td><td>23</td><tr>
	 * <tr><td>BARCODE_CODE49</td><td>24</td></tr>
	 * <tr><td>BARCODE_CODE93</td><td>25</td></tr>
	 * <tr><td>BARCODE_FLAT</td><td>28</td></tr>
	 * <tr><td>BARCODE_RSS14</td><td>29</td></tr>
	 * <tr><td>BARCODE_RSS_LTD</td><td>30</td></tr>
	 * <tr><td>BARCODE_RSS_EXP</td><td>31</td></tr>
	 * <tr><td>BARCODE_TELEPEN</td><td>32</td></tr>
	 * <tr><td>BARCODE_UPCA</td><td>34</td></tr>
	 * <tr><td>BARCODE_UPCE</td><td>37</td></tr>
	 * <tr><td>BARCODE_POSTNET</td><td>40</td></tr>
	 * <tr><td>BARCODE_MSI_PLESSEY</td><td>47</td></tr>
	 * <tr><td>BARCODE_FIM</td><td>49</td></tr>
	 * <tr><td>BARCODE_LOGMARS</td><td>50</td></tr> 
	 * <tr><td>BARCODE_PHARMA</td><td>51</td></tr>
	 * <tr><td>BARCODE_PZN</td><td>52</td><tr>
	 * <tr><td>BARCODE_PHARMA_TWO</td><td>53</td></tr>
	 * <tr><td>BARCODE_PDF417</td><td>55</td></tr>
	 * <tr><td>BARCODE_PDF417TRUNC</td><td>56</td></tr>
	 * <tr><td>BARCODE_MAXICODE</td><td>57</td></tr>
	 * <tr><td>BARCODE_QRCODE</td><td>58</td></tr> 
	 * <tr><td>BARCODE_CODE128B</td><td>60</td></tr>
	 * <tr><td>BARCODE_AUSPOST</td><td>63</td></tr>
	 * <tr><td>BARCODE_AUSREPLY</td><td>66</td></tr>
	 * <tr><td>BARCODE_AUSROUTE</td><td>67</td><tr>
	 * <tr><td>BARCODE_AUSREDIRECT</td><td>68</td></tr>
	 * <tr><td>BARCODE_ISBNX</td><td>69</td></tr>
	 * <tr><td>BARCODE_RM4SCC</td><td>70</td></tr> 
	 * <tr><td> BARCODE_DATAMATRIX</td><td>71</td></tr>
	 * <tr><td>BARCODE_EAN14</td><td>72</td></tr>
	 * <tr><td>BARCODE_CODABLOCKF</td><td>74</td></tr>
	 * <tr><td>BARCODE_NVE18</td><td>75</td></tr>
	 * <tr><td>BARCODE_JAPANPOST</td><td>76</td></tr>
	 * <tr><td>BARCODE_KOREAPOST</td><td>77</td></tr> 
	 * <tr><td>BARCODE_RSS14STACK</td><td>79</td></tr> 
	 * <tr><td>BARCODE_RSS14STACK_OMNI</td><td>80</td></tr>
	 * <tr><td>BARCODE_RSS_EXPSTACK</td><td>81</td></tr>
	 * <tr><td>BARCODE_PLANET</td><td>82</td></tr>
	 * <tr><td>BARCODE_MICROPDF417</td><td>84</td></tr>
	 * <tr><td>BARCODE_ONECODE</td><td>85</td></tr>
	 * <tr><td>BARCODE_PLESSEY</td><td>86</td></tr>
	 * <tr><td>BARCODE_AZTEC</td><td>92</td></tr>
     * </table>
	 * @param width There are four thickness level to the lines, 1 being the thinnest and 4 being the thickest.
	 * @param height The barcode height in pixel
	 * @param rotate The barcode rotation,  0 no rotation, 1 rotate 90 degree, 2 rotate 180 degree, 3 rotate 270 degree.
	 * @return Returns actual printing height if successful.  Returns -1 when failed.
	 */
    public int drawBarcode(String data, int x, int y, int barcodetype,
            int width, int height, int rotate) {
        if(data == null || data.equals("")) return 0;
        int ret = SprinterNative.prn_drawBarcode( data,  x,  y,  barcodetype,  width,  height,  rotate);
        
        return ret;
    }

	/**
	 * Draw a bitmap on the current page
	 * 
	 * @param bmp The bitmap to be drawn
	 * @param xDest Start point at X axis,
	 * @param yDest Start point at Y axis,
	 * @return Returns actual printing height is successful.  Returns -1 if failed.
	 */
    public int drawBitmap(Bitmap bmp, int xDest, int yDest) {
        if(bmp == null) return -1;
        int width = bmp.getWidth(); 
        int height = bmp.getHeight();
		if((width + xDest) >= 384){ //TODO -
			if(width >= 384){
				xDest = 0;
			}else{
				xDest = ((384 - width) >= 8) ? ((384 - width) - 8) : (384 - width);
			}
		}
        if(bmp.getConfig() == Bitmap.Config.ARGB_8888) {
            int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
            bmp.getPixels(pixels, 0, width, 0, 0, width, height);
            if(pixels == null) return -1;
            return SprinterNative.prn_printRGB8888Pixels(pixels, width, height, xDest, yDest);
        } else if(bmp.getConfig() == Bitmap.Config.RGB_565) {
            int[] pixels = RGB565ToRGB8888Pixels(bmp);
            if(pixels == null) return -1;
            return SprinterNative.prn_printRGB8888Pixels(pixels, width, height, xDest, yDest);
        } else {
            return -1;
        }
	    //Log.d(TAG,"printerdrawBitmap    width ="+width+" height	= "+height);
        //byte[] bitmapByteArray = ConvertBitmapToByteArray(bmp);
        //return SprinterNative.prn_drawBitmap(bmp.ni(), xDest, yDest, width, height);
        //return SprinterNative.prn_drawBitmapObject(bmp, xDest, yDest);


 //byte[] bitmapByteArray = generateBitmapArrayGSV_MSB(bmp, xDest, 0);
    //int lines = (bitmapByteArray.length) / WIDTH;
    //return SprinterNative.prn_drawBitmapEx(bitmapByteArray, 0, yDest, BIT_WIDTH,
        //    lines);
    }

    public int drawBitmapEx(byte[] pbmp, int xDest, int yDest, int widthDest, int heightDest) {
        if(pbmp == null) return -1;
        //byte[] bitmapByteArray = ConvertBitmapToByteArray(bmp);
        return SprinterNative.prn_drawBitmapEx(pbmp, xDest, yDest, widthDest,
                heightDest);
    }

    /**
     * Returns the printer status. 0 for status OK. -1 for out of paper. -2 for
     * overheated
     */
    public int getStatus() {
        return SprinterNative.prn_getStatus();
    }

    public void setFactoryTest(int onoff) {
        SprinterNative.prn_setFactoryTest(onoff);
    }

    public int prn_open() {
        //qcom platfrom do nothing, compatible to samsung platfrom
          return 0;
      }

      public void prn_close() {
          //qcom platfromdo nothing,compatible to samsung platfrom
      }
      public int prn_setBlack(int level) {
          setGrayLevel(level);
          return 0;
      }
      public int prn_setFactoryTest(int onoff){
          setFactoryTest(onoff);
          return 0;
      }
      public void prn_paperForWard(int len) {
           paperFeed(len);
      }
      public void prn_paperBack(int len) {
          paperFeed(-len);
      }
      public int prn_setSpeed(int level){
          setSpeedLevel(level);
          return 0;
      }

      /**
       *@hide 
       */
      public int prn_getTemp() {
          return getTemp();
      }

      public int prn_setupPage(int width, int height) {
          return setupPage(width, height);
      }

      public int prn_clearPage() {
          return clearPage();
      }

      public int prn_printPage(int rotate) { // 0 no rotate; 1 roate 90
          return printPage(rotate);
      }

      public int prn_drawLine(int x0, int y0, int x1, int y1, int lineWidth) {
          return drawLine(x0, y0, x1, y1, lineWidth);
      }

      public int prn_drawText(String data, int x, int y, String fontname,
              int fontsize, boolean bold, boolean italic, int rotate) {
          return drawText(data, x, y, fontname, fontsize, bold, italic, rotate);
      }
      
      public int prn_drawTextEx(String data, int x, int y, int width, int height, String fontname,
              int fontsize, int rotate, int style, int format) {
          return drawTextEx(data, x, y, width, height, fontname, fontsize,
                  rotate, style, format);
      }


      public int prn_drawBarcode(String data, int x, int y, int barcodetype,
              int width, int height, int rotate) {
          return drawBarcode(data, x, y, barcodetype, width, height, rotate);
      }



      public int prn_drawBitmap(Bitmap bmp, int xDest, int yDest) {
          
          return drawBitmap(bmp, xDest, yDest);
      }

      public int prn_drawBitmapEx(byte[] pbmp, int xDest, int yDest, int widthDest, int heightDest) {
          
          return drawBitmapEx(pbmp, xDest, yDest, widthDest, heightDest);
      }
        public static int[] RGB565ToRGB8888Pixels(Bitmap src) {
            int width = src.getWidth();         // 宽
            int height = src.getHeight();       // 高
            int count = src.getByteCount();     // 获取图片的RGB 565颜色数组总数
            ByteBuffer buffer = ByteBuffer.allocate(count);
            src.copyPixelsToBuffer(buffer);
            byte[] data = buffer.array();       //获取数组
            int sum = width * height;
            int[] pixels = new int[sum];

            for (int i = 0; i < sum; i++) {
                int tmpint = data[i * 2 + 0] + data[i * 2 + 1] * 256;
                int a = 0xff;                           //透明度
                int r = (tmpint & 0xf800) >> 11;        //红
                int g = (tmpint & 0x07e0) >> 5;         //绿
                int b = (tmpint & 0x001f);              //蓝

                r = r << 3;
                g = g << 2;
                b = b << 3;
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | (b);
            }
            return pixels;
        }
    /**
     * generate the MSB buffer for bitmap printing GSV command
     *
     * @param bm            the android's Bitmap data
     * @param bitMarginLeft the left white space in bits.
     * @param bitMarginTop  the top white space in bits.
     * @return buffer with DC2V_HEAD + image length
     */
    private int WIDTH = 48;
    private int BIT_WIDTH = 384;
    private byte[] generateBitmapArrayGSV_MSB(Bitmap bm,
                                                     int bitMarginLeft, int bitMarginTop) {
        byte[] result = null;
        int n = bm.getHeight() + bitMarginTop;
        int offset = 0;
        result = new byte[n * WIDTH + offset];
        for (int y = 0; y < bm.getHeight(); y++) {
            for (int x = 0; x < bm.getWidth(); x++) {
                if (x + bitMarginLeft < BIT_WIDTH) {
                    int color = bm.getPixel(x, y);
                    int alpha = Color.alpha(color);
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);
                    if (red < 128
                            || green < 128
                            || blue < 128
                            || (red * 0.2999 + green * 0.587 + blue * 0.114) < 128) {
                        int bitX = bitMarginLeft + x;
                        int byteX = bitX / 8;
                        int byteY = y + bitMarginTop;
                        result[offset + byteY * WIDTH + byteX] |= (0x80 >> (bitX - byteX * 8));
                    }
                } else {
                    break;
                }
            }
        }
        return result;
    }
      private byte[] ConvertBitmapToByteArray(Bitmap img){
           int width = img.getWidth(); 
           int height = img.getHeight();  
           Log.d(TAG,"width ="+width+" height  = "+height);
           int len =(( (width+7)>>3)<<3)*height/8;
           byte[] pixFinal = new byte[len];
           byte []pixeTag = new byte[width*height];   
           int []pixels = new int[width * height];    
           img.getPixels(pixels, 0, width, 0, 0, width, height);
           for(int i = 0; i < height*width; i++) {
             int grey = pixels[i];      
             int red = ((grey  & 0x00FF0000 ) >> 16);
             int green = ((grey & 0x0000FF00) >> 8);
             int blue = (grey & 0x000000FF);
             
             grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
             if(grey>128){
                 pixeTag[i] = 0x00;
             }           
             else{

                 pixeTag[i] = 0x01;
             }
            
           }
          for(int y = 0;y < height;y++){
           for(int i = 0; i<width;i ++){
             int j = i*8;
             if(j+7 < width ){
             pixFinal[y*(( (width+7)>>3)<<3)/8+i] =(byte) ((pixeTag[y*width+j]<<7)|
              (pixeTag[y*width+j+1]<<6)|
              (pixeTag[y*width+j+2]<<5)|
              (pixeTag[y*width+j+3]<<4)|
              (pixeTag[y*width+j+4]<<3)|
              (pixeTag[y*width+j+5]<<2)|
              (pixeTag[y*width+j+6]<<1)|
              (pixeTag[y*width+j+7]));
             }else if (j<width ){

                   for(int k = 0; k<width -j; k++){
                         pixFinal[y*(( (width+7)>>3)<<3)/8+i] |= (pixeTag[y*width+j+k]<<7-k);
               }
             }else break;
               }
          }
           return pixFinal;
      }

      public int prn_getStatus() {
          return getStatus();
      }
      public int getTemp() {
          return SprinterNative.prn_getTemp();
      }
}
