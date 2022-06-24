package com.android.server.scanner;

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
 * @Date: 20-3-13上午11:21
 */
import android.text.TextUtils;
import android.util.SparseArray;

import android.device.scanner.configuration.Symbology;
public final class OEMSymbologyId {
    static {
        loadHoneyWellTable();
        loadZebraTable();
        loadCommonTable();
    }
    //-----------------------------------------------------------------------------
    //  Hand Held Products Symbology ID characters
    //-----------------------------------------------------------------------------
    public static final int HSM_SYMID_AZTEC = 'z';
    public static final int HSM_SYMID_CODABAR = 'a';
    public static final int HSM_SYMID_CODE11 = 'h';
    public static final int HSM_SYMID_CODE128 = 'j';
    public static final int HSM_SYMID_EAN128 = 'I';
    public static final int HSM_SYMID_CODE39 = 'b';
    public static final int HSM_SYMID_CODE49 = 'l';
    public static final int HSM_SYMID_CODE93 = 'i';
    public static final int HSM_SYMID_COMPOSITE = 'y';
    public static final int HSM_SYMID_DATAMATRIX = 'w';
    public static final int HSM_SYMID_EAN8 = 'D';
    public static final int HSM_SYMID_EAN13 = 'd';
    public static final int HSM_SYMID_INT25 = 'e';
    public static final int HSM_SYMID_MAXICODE = 'x';
    public static final int HSM_SYMID_MICROPDF = 'R';
    public static final int HSM_SYMID_PDF417 = 'r';
    public static final int HSM_SYMID_POSTNET = 'P';
    public static final int HSM_SYMID_OCR = 'O';
    public static final int HSM_SYMID_QR = 's';
    public static final int HSM_SYMID_RSS = 'y';
    public static final int HSM_SYMID_UPCA = 'c';
    public static final int HSM_SYMID_UPCE = 'E';
    public static final int HSM_SYMID_ISBT = 'j';
    public static final int HSM_SYMID_BPO = 'B';
    public static final int HSM_SYMID_CANPOST = 'C';
    public static final int HSM_SYMID_AUSPOST = 'A';
    public static final int HSM_SYMID_IATA25 = 'f';
    public static final int HSM_SYMID_CODABLOCK = 'q';
    public static final int HSM_SYMID_JAPOST = 'J';
    public static final int HSM_SYMID_PLANET = 'L';
    public static final int HSM_SYMID_DUTCHPOST = 'K';
    public static final int HSM_SYMID_MSI = 'g';
    public static final int HSM_SYMID_TLC39 = 'T';
    public static final int HSM_SYMID_TRIOPTIC = '=';
    public static final int HSM_SYMID_CODE32 = '<';
    public static final int HSM_SYMID_STRT25 = 'f';
    public static final int HSM_SYMID_MATRIX25 = 'm';
    public static final int HSM_SYMID_PLESSEY = 'n';
    public static final int HSM_SYMID_CHINAPOST = 'Q';
    public static final int HSM_SYMID_KOREAPOST = '?';
    public static final int HSM_SYMID_TELEPEN = 't';
    public static final int HSM_SYMID_CODE16K = 'o';
    public static final int HSM_SYMID_POSICODE = 'W';
    public static final int HSM_SYMID_COUPONCODE = 'c';
    public static final int HSM_SYMID_USPS4CB = 'M';
    public static final int HSM_SYMID_IDTAG = 'N';
    public static final int HSM_SYMID_LABELIV = '>';
    public static final int HSM_SYMID_LABELV = ',';
    public static final int HSM_SYMID_GS1_128 = 'I';
    public static final int HSM_SYMID_HANXIN = 'H';
    public static final int HSM_SYMID_GRIDMATRIX = 'x';
    public static final int HSM_SYMID_DOTCODE	= '.';
    public static final int HSM_SYMID_GS1_DATABAR = 'y';
    public static final int HSM_SYMID_RSS_EXP	=		')';
    public static final int HSM_SYMID_RSS_14		=	'+';
    public static final int HSM_SYMID_RSS_14_LIM	=	'-';
    public static final int HSM_SYMID_UPCE1		=	'*';
    public static final int HSM_SYMID_MicroQR    =   '&';
    public static final int HSM_SYMID_ISBT_128	=	'%';
    public static final int HSM_SYMID_COMPOSITE_A =	'^';
    public static final int HSM_SYMID_COMPOSITE_B =	'$';
    public static final int HSM_SYMID_COMPOSITE_C =	'#';

    public static int getHSMSymbologyId(int type) {
        switch (type) {
            case HSM_SYMID_AZTEC:
                return Symbology.AZTEC.toInt();
            case HSM_SYMID_DOTCODE:
                return Symbology.DOTCODE.toInt();
            case HSM_SYMID_CODABAR:
                return Symbology.CODABAR.toInt();
            case HSM_SYMID_CODE11:
                return Symbology.CODE11.toInt();
            case HSM_SYMID_ISBT_128:
                return Symbology.ISBT128.toInt();
            case HSM_SYMID_CODE128:
                return Symbology.CODE128.toInt();
            case HSM_SYMID_GS1_128:
                return Symbology.GS1_128.toInt();
            case HSM_SYMID_CODE39:
                return Symbology.CODE39.toInt();
            //case HSM_SYMID_CODE49:return Symbology.CODABAR.toInt();
            case HSM_SYMID_CODE93:
                return Symbology.CODE93.toInt();
            case HSM_SYMID_DATAMATRIX:
                return Symbology.DATAMATRIX.toInt();
            case HSM_SYMID_EAN8:
                return Symbology.EAN8.toInt();
            case HSM_SYMID_EAN13:
                return Symbology.EAN13.toInt();
            case HSM_SYMID_INT25:
                return Symbology.INTERLEAVED25.toInt();
            /*case HSM_SYMID_GRIDMATRIX  :
                return Symbology.GRIDMATRIX.toInt();*/
            case HSM_SYMID_MAXICODE:
                return Symbology.MAXICODE.toInt();
            case HSM_SYMID_MICROPDF:
                return Symbology.MICROPDF417.toInt();
            case HSM_SYMID_PDF417:
                return Symbology.PDF417.toInt();
            case HSM_SYMID_POSTNET:
                return Symbology.POSTAL_POSTNET.toInt();
            case HSM_SYMID_QR:
                return Symbology.QRCODE.toInt();
            case HSM_SYMID_MicroQR:
                return Symbology.MICROQR.toInt();
            case HSM_SYMID_COMPOSITE_A:
                return Symbology.COMPOSITE_CC_AB.toInt();
            case HSM_SYMID_COMPOSITE_B:
                return Symbology.COMPOSITE_CC_B.toInt();
            case HSM_SYMID_COMPOSITE_C:
                return Symbology.COMPOSITE_CC_C.toInt();
            //case HSM_SYMID_COMPOSITE:
            //case HSM_SYMID_RSS:
            case HSM_SYMID_RSS_14:
                return Symbology.GS1_14.toInt();
            case HSM_SYMID_RSS_EXP:
                return Symbology.GS1_EXP.toInt();
            case HSM_SYMID_RSS_14_LIM:
                return Symbology.GS1_LIMIT.toInt();
            //case HSM_SYMID_COUPONCODE  :return Symbology.CODABAR.toInt();
            case HSM_SYMID_UPCA:
                return Symbology.UPCA.toInt();
            case HSM_SYMID_UPCE:
                return Symbology.UPCE.toInt();
            case HSM_SYMID_UPCE1:
                return Symbology.UPCE1.toInt();
            case HSM_SYMID_MSI:
                return Symbology.MSI.toInt();
            case HSM_SYMID_TRIOPTIC:
                return Symbology.TRIOPTIC.toInt();
            case HSM_SYMID_CODE32:
                return Symbology.CODE32.toInt();
            //case HSM_SYMID_IATA25 :return Symbology.CODABAR.toInt();
            case HSM_SYMID_IDTAG:
                return Symbology.DISCRETE25.toInt();
            case HSM_SYMID_STRT25:
                return Symbology.DISCRETE25.toInt();
            case HSM_SYMID_MATRIX25:
                return Symbology.MATRIX25.toInt();
            case HSM_SYMID_CHINAPOST:
                return Symbology.CHINESE25.toInt();
            case HSM_SYMID_HANXIN:
                return Symbology.HANXIN.toInt();
            case HSM_SYMID_KOREAPOST:
                return Symbology.KOREA_POST.toInt();
            case HSM_SYMID_TELEPEN:
                return Symbology.TELEPEN.toInt();
            /*case HSM_SYMID_CODE16K:
                return Symbology.CODABAR.toInt();
            case HSM_SYMID_POSICODE:
                return Symbology.CODABAR.toInt();*/
            case HSM_SYMID_TLC39:
                return Symbology.COMPOSITE_TLC_39.toInt();
            case HSM_SYMID_BPO:
                return Symbology.POSTAL_ROYALMAIL.toInt();
            case HSM_SYMID_CANPOST:
                return Symbology.CANADA_POST.toInt();
            case HSM_SYMID_AUSPOST:
                return Symbology.POSTAL_AUSTRALIAN.toInt();
            case HSM_SYMID_CODABLOCK:
                return Symbology.CODABLOCK_A.toInt();
            case HSM_SYMID_JAPOST:
                return Symbology.POSTAL_JAPAN.toInt();
            case HSM_SYMID_PLANET:
                return Symbology.POSTAL_PLANET.toInt();
            case HSM_SYMID_DUTCHPOST:
                return Symbology.POSTAL_UPUFICS.toInt();
            case HSM_SYMID_USPS4CB:
                return Symbology.POSTAL_4STATE.toInt();
            //case HSM_SYMID_LABELIV  :return Symbology.CODABAR.toInt();
            //case HSM_SYMID_LABELV  :return Symbology.CODABAR.toInt();
            //case HSM_SYMID_PLESSEY :return Symbology.CODABAR.toInt();
            //case HSM_SYMID_OCR:return Symbology.CODABAR.toInt();
        }
        return Symbology.NONE.toInt();
    }
    public static int getZebraSymbologyId(int type) {
        switch (type) {
            case Zebra_Aztec:
            case Zebra_Aztec_Rune:
                return Symbology.AZTEC.toInt();
            case Zebra_Codabar:
                return Symbology.CODABAR.toInt();
            case Zebra_Code11:
                return Symbology.CODE11.toInt();
            case Zebra_Code128:
                return Symbology.CODE128.toInt();
            case Zebra_EAN128:
                return Symbology.GS1_128.toInt();
            case Zebra_ISBT128:
            case Zebra_ISBT128_Con:
                return Symbology.ISBT128.toInt();
            case Zebra_Code39:
            case Zebra_Code39FullASCII:
                return Symbology.CODE39.toInt();
            case Zebra_Code39_Trioptic:
                return Symbology.TRIOPTIC.toInt();
            case Zebra_Code93:
                return Symbology.CODE93.toInt();
            case Zebra_DataMatrix:
                return Symbology.DATAMATRIX.toInt();
            case Zebra_EAN8:
            case Zebra_EAN8_2Supplemental:
            case Zebra_EAN8_5supplemental:
                return Symbology.EAN8.toInt();
            case Zebra_EAN13:
            case Zebra_Bookland:
            case Zebra_ISSN:
            case Zebra_EAN13_2Supplemental:
            case Zebra_EAN13_5supplemental:
                return Symbology.EAN13.toInt();
            case Zebra_Interleaved_25:
                return Symbology.INTERLEAVED25.toInt();
            case Zebra_MaxiCode:
                return Symbology.MAXICODE.toInt();
            case Zebra_MicroPDF:
            case Zebra_MacroPDF:
            case Zebra_Macro_MicroPDF:
                return Symbology.MICROPDF417.toInt();
            case Zebra_PDF417:
                return Symbology.PDF417.toInt();
            case Zebra_QRCode:
                return Symbology.QRCODE.toInt();
            case Zebra_MacroQR:
            case Zebra_MicroQR:
                return Symbology.MICROQR.toInt();
            case Zebra_GS1DataBar14:
            case Zebra_GS1_DatabarCoupon:
                return Symbology.GS1_14.toInt();
            case Zebra_GS1DataBar_Expanded:
                return Symbology.GS1_EXP.toInt();
            case Zebra_GS1DataBar_Limited:
                return Symbology.GS1_LIMIT.toInt();
            case Zebra_UPCA:
            case Zebra_UPCA_2Supplemental:
            case Zebra_UPCA_5supplemental:
                return Symbology.UPCA.toInt();
            case Zebra_UPCE0:
            case Zebra_UPCE0_2Supplemental:
            case Zebra_UPCE0_5supplemental:
                return Symbology.UPCE.toInt();
            case Zebra_UPCE1:
            case Zebra_UPCE1_2Supplemental:
            case Zebra_UPCE1_5supplemental:
                return Symbology.UPCE1.toInt();
            case Zebra_MSI:
                return Symbology.MSI.toInt();
            case Zebra_Code32:
                return Symbology.CODE32.toInt();
            case Zebra_Discrete_Standard_25:
                return Symbology.DISCRETE25.toInt();
            case Zebra_Matrix_25:
                return Symbology.MATRIX25.toInt();
            case Zebra_Chinese_25:
                return Symbology.CHINESE25.toInt();
            case Zebra_HanXin:
                return Symbology.HANXIN.toInt();
            case Zebra_TLC39:
                return Symbology.COMPOSITE_TLC_39.toInt();
            case Zebra_CCA_GS1_DataBarLimited:
            case Zebra_CCA_GS1_DataBarExpanded:
            case Zebra_CCA_GS1DataBar14:
            case Zebra_CCA_EAN8:
            case Zebra_CCA_EAN13:
            case Zebra_CCA_EAN128:
            case Zebra_CCA_UPCA:
            case Zebra_CCA_UPCE:
            case Zebra_MicroPDF_CCA:
                return Symbology.COMPOSITE_CC_AB.toInt();
            case Zebra_CCB_GS1DataBar_Limited:
            case Zebra_CCB_GS1DataBar_Expanded:
            case Zebra_CCB_GS1DataBar14:
            case Zebra_CCB_EAN8:
            case Zebra_CCB_EAN13:
            case Zebra_CCB_EAN128:
            case Zebra_CCB_UPCA:
            case Zebra_CCB_UPCE:
                return Symbology.COMPOSITE_CC_B.toInt();
            case Zebra_CCC_EAN128:
                return Symbology.COMPOSITE_CC_C.toInt();
            case Zebra_DotCode:
                return Symbology.DOTCODE.toInt();
            case Zebra_GridMatrix:
                return Symbology.GRIDMATRIX.toInt();
            case Zebra_IATA:
                return Symbology.NEC25.toInt();
            case Zebra_Code49:
                return Symbology.CODE49.toInt();
            case Zebra_Korean_35:
                return Symbology.KOREA_POST.toInt();
            case Zebra_Planet_Code:
                return Symbology.POSTAL_PLANET.toInt();
            case Zebra_PostNet_US:
                return Symbology.POSTAL_POSTNET.toInt();
            case Zebra_CanadianPostal:
                return Symbology.CANADA_POST.toInt();
            case Zebra_Australian_Postal:
                return Symbology.POSTAL_AUSTRALIAN.toInt();
            case Zebra_Japan_Postal:
                return Symbology.POSTAL_JAPAN.toInt();
            case Zebra_UK_Postal:
                return Symbology.POSTAL_UK.toInt();
            case Zebra_DutchPostal:
                return Symbology.POSTAL_KIX.toInt();
            case Zebra_UPU4State:
                return Symbology.POSTAL_4STATE.toInt();
            case Zebra_USPS4CB:
                //return Symbology.POSTAL_ROYALMAIL.toInt();
                return Symbology.POSTAL_UPUFICS.toInt();
            case Zebra_Code16K:
                return Symbology.GRIDMATRIX.toInt() + 5;
            case Zebra_UPCD:
                return Symbology.GRIDMATRIX.toInt()+ 6;
            case Zebra_Coupon_Code:
                return Symbology.GRIDMATRIX.toInt()+ 7;
            case Zebra_Scanlet:
                return Symbology.GRIDMATRIX.toInt()+ 8;
            case Zebra_Signature_Capture:
                return Symbology.GRIDMATRIX.toInt()+ 9;
            case Zebra_NW7:
                return Symbology.GRIDMATRIX.toInt()+ 10;
            case Zebra_CueCode:
                return Symbology.GRIDMATRIX.toInt()+ 11;
        }
        return Symbology.NONE.toInt();
    }

    /**
     * convert to HSM Symbol Code Identifiers
     * @param type
     * @return
     */
    public static byte getZebraSymbolCodeId(int type) {
        switch (type) {
            case Zebra_Aztec:
            case Zebra_Aztec_Rune:
                return HSM_SYMID_AZTEC;
            case Zebra_Codabar:
                return HSM_SYMID_CODABAR;
            case Zebra_Code11:
                return HSM_SYMID_CODE11;
            case Zebra_Code128:
                return HSM_SYMID_CODE128;
            case Zebra_EAN128:
                return HSM_SYMID_GS1_128;
            case Zebra_ISBT128:
            case Zebra_ISBT128_Con:
                return HSM_SYMID_ISBT_128;
            case Zebra_Code39:
            case Zebra_Code39FullASCII:
                return HSM_SYMID_CODE39;
            case Zebra_Code39_Trioptic:
                return HSM_SYMID_TRIOPTIC;
            case Zebra_Code93:
                return HSM_SYMID_CODE93;
            case Zebra_DataMatrix:
                return HSM_SYMID_DATAMATRIX;
            case Zebra_EAN8:
            case Zebra_EAN8_2Supplemental:
            case Zebra_EAN8_5supplemental:
                return HSM_SYMID_EAN8;
            case Zebra_EAN13:
            case Zebra_Bookland:
            case Zebra_ISSN:
            case Zebra_EAN13_2Supplemental:
            case Zebra_EAN13_5supplemental:
                return HSM_SYMID_EAN13;
            case Zebra_Interleaved_25:
                return HSM_SYMID_INT25;
            case Zebra_MaxiCode:
                return HSM_SYMID_MAXICODE;
            case Zebra_MicroPDF:
            case Zebra_MacroPDF:
            case Zebra_Macro_MicroPDF:
                return HSM_SYMID_MICROPDF;
            case Zebra_PDF417:
                return HSM_SYMID_PDF417;
            case Zebra_QRCode:
                return HSM_SYMID_QR;
            case Zebra_MacroQR:
            case Zebra_MicroQR:
                return HSM_SYMID_MicroQR;
            case Zebra_GS1DataBar14:
            case Zebra_GS1_DatabarCoupon:
                return HSM_SYMID_RSS_14;
            case Zebra_GS1DataBar_Expanded:
                return HSM_SYMID_RSS_EXP;
            case Zebra_GS1DataBar_Limited:
                return HSM_SYMID_RSS_14_LIM;
            case Zebra_UPCA:
            case Zebra_UPCA_2Supplemental:
            case Zebra_UPCA_5supplemental:
                return HSM_SYMID_UPCA;
            case Zebra_UPCE0:
            case Zebra_UPCE0_2Supplemental:
            case Zebra_UPCE0_5supplemental:
                return HSM_SYMID_UPCE;
            case Zebra_UPCE1:
            case Zebra_UPCE1_2Supplemental:
            case Zebra_UPCE1_5supplemental:
                return HSM_SYMID_UPCE1;
            case Zebra_MSI:
                return HSM_SYMID_MSI;
            case Zebra_Code32:
                return HSM_SYMID_CODE32;
            case Zebra_Discrete_Standard_25:
                return HSM_SYMID_STRT25;
            case Zebra_Matrix_25:
                return HSM_SYMID_MATRIX25;
            case Zebra_Chinese_25:
                return HSM_SYMID_CHINAPOST;
            case Zebra_HanXin:
                return HSM_SYMID_HANXIN;
            case Zebra_TLC39:
                return HSM_SYMID_TLC39;
            case Zebra_CCA_GS1_DataBarLimited:
            case Zebra_CCA_GS1_DataBarExpanded:
            case Zebra_CCA_GS1DataBar14:
            case Zebra_CCA_EAN8:
            case Zebra_CCA_EAN13:
            case Zebra_CCA_EAN128:
            case Zebra_CCA_UPCA:
            case Zebra_CCA_UPCE:
            case Zebra_MicroPDF_CCA:
                return HSM_SYMID_COMPOSITE_A;
            case Zebra_CCB_GS1DataBar_Limited:
            case Zebra_CCB_GS1DataBar_Expanded:
            case Zebra_CCB_GS1DataBar14:
            case Zebra_CCB_EAN8:
            case Zebra_CCB_EAN13:
            case Zebra_CCB_EAN128:
            case Zebra_CCB_UPCA:
            case Zebra_CCB_UPCE:
                return HSM_SYMID_COMPOSITE_B;
            case Zebra_CCC_EAN128:
                return HSM_SYMID_COMPOSITE_C;
            case Zebra_DotCode:
                return HSM_SYMID_DOTCODE;
            case Zebra_GridMatrix:
                return HSM_SYMID_GRIDMATRIX;
            case Zebra_IATA:
                return HSM_SYMID_IATA25;
            case Zebra_Code49:
                return HSM_SYMID_CODE49;
            case Zebra_Korean_35:
                return HSM_SYMID_KOREAPOST;
            case Zebra_Planet_Code:
                return HSM_SYMID_PLANET;
            case Zebra_PostNet_US:
                return HSM_SYMID_POSTNET;
            case Zebra_CanadianPostal:
                return HSM_SYMID_CANPOST;
            case Zebra_Australian_Postal:
                return HSM_SYMID_AUSPOST;
            case Zebra_Japan_Postal:
                return HSM_SYMID_JAPOST;
            case Zebra_UK_Postal:
                return HSM_SYMID_POSTNET;
            case Zebra_DutchPostal:
                return HSM_SYMID_DUTCHPOST;
            case Zebra_UPU4State:
                return HSM_SYMID_BPO;
            case Zebra_USPS4CB:
                //return Symbology.POSTAL_ROYALMAIL.toInt();
                return HSM_SYMID_USPS4CB;
            case Zebra_Code16K:
                return HSM_SYMID_CODE16K;
            //case Zebra_UPCD:
            case Zebra_Coupon_Code:
                return HSM_SYMID_COUPONCODE;
            case Zebra_Scanlet:
            case Zebra_Signature_Capture:
            case Zebra_NW7:
            case Zebra_CueCode:
                return HSM_SYMID_POSICODE;
        }
        return HSM_SYMID_POSICODE;
    }
    public static final int Zebra_Code39 = 1;
    public static final int Zebra_Codabar = 2;
    public static final int Zebra_Code128 = 3;
    public static final int Zebra_Discrete_Standard_25 = 4;
    public static final int Zebra_IATA = 5;
    public static final int Zebra_Interleaved_25 = 6;
    public static final int Zebra_Code93 = 7;
    public static final int Zebra_UPCA = 8;
    public static final int Zebra_UPCE0 = 9;
    public static final int Zebra_EAN8 = 10;
    public static final int Zebra_EAN13 = 11;
    public static final int Zebra_Code11 = 12;
    public static final int Zebra_Code49 = 13;
    public static final int Zebra_MSI = 14;
    public static final int Zebra_EAN128 = 15;
    public static final int Zebra_UPCE1 = 16;
    public static final int Zebra_PDF417 = 17;
    public static final int Zebra_Code16K = 18;
    public static final int Zebra_Code39FullASCII = 19;
    public static final int Zebra_UPCD = 20;
    public static final int Zebra_Code39_Trioptic = 21;
    public static final int Zebra_Bookland = 22;
    public static final int Zebra_Coupon_Code = 23;
    public static final int Zebra_NW7 = 24;
    public static final int Zebra_ISBT128 = 25;
    public static final int Zebra_MicroPDF = 26;
    public static final int Zebra_DataMatrix = 27;
    public static final int Zebra_QRCode = 28;
    public static final int Zebra_MicroPDF_CCA = 29;
    public static final int Zebra_PostNet_US = 30;
    public static final int Zebra_Planet_Code = 31;
    public static final int Zebra_Code32 = 32;
    public static final int Zebra_ISBT128_Con = 33;
    public static final int Zebra_Japan_Postal = 34;
    public static final int Zebra_Australian_Postal = 35;
    public static final int Zebra_DutchPostal = 36;
    public static final int Zebra_MaxiCode = 37;
    public static final int Zebra_CanadianPostal = 38;
    public static final int Zebra_UK_Postal = 39;
    public static final int Zebra_MacroPDF = 40;
    public static final int Zebra_MacroQR = 41;
    public static final int Zebra_MicroQR = 44;
    public static final int Zebra_Aztec = 45;
    public static final int Zebra_Aztec_Rune = 46;
    public static final int Zebra_GS1DataBar14 = 48;
    public static final int Zebra_GS1DataBar_Limited = 49;
    public static final int Zebra_GS1DataBar_Expanded = 50;
    public static final int Zebra_USPS4CB = 52;
    public static final int Zebra_UPU4State = 53;
    public static final int Zebra_ISSN = 54;
    public static final int Zebra_Scanlet = 55;
    public static final int Zebra_CueCode = 56;
    public static final int Zebra_Matrix_25 = 57;
    public static final int Zebra_UPCA_2Supplemental = 72;
    public static final int Zebra_UPCE0_2Supplemental = 73;
    public static final int Zebra_EAN8_2Supplemental = 74;
    public static final int Zebra_EAN13_2Supplemental = 75;
    public static final int Zebra_UPCE1_2Supplemental = 80;
    public static final int Zebra_CCA_EAN128 = 81;
    public static final int Zebra_CCA_EAN13 = 82;
    public static final int Zebra_CCA_EAN8 = 83;
    public static final int Zebra_CCA_GS1_DataBarExpanded = 84;
    public static final int Zebra_CCA_GS1_DataBarLimited = 85;
    public static final int Zebra_CCA_GS1DataBar14 = 86;
    public static final int Zebra_CCA_UPCA = 87;
    public static final int Zebra_CCA_UPCE = 88;
    public static final int Zebra_CCC_EAN128 = 89;
    public static final int Zebra_TLC39 = 90;
    public static final int Zebra_CCB_EAN128 = 97;
    public static final int Zebra_CCB_EAN13 = 98;
    public static final int Zebra_CCB_EAN8 = 99;
    public static final int Zebra_CCB_GS1DataBar_Expanded = 100;
    public static final int Zebra_CCB_GS1DataBar_Limited = 101;
    public static final int Zebra_CCB_GS1DataBar14 = 102;
    public static final int Zebra_CCB_UPCA = 103;
    public static final int Zebra_CCB_UPCE = 104;
    public static final int Zebra_Signature_Capture = 105;
    public static final int Zebra_Chinese_25 = 114;
    public static final int Zebra_Korean_35 = 115;
    public static final int Zebra_UPCA_5supplemental = 136;
    public static final int Zebra_UPCE0_5supplemental = 137;
    public static final int Zebra_EAN8_5supplemental = 138;
    public static final int Zebra_EAN13_5supplemental = 139;
    public static final int Zebra_UPCE1_5supplemental = 144;
    public static final int Zebra_Macro_MicroPDF = 154;
    public static final int Zebra_GS1_DatabarCoupon = 180;
    public static final int Zebra_HanXin = 183;
    public static final int Zebra_DotCode = 196;
    public static final int Zebra_GridMatrix = 200;

    private static SparseArray<String> zebraTable;
    private static SparseArray<String> honeyWellTable;
    private static SparseArray<String> commonTable;
    public final static int HoneyWellEngine = 0;
    public final static int ZebraEngine = 1;
    public final static int CommonEngine = 2;
    public static String stringFromSymbologyType(int engineType, int symbologyType) {
        if(engineType == HoneyWellEngine) {
            String value = honeyWellTable.get(symbologyType);
            if(TextUtils.isEmpty(value)) {
                return "Undefined";
            } else {
                return value;
            }
        } else if(engineType == ZebraEngine) {
            String value = zebraTable.get(symbologyType);
            if(TextUtils.isEmpty(value)) {
                return "Undefined";
            } else {
                return value;
            }
        } else if(engineType == CommonEngine){
            String value = commonTable.get(symbologyType);
            if(TextUtils.isEmpty(value)) {
                return "Undefined";
            } else {
                return value;
            }
        } else {
            return "Undefined";
        }
    }
    public static String stringFromHSMSymbology(int symbologyType) {
        String value = honeyWellTable.get(symbologyType);
        if(TextUtils.isEmpty(value)) {
            return "Undefined";
        } else {
            return value;
        }
    }
    public static String stringFromZebraSymbology(int symbologyType) {
        String value = zebraTable.get(symbologyType);
        if(TextUtils.isEmpty(value)) {
            return "Undefined";
        } else {
            return value;
        }
    }
    private static void loadCommonTable() {
        commonTable = new SparseArray<String>();
        commonTable.put(Symbology.CODE39.toInt(), "Code39");
        commonTable.put(Symbology.CODABAR.toInt(), "Codabar");
        commonTable.put(Symbology.CODE128.toInt(), "Code128");
        commonTable.put(Symbology.DISCRETE25.toInt(), "Discrete-2of5");
        //commonTable.put(HSM_SYMID_IATA25, "IATA");
        commonTable.put(Symbology.INTERLEAVED25.toInt(), "Interleaved-2of5");
        commonTable.put(Symbology.CODE93.toInt(), "Code93");
        commonTable.put(Symbology.UPCA.toInt(), "UPC-A");
        commonTable.put(Symbology.UPCE.toInt(), "UPC-E0");
        commonTable.put(Symbology.UPCE1.toInt(), "UPC-E1");
        commonTable.put(Symbology.EAN8.toInt(), "EAN-8");
        commonTable.put(Symbology.EAN13.toInt(), "EAN-13");
        commonTable.put(Symbology.CODE11.toInt(), "Code11");
        commonTable.put(Symbology.CODE49.toInt(), "Code49");
        commonTable.put(Symbology.MSI.toInt(), "MSI");
        commonTable.put(Symbology.GS1_128.toInt(), "GS1-128");
        commonTable.put(Symbology.PDF417.toInt(), "PDF-417");
        //commonTable.put(HSM_SYMID_CODE16K, "Code 16K");
        commonTable.put(Symbology.CODABLOCK_A.toInt(), "Codablock_A");
        commonTable.put(Symbology.CODABLOCK_F.toInt(), "Codablock_F");
        commonTable.put(Symbology.TRIOPTIC.toInt(), "Trioptic39");
        commonTable.put(Symbology.CouponCode.toInt(), "CouponCode");
        commonTable.put(Symbology.TELEPEN.toInt(), "Telepen");
        commonTable.put(Symbology.ISBT128.toInt(), "ISBT-128");
        commonTable.put(Symbology.MICROPDF417.toInt(), "MicroPDF");
        commonTable.put(Symbology.DATAMATRIX.toInt(), "DataMatrix");
        commonTable.put(Symbology.QRCODE.toInt(), "QRCode");
        commonTable.put(Symbology.POSTAL_POSTNET.toInt(), "USPostNet");
        commonTable.put(Symbology.POSTAL_PLANET.toInt(), "USPlanet");
        commonTable.put(Symbology.CODE32.toInt(), "Code32");
        commonTable.put(Symbology.POSTAL_JAPAN.toInt(), "JapanPostal");
        commonTable.put(Symbology.POSTAL_AUSTRALIAN.toInt(), "AustralianPostal");
        commonTable.put(Symbology.POSTAL_KIX.toInt(), "DutchPostal");
        commonTable.put(Symbology.MAXICODE.toInt(), "MaxiCode");
        commonTable.put(Symbology.CANADA_POST.toInt(), "CanadianPostal");
        commonTable.put(Symbology.AZTEC.toInt(), "Aztec");
        commonTable.put(Symbology.GS1_14.toInt(), "GS1-DataBar-14");
        commonTable.put(Symbology.GS1_LIMIT.toInt(), "GS1-DataBar-Limited");
        commonTable.put(Symbology.GS1_EXP.toInt(), "GS1-DataBar-Expanded");
        commonTable.put(Symbology.POSTAL_UPUFICS.toInt(), "USPS4CB");
        commonTable.put(Symbology.POSTAL_4STATE.toInt(), "UPU4State");
        //commonTable.put(HSM_SYMID_POSICODE, "PosiCode");
        commonTable.put(Symbology.MATRIX25.toInt(), "Matrix-2of5");
        commonTable.put(Symbology.COMPOSITE_CC_AB.toInt(), "Composite-AB");
        commonTable.put(Symbology.COMPOSITE_CC_C.toInt(), "Composite-C");
        commonTable.put(Symbology.GRIDMATRIX.toInt(), "Grid Matrix");
        //commonTable.put(HSM_SYMID_LABELV, "LABELV");
        //commonTable.put(HSM_SYMID_LABELIV, "LABELIV");
        commonTable.put(Symbology.COMPOSITE_TLC_39.toInt(), "TLC-39");
        //commonTable.put(HSM_SYMID_PLESSEY, "Plessey");
        commonTable.put(Symbology.CHINESE25.toInt(), "Chinese-2of5");
        commonTable.put(Symbology.KOREA_POST.toInt(), "Korean-3of5");
        commonTable.put(Symbology.NEC25.toInt(), "NEC-2of5");
        commonTable.put(Symbology.HANXIN.toInt(), "HanXin");
        commonTable.put(Symbology.DOTCODE.toInt(), "DotCode");
        //commonTable.put(Symbology.OCR, "OCR");
    }
    private static void loadHoneyWellTable(){
        honeyWellTable = new SparseArray<String>();
        honeyWellTable.put(HSM_SYMID_AZTEC, "Aztec");
        honeyWellTable.put(HSM_SYMID_CODABAR, "Codabar");
        honeyWellTable.put(HSM_SYMID_CODE11, "Code11");
        honeyWellTable.put(HSM_SYMID_CODE128, "Code128");
        //honeyWellTable.put(HSM_SYMID_EAN128, "EAN-128");
        honeyWellTable.put(HSM_SYMID_GS1_128, "GS1-128");
        honeyWellTable.put(HSM_SYMID_ISBT_128, "ISBT-128");
        honeyWellTable.put(HSM_SYMID_CODE39, "Code39");
        honeyWellTable.put(HSM_SYMID_CODE49, "Code49");
        honeyWellTable.put(HSM_SYMID_CODE93, "Code93");
        honeyWellTable.put(HSM_SYMID_COMPOSITE, "Composite-AB-C");//y
        honeyWellTable.put(HSM_SYMID_DATAMATRIX, "DataMatrix");
        honeyWellTable.put(HSM_SYMID_EAN8, "EAN-8");
        honeyWellTable.put(HSM_SYMID_EAN13, "EAN-13");
        honeyWellTable.put(HSM_SYMID_INT25, "Interleaved-2of5");
        honeyWellTable.put(HSM_SYMID_MAXICODE, "MaxiCode");
        honeyWellTable.put(HSM_SYMID_MICROPDF, "MicroPDF");
        honeyWellTable.put(HSM_SYMID_PDF417, "PDF-417");
        honeyWellTable.put(HSM_SYMID_POSTNET, "USPostNet");
        honeyWellTable.put(HSM_SYMID_OCR, "OCR");
        honeyWellTable.put(HSM_SYMID_QR, "QRCode");
        //honeyWellTable.put(HSM_SYMID_RSS, "GS1-DataBar");//y
        honeyWellTable.put(HSM_SYMID_UPCA, "UPC-A");
        honeyWellTable.put(HSM_SYMID_UPCE, "UPC-E0");
        honeyWellTable.put(HSM_SYMID_BPO, "UPU4State");//British post(BPO 4-State)
        honeyWellTable.put(HSM_SYMID_CANPOST, "CanadianPostal");
        honeyWellTable.put(HSM_SYMID_AUSPOST, "AustralianPostal");
        honeyWellTable.put(HSM_SYMID_IATA25, "IATA25");
        honeyWellTable.put(HSM_SYMID_CODABLOCK, "Codablock");
        honeyWellTable.put(HSM_SYMID_JAPOST, "JapanPostal");
        honeyWellTable.put(HSM_SYMID_PLANET, "USPlanet");
        honeyWellTable.put(HSM_SYMID_DUTCHPOST, "DutchPostal");
        honeyWellTable.put(HSM_SYMID_MSI, "MSI");
        honeyWellTable.put(HSM_SYMID_TLC39, "TLC-39");
        honeyWellTable.put(HSM_SYMID_TRIOPTIC, "Trioptic39");
        honeyWellTable.put(HSM_SYMID_CODE32, "Code32");
        honeyWellTable.put(HSM_SYMID_STRT25, "Discrete-2of5");
        honeyWellTable.put(HSM_SYMID_MATRIX25, "Matrix-2of5");
        honeyWellTable.put(HSM_SYMID_PLESSEY, "Plessey");
        honeyWellTable.put(HSM_SYMID_CHINAPOST, "Chinese-2of5");
        honeyWellTable.put(HSM_SYMID_KOREAPOST, "Korean-3of5");
        honeyWellTable.put(HSM_SYMID_TELEPEN, "Telepen");
        honeyWellTable.put(HSM_SYMID_CODE16K, "Code-16K");
        honeyWellTable.put(HSM_SYMID_POSICODE, "PosiCode");
        //honeyWellTable.put(HSM_SYMID_COUPONCODE, "Coupon Code");
        honeyWellTable.put(HSM_SYMID_USPS4CB, "USPS4CB");
        honeyWellTable.put(HSM_SYMID_IDTAG, "Standard-2of5");
        honeyWellTable.put(HSM_SYMID_LABELIV, "LABELIV");
        honeyWellTable.put(HSM_SYMID_LABELV, "LABELV");
        honeyWellTable.put(HSM_SYMID_HANXIN, "HanXin");
        honeyWellTable.put(HSM_SYMID_GRIDMATRIX, "Grid Matrix");
        honeyWellTable.put(HSM_SYMID_DOTCODE, "DotCode");
        honeyWellTable.put(HSM_SYMID_UPCE1, "UPC-E1");
        honeyWellTable.put(HSM_SYMID_MicroQR, "MicroQR");
        honeyWellTable.put(HSM_SYMID_RSS_EXP, "GS1-DataBar-Expanded");
        honeyWellTable.put(HSM_SYMID_RSS_14, "GS1-DataBar-14");//y
        honeyWellTable.put(HSM_SYMID_RSS_14_LIM, "GS1-DataBar-Limited");
        honeyWellTable.put(HSM_SYMID_COMPOSITE_A, "Composite-CC-AB");
        honeyWellTable.put(HSM_SYMID_COMPOSITE_B, "COMPOSITE_CC_B");
        honeyWellTable.put(HSM_SYMID_COMPOSITE_C, "COMPOSITE_CC_C");
    }
    private static void loadZebraTable() {
        zebraTable = new SparseArray<String>();
        zebraTable.put(1, "Code39");
        zebraTable.put(2, "Codabar");
        zebraTable.put(3, "Code128");
        zebraTable.put(4, "Discrete-2of5");
        zebraTable.put(5, "IATA");
        zebraTable.put(6, "Interleaved-2of5");
        zebraTable.put(7, "Code93");
        zebraTable.put(8, "UPC-A");
        zebraTable.put(9, "UPC-E0");
        zebraTable.put(10, "EAN-8");
        zebraTable.put(11, "EAN-13");
        zebraTable.put(12, "Code11");
        zebraTable.put(13, "Code49");
        zebraTable.put(14, "MSI");
        zebraTable.put(15, "EAN-128");
        zebraTable.put(16, "UPC-E1");
        zebraTable.put(17, "PDF-417");
        zebraTable.put(18, "Code16K");
        zebraTable.put(19, "Code39-Full-ASCII");
        zebraTable.put(20, "UPC-D");
        zebraTable.put(21, "Trioptic39");
        zebraTable.put(22, "Bookland");
        zebraTable.put(23, "CouponCode");
        zebraTable.put(24, "NW-7");
        zebraTable.put(25, "ISBT-128");
        zebraTable.put(26, "MicroPDF");
        zebraTable.put(27, "DataMatrix");
        zebraTable.put(28, "QRCode");
        zebraTable.put(29, "MicroPDF-CCA");
        zebraTable.put(30, "USPostNet");
        zebraTable.put(31, "USPlanet");
        zebraTable.put(32, "Code32");
        zebraTable.put(33, "ISBT-128-Con");
        zebraTable.put(34, "JapanPostal");
        zebraTable.put(35, "AustralianPostal");
        zebraTable.put(36, "DutchPostal");
        zebraTable.put(37, "MaxiCode");
        zebraTable.put(38, "CanadianPostal");
        zebraTable.put(39, "UKPostal");
        zebraTable.put(40, "MacroPDF");
        zebraTable.put(41, "MacroQR");
        zebraTable.put(44, "MicroQR");
        zebraTable.put(45, "Aztec");
        zebraTable.put(46, "AztecRune");
        zebraTable.put(48, "GS1-DataBar-14");
        zebraTable.put(49, "GS1-DataBar-Limited");
        zebraTable.put(50, "GS1-DataBar-Expanded");
        zebraTable.put(52, "USPS4CB");
        zebraTable.put(53, "UPU4State");
        zebraTable.put(54, "ISSN");
        zebraTable.put(55, "Scanlet");
        zebraTable.put(56, "CueCode");
        zebraTable.put(57, "Matrix-2of5");
        zebraTable.put(72, "UPC-A+2");
        zebraTable.put(73, "UPC-E0+2");
        zebraTable.put(74, "EAN-8+2");
        zebraTable.put(75, "EAN-13+2");
        zebraTable.put(80, "UPC-E1+2");
        zebraTable.put(81, "CCA-EAN-128");
        zebraTable.put(82, "CCA-EAN-13");
        zebraTable.put(83, "CCA-EAN-8");
        zebraTable.put(84, "CCA-GS1-DataBar-Expanded");
        zebraTable.put(85, "CCA-GS1-DataBar-Limited");
        zebraTable.put(86, "CCA-GS1-DataBar-14");
        zebraTable.put(87, "CCA-UPC-A");
        zebraTable.put(88, "CCA-UPC-E");
        zebraTable.put(89, "CCC-EAN-128");
        zebraTable.put(90, "TLC-39");
        zebraTable.put(97, "CCB-EAN-128");
        zebraTable.put(98, "CCB-EAN-13");
        zebraTable.put(99, "CCB-EAN-8");
        zebraTable.put(100, "CCB-GS1-DataBar-Expanded");
        zebraTable.put(101, "CCB-GS1-DataBar-Limited");
        zebraTable.put(102, "CCB-GS1-DataBar-14");
        zebraTable.put(103, "CCB-UPC-A");
        zebraTable.put(104, "CCB-UPC-E");
        zebraTable.put(105, "Signature-Capture");
        zebraTable.put(114, "Chinese-2of5");
        zebraTable.put(115, "Korean-3of5");
        zebraTable.put(136, "UPC-A+5");
        zebraTable.put(137, "UPC-E0+5");
        zebraTable.put(138, "EAN-8+5");
        zebraTable.put(139, "EAN-13+5");
        zebraTable.put(144, "UPC-E1+5");
        zebraTable.put(154, "Macro-Micro-PDF");
        zebraTable.put(180, "GS1-Databar-Coupon");
        zebraTable.put(183, "HanXin");
        zebraTable.put(196, "DotCode");
        zebraTable.put(200, "GridMatrix");
    }
}
