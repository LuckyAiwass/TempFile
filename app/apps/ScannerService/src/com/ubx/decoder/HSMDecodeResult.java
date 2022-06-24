package com.ubx.decoder;

import android.device.scanner.configuration.Symbology;
import android.util.Log;

/**
 * Created by rocky on 18-4-1.
 * ] = Flag Character (ASCII 93)
 * c = aimCodeLetter
 * m = aimModifier
 */

public class HSMDecodeResult {
    private byte[] barcodeByteData = null;
    private int length = 0;
    private long decodeTime = 0L;
    private int[] bounds = null;
    private int modifier = 0;
    private int modifierEx = 0;
    /**
     * This variable will contain the Honeywell Code ID for the decoded symbology
     */
    private byte codeId = 0;
    /**
     * This variable will contain the AIM ID for the decoded symbology
     */
    private byte aimCodeLetter = 0;
    /**
     * This variable will contain the code modifier for the decoded symbology
     */
    private byte aimModifier = 0;
    private long symbologyId = 0L;
    private long symbologyExId = 0L;

    public HSMDecodeResult() {
    }

    public void setBarcodeDataBytes(byte[] data) {
        this.barcodeByteData = data;
    }

    public byte[] getBarcodeDataBytes() {
        return this.barcodeByteData;
    }

    public int getBarcodeDataLength() {
        return this.length;
    }

    public void setBarcodeDataLength(int size) {
        this.length = size;
    }

    public long getDecodeTime() {
        return this.decodeTime;
    }

    public void setDecodeTime(long time) {
        decodeTime = time;
    }

    public int[] getBarcodeBounds() {
        return this.bounds;
    }

    public void setBarcodeBounds(int[] barcodeBounds) {
        this.bounds = barcodeBounds;
    }

    public byte getCodeId() {
        return this.codeId;
    }

    public void setCodeId(byte symCodeId) {
        this.codeId = symCodeId;
    }

    public byte getAIMCodeLetter() {
        return this.aimCodeLetter;
    }

    public void setAIMCodeLetter(byte aimId) {
        aimCodeLetter = aimId;
    }

    public byte getAIMModifier() {
        return this.aimModifier;
    }

    public void setAIMModifier(byte aimModifier) {
        this.aimModifier = aimModifier;
    }

    public int getModifierId() {
        return modifier;
    }

    public void setModifierId(int modifier) {
        this.modifier = modifier;
    }

    public int getModifierExId() {
        return modifierEx;
    }

    public void setModifierExId(int modifierEx) {
        this.modifierEx = modifierEx;
    }

    public int getSymbologyId() {
        /*if (symbologyId == SymbologyId.SYMBOLOGY_ID_UNKNOWN) {
            return getSymbologyExType(symbologyExId);
        } else {
            return getSymbologyType(symbologyId);
        }*/
        return (int)symbologyId;
    }

    public void setSymbologyId(long symId) {
        symbologyId = symId;
    }

    public void setSymbologyIdEx(long idEx) {
        symbologyExId = idEx;
    }

    private String convertByteArrayToString(byte[] data) {
        try {
            return new String(this.barcodeByteData, "ISO-8859-1");
        } catch (Exception e) {
        }
        return "";
    }

    private int getSymbologyExType(long idEx) {
        aimModifier = (byte)(modifier);
        if (SymbologyIdEx.SYMBOLOGY_ID_EX_UNKNOWN == idEx) {
            codeId = CodeId.CODE_ID_UNKNOWN;
            aimModifier = 0x30;
            return Symbology.NONE.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_TRIOPTIC == idEx) {
            codeId = CodeId.CODE_ID_TRIOPTIC;
            aimCodeLetter = AIMCodeLetter.AIMID_TRIOPTIC;
            return Symbology.TRIOPTIC.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_TELEPEN == idEx) {
            codeId = CodeId.CODE_ID_TELEPEN;
            aimCodeLetter = AIMCodeLetter.AIMID_TELEPEN;
            return Symbology.TELEPEN.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_OCR == idEx) {
            codeId = CodeId.CODE_ID_OCR;
            aimCodeLetter = AIMCodeLetter.AIMID_OCR;
            return Symbology.DOTCODE.toInt() + 21;
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_NEC25 == idEx) {
            codeId = CodeId.CODE_ID_NEC25;
            aimCodeLetter = AIMCodeLetter.AIMID_IATA25;
            return Symbology.DOTCODE.toInt() + 2;
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_M25 == idEx) {
            codeId = CodeId.CODE_ID_M25;
            aimCodeLetter = AIMCodeLetter.AIMID_MATRIX25;
            return Symbology.MATRIX25.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_KOREA_POST == idEx) {
            codeId = CodeId.CODE_ID_KOREAN_POST;
            aimCodeLetter = AIMCodeLetter.AIMID_KOREAPOST;
            return Symbology.KOREA_POST.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_INFOMAIL == idEx) {
            codeId = CodeId.CODE_ID_INFOMAIL;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.DOTCODE.toInt() + 3;
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_HK25 == idEx) {
            codeId = CodeId.CODE_ID_HK25;
            aimCodeLetter = AIMCodeLetter.AIMID_CHINAPOST;
            return Symbology.CHINESE25.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_HAN_XIN_CODE == idEx) {
            codeId = CodeId.CODE_ID_HAN_XIN_CODE;
            aimCodeLetter = AIMCodeLetter.AIMID_HANXIN;
            return Symbology.HANXIN.toInt();
        } else if (SymbologyIdEx.SYMBOLOGY_ID_EX_CANADIAN_POST == idEx) {
            codeId = CodeId.CODE_ID_CANADIAN_POST;
            aimCodeLetter = AIMCodeLetter.AIMID_CANPOST;
            return Symbology.CANADA_POST.toInt();
        } else if (SymbologyIdEx.SD_CONST_DOTCODE == idEx) {
            codeId = CodeId.CODE_ID_DOTCODE;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.DOTCODE.toInt();
        } else if (SymbologyIdEx.SD_CONST_GM == idEx) {
            codeId = CodeId.CODE_ID_GRID_MATRIX;
            aimCodeLetter = AIMCodeLetter.AIMID_GRIDMATRIX;
            return Symbology.GRIDMATRIX.toInt();
        } else {
            return Symbology.NONE.toInt();
        }
    }

    private int getSymbologyType(long symId) {
        aimModifier = (byte)modifier;
        if (SymbologyId.SYMBOLOGY_ID_UNKNOWN == symId) {
            codeId = CodeId.CODE_ID_UNKNOWN;
            return Symbology.NONE.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_UPC == symId) {
            /*'A': UPC A
            'a': UPC A with Two Character
            '0': UPC A with Five Character
            'B': UPC E0
            'b': UPC E0 with Two Character
            '1': UPC E0 with Five Character
            'C': UPC E1
            'c': UPC E1 with Two Character
            '2': UPC E1 with Five Character
            'D': EAN/JAN8
            'd': EAN/JAN8 with Two Character
            '3': EAN/JAN8 with Five Character
            'E': EAN/JAN13
            'e': EAN/JAN13 with Two Character
            '4': EAN/JAN13 with Five Character*/
            aimCodeLetter = AIMCodeLetter.AIMID_UPC;
            if (modifier == 'A' || modifier == 'a' || modifier == '0') {
                if(modifier == 'a' || modifier == '0') {
                    aimModifier = 0x33;
                } else {
                    aimModifier = 0x30;
                }
                codeId = CodeId.CODE_ID_UPCA;
                return Symbology.UPCA.toInt();
            } else if (modifier == 'B' || modifier == 'b' || modifier == '1') {
                if(modifier == 'b' || modifier == '1') {
                    aimModifier = 0x33;
                } else {
                    aimModifier = 0x30;
                }
                codeId = CodeId.CODE_ID_UPCE;
                return Symbology.UPCE.toInt();
            } else if (modifier == 'C' || modifier == 'c' || modifier == '2') {
                if(modifier == 'c' || modifier == '2') {
                    aimModifier = 0x33;
                } else {
                    aimModifier = 0x30;
                }
                codeId = CodeId.CODE_ID_UPCE;
                return Symbology.UPCE1.toInt();
            } else if (modifier == 'D' || modifier == 'd' || modifier == '3') {
                if(modifier == 'D') {
                    aimModifier = 0x34;
                } else {
                    aimModifier = 0x33;
                }
                codeId = CodeId.CODE_ID_EAN8;
                return Symbology.EAN8.toInt();
            } else if (modifier == 'E' || modifier == 'e' || modifier == '4') {
                if(modifier == 'e' || modifier == '4') {
                    aimModifier = 0x33;
                } else {
                    aimModifier = 0x30;
                }
                codeId = CodeId.CODE_ID_EAN13;
                return Symbology.EAN13.toInt();
            } else {
                return Symbology.NONE.toInt();
            }
        } else if (SymbologyId.SYMBOLOGY_ID_AUS_POST == symId) {
            codeId = CodeId.CODE_ID_AUS_POST;
            aimCodeLetter = AIMCodeLetter.AIMID_AUSPOST;
            return Symbology.POSTAL_AUSTRALIAN.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODE128 == symId) {
            codeId = CodeId.CODE_ID_CODE128;
            aimCodeLetter = AIMCodeLetter.AIMID_CODE128;
            return Symbology.CODE128.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_GS1_128 == symId) {
            codeId = CodeId.CODE_ID_GS1_128;
            aimCodeLetter = AIMCodeLetter.AIMID_CODE128;
            aimModifier = 0x31;
            return Symbology.GS1_128.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODABLOCK_F == symId) {
            codeId = CodeId.CODE_ID_CODABLOCK_F;
            aimCodeLetter = AIMCodeLetter.AIMID_CODABLOCK;
            return Symbology.CODABLOCK_F.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODE39 == symId) {
            codeId = CodeId.CODE_ID_CODE39;
            aimCodeLetter = AIMCodeLetter.AIMID_CODE39;
            return Symbology.CODE39.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODABLOCK_A == symId) {
            codeId = CodeId.CODE_ID_CODABLOCK_A;
            aimCodeLetter = AIMCodeLetter.AIMID_CODABLOCK;
            return Symbology.CODABLOCK_A.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODABAR == symId) {
            codeId = CodeId.CODE_ID_CODABAR;
            aimCodeLetter = AIMCodeLetter.AIMID_CODABAR;
            return Symbology.CODABAR.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_PLANETCODE == symId) {
            codeId = CodeId.CODE_ID_PLANET_CODE;
            aimCodeLetter = AIMCodeLetter.AIMID_PLANET;
            return Symbology.POSTAL_PLANET.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_DATAMATRIX == symId) {
            codeId = CodeId.CODE_ID_DATAMATRIX;
            aimCodeLetter = AIMCodeLetter.AIMID_DATAMATRIX;
            return Symbology.DATAMATRIX.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_MAXICODE == symId) {
            codeId = CodeId.CODE_ID_MAXICODE;
            aimCodeLetter = AIMCodeLetter.AIMID_MAXICODE;
            return Symbology.MAXICODE.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_I25 == symId) {
            codeId = CodeId.CODE_ID_I25;
            aimCodeLetter = AIMCodeLetter.AIMID_INT25;
            return Symbology.INTERLEAVED25.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_PDF == symId) {
            codeId = CodeId.CODE_ID_PDF417;
            aimCodeLetter = AIMCodeLetter.AIMID_PDF417;
            if(modifierEx == 1) {
                codeId = CodeId.CODE_ID_MICROPDF;
                return Symbology.MICROPDF417.toInt();
            }
            return Symbology.PDF417.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_POSTNET == symId) {
            codeId = CodeId.CODE_ID_POSTNET;
            aimCodeLetter = AIMCodeLetter.AIMID_POSTNET;
            return Symbology.POSTAL_POSTNET.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_QRCODE == symId) {
            codeId = CodeId.CODE_ID_QRCODE;
            aimCodeLetter = AIMCodeLetter.AIMID_QR;
            if(modifierEx == 1) {
                return Symbology.MICROQR.toInt();
            }
            return Symbology.QRCODE.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_RSS_14 == symId || SymbologyId.SYMBOLOGY_ID_RSS_14_ST == symId) {
            /**SD_CONST_RSS      (1 << 15)
             * #define  SD_CONST_RSS_EXP            ((SD_CONST_RSS) + 1)
             * #define  SD_CONST_RSS_EXP_ST         ((SD_CONST_RSS) + 2)
             * #define  SD_CONST_RSS_14_LIM         ((SD_CONST_RSS) + 4)
             * #define  SD_CONST_RSS_14             ((SD_CONST_RSS) + 8)
             * #define  SD_CONST_RSS_14_ST          ((SD_CONST_RSS) + 16)
             */
            codeId = CodeId.CODE_ID_RSS;
            aimCodeLetter = AIMCodeLetter.AIMID_RSS;
            return Symbology.GS1_14.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_RSS_EXP == symId || SymbologyId.SYMBOLOGY_ID_RSS_EXP_ST == symId) {
            codeId = CodeId.CODE_ID_RSS;
            aimCodeLetter = AIMCodeLetter.AIMID_RSS;
            return Symbology.GS1_EXP.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_RSS_14_LIM == symId) {
            codeId = CodeId.CODE_ID_RSS;
            aimCodeLetter = AIMCodeLetter.AIMID_RSS;
            return Symbology.GS1_LIMIT.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_JAPAN_POST == symId) {
            codeId = CodeId.CODE_ID_JAPAN_POST;
            aimCodeLetter = AIMCodeLetter.AIMID_JAPOST;
            return Symbology.POSTAL_JAPAN.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODE93 == symId) {
            codeId = CodeId.CODE_ID_CODE93;
            aimCodeLetter = AIMCodeLetter.AIMID_CODE93;
            return Symbology.CODE93.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_AZTEC_CODE == symId) {
            codeId = CodeId.CODE_ID_AZTEC_CODE;
            aimCodeLetter = AIMCodeLetter.AIMID_AZTEC;
            return Symbology.AZTEC.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_ROYAL_MAIL == symId) {
            codeId = CodeId.CODE_ID_BRITISH_POST;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.POSTAL_ROYALMAIL.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_KIX == symId) {
            codeId = CodeId.CODE_ID_KIX_CODE;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.POSTAL_KIX.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_S25 == symId || SymbologyId.SYMBOLOGY_ID_S25_2BAR == symId || SymbologyId.SYMBOLOGY_ID_S25_3BAR == symId) {
            codeId = CodeId.CODE_ID_S25;
            if (SymbologyId.SYMBOLOGY_ID_S25_2BAR == symId) {
                aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_S25_2BAR;
            } else {
                aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_S25_3BAR;
            }
            return Symbology.DISCRETE25.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_MSI_PLESSEY == symId) {
            codeId = CodeId.CODE_ID_MSI;
            aimCodeLetter = AIMCodeLetter.AIMID_MSI;
            return Symbology.MSI.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_GO_CODE == symId) {
            codeId = CodeId.CODE_ID_PLESSEY;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.DOTCODE.toInt() + 4;
        } else if (SymbologyId.SYMBOLOGY_ID_PHARMACODE == symId) {
            codeId = CodeId.CODE_ID_CODE39_BASE32;
            aimCodeLetter = AIMCodeLetter.AIM_CODELETTER_OTHER;
            return Symbology.CODE32.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_UPU_4_STATE == symId) {
            codeId = CodeId.CODE_ID_UPU_4_STATE;
            aimCodeLetter = AIMCodeLetter.AIMID_USPS4CB;
            return Symbology.POSTAL_4STATE.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_CODE11 == symId) {
            codeId = CodeId.CODE_ID_CODE11;
            aimCodeLetter = AIMCodeLetter.AIMID_CODE11;
            return Symbology.CODE11.toInt();
        } else if (SymbologyId.SYMBOLOGY_ID_USPS_4_STATE == symId) {
            codeId = CodeId.CODE_ID_USPS_4_STATE;
            aimCodeLetter = AIMCodeLetter.AIMID_USPS4CB;
            return Symbology.POSTAL_UPUFICS.toInt();
        } else {
            return Symbology.NONE.toInt();
        }
    }
}
