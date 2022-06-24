package com.ubx.decoder;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

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
 * @Date: 20-6-16下午7:52
 * https://www.gs1.org/standards/barcodes/application-identifiers?lang=zh
 */
public class GS1DataParser {
    /* "]C1", // GS1-128
     "]e0", // GS1/GS1 DataBar Expanded/GS1 DataBar Limited
     "]d2", // DataMatrix
     "]Q3", // QR code
     "]E0", // EAN-13 Composite / UPC-A Composite / UPC-E Composite
     "]E4", // EAN-8 Composite*/
    private static byte[] aiSeparatorChar = new byte[]{0x28, 0x29};
    /*
     * BarcodeCodeID byte[i] is code type
     */
    private static final byte[] SEPARATORCODE = {
            0x49, // GS1-128     AIM ID is ]C1 , Honeywell ID is 0x49
            0x79, // GS1		 AIM ID is ]e0 , Honeywell ID is 0x79
            0x77, // DataMatrix  AIM ID is ]d2 , Honeywell ID is 0x77
            0x73, // QR code 	 AIM ID is ]Q3 , Honeywell ID is 0x73
            0x7D, // GS1 DataBar Expanded    AIM ID is ]e0 , Honeywell ID is 0x7D
            0x7B, // GS1 DataBar Limited    AIM ID is ]e0 , Honeywell ID is 0x7B
            0x64, // EAN-13 Composite    AIM ID is ]E0 , Honeywell ID is 0x64
            0x44, // EAN-8 Composite    AIM ID is ]E4 , Honeywell ID is 0x44
            0x63, // UPC-A Composite    AIM ID is ]E0 , Honeywell ID is 0x63
            0x45  // UPC-E Composite    AIM ID is ]E0 , Honeywell ID is 0x45
    };

    private static final byte[] AIM = {
            0x43, // first BarcodeAimID ASCII is C
            0x65, // first BarcodeAimID ASCII is e
            0x64, // first BarcodeAimID ASCII is d
            0x51, // first BarcodeAimID ASCII is Q
            0x65, // first BarcodeAimID ASCII is e (GS1 DataBar Expanded) , SEPARATORCODE is 0x7D
            0x65, // first BarcodeAimID ASCII is e (GS1 DataBar Limited) , SEPARATORCODE is 0x7B
            0x45, // first BarcodeAimID ASCII is E  (EAN-13 Composite), SEPARATORCODE is 0x64
            0x45, // first BarcodeAimID ASCII is E (EAN-8 Composite) , SEPARATORCODE is 0x44
            0x45, // first BarcodeAimID ASCII is E (UPC-A Composite) , SEPARATORCODE is 0x63
            0x45  // first BarcodeAimID ASCII is E (UPC-E Composite) , SEPARATORCODE is 0x45
    };

    private static final byte[] AIMMODIFIER = {
            0x31, // second BarcodeAimModifier ID ASCII is number 1
            0x30, // second BarcodeAimModifier ID ASCII is number 0
            0x32, // second BarcodeAimModifier ID ASCII is number 2
            0x33, // second BarcodeAimModifier ID ASCII is number 3
            0x30, // second BarcodeAimModifier ID ASCII is number 0 (GS1 DataBar Expanded) , SEPARATORCODE is 0x7D
            0x30, // second BarcodeAimModifier ID ASCII is number 0 (GS1 DataBar Limited) , SEPARATORCODE is 0x7B
            0x30, // second BarcodeAimModifier ID ASCII is number 0 (EAN-13 Composite) , SEPARATORCODE is 0x64
            0x34, // second BarcodeAimModifier ID ASCII is number 4 (EAN-8 Composite) , SEPARATORCODE is 0x44
            0x30, // second BarcodeAimModifier ID ASCII is number 0 (UPC-A Composite) , SEPARATORCODE is 0x63
            0x30  // second BarcodeAimModifier ID ASCII is number 0 (UPC-E Composite) , SEPARATORCODE is 0x45
    };

    // 4710 / 2100 / 4750
    private static final int[] COMPOSITECODEIDS = {
            82, // CCA EAN-13
            83, // CCA EAN-8
            87, // CCA UPC-A
            88, // CCA UPC-E
            98, // CCB EAN-13
            99, // CCB EAN-8
            103,// CCB UPC-A
            104 // CCB UPC-E
    };
    // 6603 / 6703 , EAN-13/EAN-8/UPC-A/UPC-E/ return false , other return true
    public static final boolean isSupperCompositeCode(byte aim , byte aimModifier ,byte code) {
        if(code == SEPARATORCODE[6] && aim == AIM[6] && aimModifier == AIMMODIFIER[6]) {
            return false; // EAN-13
        } else if(code == SEPARATORCODE[7] && aim == AIM[7] && aimModifier == AIMMODIFIER[7]) {
            return false; // EAN-8
        } else if(code == SEPARATORCODE[8] && aim == AIM[8] && aimModifier == AIMMODIFIER[8]) {
            return false; // UPC-A
        } else if(code == SEPARATORCODE[9] && aim == AIM[9] && aimModifier == AIMMODIFIER[9]) {
            return false; // UPC-E
        }
        return true;
    }

    // 4710 / 2100 / 4750
    public static final int isSupperCompositeCode(byte[] data) {
        if(data == null || data.length < 4) { // ]e0 length is 3 , data length must > 3
            return 0;
        }
        for(int i=0;i<data.length - 3;i++) {
            if(data[i] == 0x5D && data[i+1] == 0x65 && data[i+2] == 0x30) { // ]e0 is 0x5D 0x65 0x30
                return i;
            }
        }
        return 0;
    }

    // Se4710/Se2100
    public static final int compositeIndexCode(String aimCodeIDStr , int codeID) {
        if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 1].equals(aimCodeIDStr)) {
            if(codeID == COMPOSITECODEIDS[1] || codeID == COMPOSITECODEIDS[5]) {
                return 8; // EAN-8 Composite max length is 8 , contains CCA EAN-8 and CCB EAN-8
            }
        } else if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 2].equals(aimCodeIDStr)) {
            if(codeID == COMPOSITECODEIDS[0] || codeID == COMPOSITECODEIDS[4]) {
                return 13; // EAN-13 Composite max length is 13 , contains CCA EAN-13 and CCB EAN-13
            } else if(codeID == COMPOSITECODEIDS[2] || codeID == COMPOSITECODEIDS[6]) {
                return 12; // UPC-A Composite max length is 12 , contains CCA UPC-A and CCB UPC-A
            } else if(codeID == COMPOSITECODEIDS[3] || codeID == COMPOSITECODEIDS[7]) {
                return 8; // UPC-E Composite max length is 8 , contains CCA UPC-E and CCB UPC-E
            }
        }
        return 0;
    }
    // Se4710/Se2100 AIM Code ID Character
    private static final String[] SEAIMMODIFIERCODE = {
            "]C1", // GS1-128
            "]e0", // GS1/GS1 DataBar Expanded/GS1 DataBar Limited
            "]d2", // DataMatrix
            "]Q3", // QR code
            "]E0", // EAN-13 Composite / UPC-A Composite / UPC-E Composite
            "]E4", // EAN-8 Composite
    };
    //0x64, // EAN-13 Composite    AIM ID is ]E0 , Honeywell ID is 0x64
    //0x44, // EAN-8 Composite    AIM ID is ]E4 , Honeywell ID is 0x44
    //0x63, // UPC-A Composite    AIM ID is ]E0 , Honeywell ID is 0x63
    //0x45  // UPC-E Composite    AIM ID is ]E0 , Honeywell ID is 0x45
    public static int ignoreUPCCompositeCode(byte codeID, int length) {
        if(codeID == 0x64 && length > 13) {
            return 13;
        } else if(codeID == 0x44 && length > 8) {
            return 8;
        } else if(codeID == 0x45 && length > 8 ) {
            return 8;
        } else if(codeID == 0x63 && length > 12) {
            return 12;
        }
        return 0;
    }
    /*
     * BarcodeCodeID contains one of SEPARATORCODE
     * (AIM[i]AIMMODIFIER[i]) ---> SEPARATORCODE[i]
     * AIM[0]AIMMODIFIER[0] ---> C1
     * AIM[1]AIMMODIFIER[1] ---> e0
     * AIM[2]AIMMODIFIER[2] ---> d2
     * AIM[3]AIMMODIFIER[3] ---> Q3
     * Data content of GS1 DataMatrix symbol
     * ------------------------------------------------------------------------------------------------------------
     * |FNC1|ES 1 (predefined length) |ES 2 (non-predefined length) | FNC1 or <GS> | ES 3 (non-predefined length) |
     * ------------------------------------------------------------------------------------------------------------
     * FNC1 value is ]C1、]e0、]d2、]Q3
     * if contains C1/e0/d2/Q3(]C1、]e0、]d2、]Q3)  and separator decode.
     */
    public static byte[] addAISeparator(byte aimCodeLetter, byte aimModifier, byte[] barcodeData, byte[] sepChar) {
        if(sepChar != null) {
            if(sepChar.length == 1) {
                aiSeparatorChar[0] = sepChar[0];
            } else if(sepChar.length >= 2) {
                aiSeparatorChar[0] = sepChar[0];
                aiSeparatorChar[1] = sepChar[1];
            }
        }
        try {
            if ((aimCodeLetter == 'd' && aimModifier == '2')
                    || (aimCodeLetter == 'd' && aimModifier == '5')
                    || (aimCodeLetter == 'C' && aimModifier == '1')
                    || (aimCodeLetter == 'e' && aimModifier == '0')
                    || (aimCodeLetter == 'E' && aimModifier == '0')
                    || (aimCodeLetter == 'E' && aimModifier == '4')
                    || (aimCodeLetter == 'Q' && aimModifier == '3')) {
                //单线程性能比StringBuffer高，StringBuffer支持多线程并发
                StringBuilder sbIdentifier = new StringBuilder();
                String currentAIStr = "";
                GS1ApplicationIdentifier ai = null;
                for (int i = 0; i < barcodeData.length; i++) {
                    if (barcodeData[i] != 29) {
                        currentAIStr = currentAIStr + (char) barcodeData[i];
                        ai = GS1ApplicationIdentifier.getByIdentifier(currentAIStr);
                        if (ai != null) {
                            int dpLength = ai.getAILength() - currentAIStr.length();
                            if(dpLength > 0) {
                                char AIsuffix = (char) barcodeData[i + 1];
                                sbIdentifier.append((char)aiSeparatorChar[0]);
                                sbIdentifier.append(currentAIStr);
                                sbIdentifier.append(AIsuffix);
                                sbIdentifier.append((char)aiSeparatorChar[1]);
                            } else {
                                sbIdentifier.append((char)aiSeparatorChar[0]);
                                sbIdentifier.append(currentAIStr);
                                sbIdentifier.append((char)aiSeparatorChar[1]);
                            }
                            int currentIndex = i;
                            for (int j = 0; j < ai.getValueMaxLength(); j++) {
                                currentIndex = i + 1 + dpLength + j;
                                if (currentIndex >= barcodeData.length)
                                    break;
                                if (barcodeData[currentIndex] == 29)
                                    break;
                                sbIdentifier.append((char) barcodeData[currentIndex]);
                            }
                            i = currentIndex;
                            currentAIStr = "";
                        }
                    }
                }
                String gs1Code = sbIdentifier.toString();
                if(TextUtils.isEmpty(gs1Code) == false) {
                    return gs1Code.getBytes();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<GS1ApplicationIdentifier> parse(byte aimCodeLetter, byte aimModifier, byte[] rawData) {
        List<GS1ApplicationIdentifier> barcodeAIList = new ArrayList<>();
        try {
            if ((aimCodeLetter == 'd' && aimModifier == '2')
                    || (aimCodeLetter == 'd' && aimModifier == '5')
                    || (aimCodeLetter == 'C' && aimModifier == '1')
                    || (aimCodeLetter == 'e' && aimModifier == '0')
                    || (aimCodeLetter == 'E' && aimModifier == '0')
                    || (aimCodeLetter == 'E' && aimModifier == '4')
                    || (aimCodeLetter == 'Q' && aimModifier == '3')) {
                String currentAIStr = "";
                for (int i = 0; i < rawData.length; i++) {
                    if (rawData[i] != 29) {
                        currentAIStr = currentAIStr + (char) rawData[i];
                        GS1ApplicationIdentifier ai = GS1ApplicationIdentifier.getByIdentifier(currentAIStr);
                        if (ai != null) {
                            int dpLength = ai.getAILength() - currentAIStr.length();
                            String dpp = "";
                            for (int x = 0; x < dpLength; x++)
                                dpp = dpp + (char) rawData[i + 1 + x];
                            ai.setDecimalPointPlace(dpp);
                            int currentIndex = i;
                            String value = "";
                            for (int j = 0; j < ai.getValueMaxLength(); j++) {
                                currentIndex = i + 1 + dpLength + j;
                                if (currentIndex >= rawData.length)
                                    break;
                                if (rawData[currentIndex] == 29)
                                    break;
                                value = value + (char) rawData[currentIndex];
                            }
                            ai.setValue(value);
                            if(dpLength > 0) {
                                char AIsuffix = (char) rawData[i + 1];
                                ai.setSepAI(((char)aiSeparatorChar[0]) + currentAIStr + AIsuffix+((char)aiSeparatorChar[1]));
                            } else {
                                ai.setSepAI(((char)aiSeparatorChar[0]) + currentAIStr +((char)aiSeparatorChar[1]));
                            }
                            barcodeAIList.add(ai);
                            i = currentIndex;
                            currentAIStr = "";
                        }
                    }
                }
            }
            return barcodeAIList;
        } catch (Exception e) {
            e.printStackTrace();
            return barcodeAIList;
        }
    }
}

