package com.ubx.factorykit.Print;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.device.PrinterManager;
import android.os.Build;
import android.os.Bundle;

public class PrintBillService extends IntentService {
    private final static String STR_PRNT_BILL = "prn_bill";
    private static int fontSize = 24;
    private static int fontStyle = 0x0000;
    private static String fontName = "simsun";
    private PrinterManager printer;
    
    public PrintBillService() {
        super("bill");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        printer = new PrinterManager();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    @TargetApi(12)
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        String context = intent.getStringExtra("SPRT");
        int ret;
        int height = 50;
        if(context== null || context.equals("")) return ;

        if(context.equals(STR_PRNT_BILL)){ // print bill
           printer.prn_setupPage(384,1148);
     //   printer.prn_drawLine(0,0,384,0,2);
          height += printer.prn_drawText(("  打印机测试"), 5, height, ("宋体"), 48 , false, false, 0);
          height += printer.prn_drawText(("商户名(MERCHANT NAME):"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("  面点王（科技园店）"), 0, height, ("宋体"), 24 , false, false, 0);

          height += printer.prn_drawText(("商户号(MERCHANT NO):"), 0, height, ("宋体"), 24 , false, false, 0);

          height += printer.prn_drawText(("  104440358143001"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("终端号(TERMINAL NO):"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("  26605406"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("卡号(CARD NO):"), 0, height, ("宋体"), 24 , false, false, 0);

          height += printer.prn_drawText(("  1234 56** ****0789"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("收单行号:01045840"), 0, height, ("宋体"), 24 ,false, false, 0);
          height += printer.prn_drawText(("发卡行名:渤海银行"), 0, height, ("宋体"), 24 , false, false, 0);

          height += 30;
       /*   printer.prn_drawLine(0,380,384,380,500);*/
          printer.prn_drawLine(32,height,352,height,8);
          height += 6;
          printer.prn_drawLine(32,height,352,height,8);
          height += 6;
          printer.prn_drawLine(32,height,352,height,8);
          height += 8;
          printer.prn_drawLine(32,height,352,height,8);
          height += 6;
          printer.prn_drawLine(32,height,352,height,32);
          
          height += 30;
          height += printer.prn_drawText(("24ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, height, ("宋体"), 32 , false, false, 0);

          height += printer.prn_drawText(("32abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("48ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, height, ("宋体"), 16 , false, false, 0);
          height += printer.prn_drawText(("abcdefghlijkmnopqxyztrswefghlijkmn"), 0, height, ("宋体"), 32 ,false, false, 0);
          height += printer.prn_drawText(("囎囏囐囑囒囓囔囕囖墼墽墾"), 0, height, ("宋体"), 36 ,false, false, 0);

          height += printer.prn_drawText(("囎囏囐囑囒囓囔囕囖墼墽墾孽孾幭幮幯幰幱欆欇欈欉"), 0, height, ("宋体"), 24 , false, false, 0);
      
          height += printer.prn_drawText(("HHHHHHHHHHHHHHHHHHHHHHHH"), 0, height, ("宋体"), 36 , false, false, 0);
          height += printer.prn_drawText(("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"), 0, height, ("宋体"), 24 , false, false, 0);
            
          height += printer.prn_drawText(("☆★○●▲△▼☆★○●▲"), 0, height, "宋体", 36 , false, false, 0);
      

          height += printer.prn_drawText(("ぱばびぶづぢだざじずぜぞ"), 0, height, ("宋体"), 36 , false, false, 0);
      
          height += printer.prn_drawText(("㊣㈱卍▁▂▃▌▍▎▏※※"), 0, height, ("宋体"), 36 , false, false, 0);
      

         // printer.prn_drawBarcode("12345678ABCDEF",0,826,20,2,2,0);
          printer.prn_drawBarcode("12345678ABCDEF", 0, height, 20, 2, 70, 0);
          
          height += 70;
       //   public int prn_drawBarcode(String data, int x, int y, int barcodetype,int width, int height, int rotate) {
          printer.prn_drawBarcode("12345678ABCDEF", 320, height, 20, 2, 50, 3); 
          
		  height += 30;
		  height += printer.prn_drawText(("十六号宋体每一行支持有二十四个字且与加粗与否无关"), 0, height, ("宋体"), 16 , false, false, 0);
          height += printer.prn_drawText(("二十四号宋体每一行支持有十六个字"), 0, height, ("宋体"), 24 , false, false, 0);
          height += printer.prn_drawText(("三十二号一行可有十二个字"), 0, height, ("宋体"), 32 , false, false, 0);
        }else{
            printer.prn_setupPage(384, -1);

            // add by tao.he, for custom print	
            Bundle fontInfo = intent.getBundleExtra("font-info");
            android.util.Log.v("tao.he", fontInfo.toString());

           if (fontInfo != null) {
            fontSize = fontInfo.getInt("font-size", 24);
            fontStyle = fontInfo.getInt("font-style", 0);
            fontName = fontInfo.getString("font-name", "simsun");
           } else {
            fontSize = 24;
            fontStyle = 0;
            fontName = "simsun";
           }

//            ret =printer.prn_drawTextEx(context, 5, 0,300,-1, "arial", 50, 0, 0x0002 | 0x0004, 0);
//            ret = printer.prn_drawText(context, 0, -1, ("arial"), 24 ,false, false, 0);
           ret = printer.prn_drawTextEx(context, 5, 0, 384, -1, fontName, fontSize, 0, fontStyle, 0);
           // end add
//            ret +=printer.prn_drawTextEx(context, 300, ret,-1,-1, "arial", 25, 1, 0, 0);
           android.util.Log.i("debug", "ret:" + ret);
        }
        
  //    printer.prn_drawBarcode(text, 196, 300, 20, 2, 70, 2);
     // printer.prn_drawLine(0,999,384,999,1);
    //  printer.prn_drawLine(0,1022,180,1022,2);
        ret=printer.prn_printPage(0);
        	printer.prn_paperForWard((120));
        Intent i = new Intent("urovo.prnt.message");
        i.putExtra("ret", ret);
        this.sendBroadcast(i);
    }
    
    private void sleep(){
        //延时1秒
        try {
            Thread.currentThread();
			Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
