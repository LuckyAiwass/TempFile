package com.ubx.decoder;

public final class General {
    public static final int CONST_DISABLE = 0;
    public static final int CONST_ENABLE = 1;
    public static final int CONST_ENABLE_0F = 15;
    public static final int CONST_ENABLE_1F = 31;
    public static final int CONST_ENABLE_7F = 127;
    public static final int CONST_ENABLE_3C = 0x3c;
    public static final int CONST_INVERSE_ENABLE = 2;
    
    public static final int  SD_CONST_CORE     = (1 << 0);
    public static final int  SD_CONST_AGC      = (1 << 1);
    public static final int  SD_CONST_UPC      = (1 << 2);
    public static final int  SD_CONST_AP       = (1 << 3);
    public static final int  SD_CONST_C128     = (1 << 4);
    public static final int  SD_CONST_C39      = (1 << 5);
    public static final int  SD_CONST_CB       = (1 << 6);
    public static final int  SD_CONST_PL       = (1 << 7);
    public static final int  SD_CONST_DM       = (1 << 8);
    public static final int  SD_CONST_I25      = (1 << 9);
    public static final int  SD_CONST_MANOP    = (1 << 10);
    public static final int  SD_CONST_MC       = (1 << 11);
    public static final int  SD_CONST_PDF      = (1 << 12);
    public static final int  SD_CONST_PN       = (1 << 13);
    public static final int  SD_CONST_QR       = (1 << 14);
    public static final int  SD_CONST_RSS      = (1 << 15);
    public static final int  SD_CONST_UNOP     = (1 << 16);
    public static final int  SD_CONST_JP       = (1 << 17);
    public static final int  SD_CONST_C93      = (1 << 18);
    public static final int  SD_CONST_AZ       = (1 << 19);
    public static final int  SD_CONST_PD       = (1 << 20);
    public static final int  SD_CONST_RM       = (1 << 21);
    public static final int  SD_CONST_S25      = (1 << 22);
    public static final int  SD_CONST_MSIP     = (1 << 23);
    public static final int  SD_CONST_DP       = (1 << 25);
    public static final int  SD_CONST_PHARMA   = (1 << 26);
    public static final int  SD_CONST_UPU      = (1 << 27);
    public static final int  SD_CONST_C11      = (1 << 28);
    public static final int  SD_CONST_USPS4CB  = (1 << 31);
    
        /* The following definitions apply to EX Properties */
    public static final int  SD_CONST_M25      = (1 << 0);
    public static final int  SD_CONST_TP       = (1 << 1);
    public static final int  SD_CONST_NEC25    = (1 << 2);
    public static final int  SD_CONST_TRIOPTIC = (1 << 3);
    public static final int  SD_CONST_OCR      = (1 << 4);
    public static final int  SD_CONST_VER1D    = (1 << 5);
    public static final int  SD_CONST_HK25     = (1 << 6);
    public static final int  SD_CONST_VERPN    = (1 << 7);
    public static final int  SD_CONST_VERPDF   = (1 << 9);
    public static final int  SD_CONST_INFOMAIL = (1 << 12);
    public static final int  SD_CONST_VER2D    = (1 << 13);
    public static final int  SD_CONST_KP       = (1 << 14);
    public static final int  SD_CONST_CP       = (1 << 16);
    public static final int  SD_CONST_LC       = (1 << 17);
    public static final int  SD_CONST_SP       = (1 << 18);
    public static final int  SD_CONST_EIB      = (1 << 19);
    public static final int  SD_CONST_BZ4      = (1 << 20);
    public static final int  SD_CONST_GM       = (1 << 22);
    public static final int  SD_CONST_DOTCODE  = (1 << 23);

    /* Definitions->Constants->Symbology Groups */
public static final int SD_CONST_SYMBOLOGIES_GROUP = ((SD_CONST_UPC) | 
            (SD_CONST_AP) | 
            (SD_CONST_JP) | 
            (SD_CONST_PL) | 
            (SD_CONST_PN) | 
            (SD_CONST_C11) | 
            (SD_CONST_C128) | 
            (SD_CONST_C39) | 
            (SD_CONST_C93) | 
            (SD_CONST_CB) | 
            (SD_CONST_DM) | 
            (SD_CONST_I25) | 
            (SD_CONST_S25) | 
            (SD_CONST_MC) | 
            (SD_CONST_MSIP) | 
            (SD_CONST_PDF) | 
            (SD_CONST_QR) | 
            (SD_CONST_RSS) | 
            (SD_CONST_AZ) | 
            (SD_CONST_PHARMA) | 
            (SD_CONST_UPU) | 
            (SD_CONST_RM) | 
            (SD_CONST_USPS4CB));

            public static final int SD_CONST_SYMBOLOGIES_GROUP_EX = ((SD_CONST_M25)   | 
            (SD_CONST_TP)    | 
            (SD_CONST_NEC25) | 
            (SD_CONST_TRIOPTIC)  | 
            (SD_CONST_OCR)  | 
            (SD_CONST_HK25) | 
            (SD_CONST_INFOMAIL) | 
            (SD_CONST_KP) | 
            (SD_CONST_CP) | 
            (SD_CONST_LC) | 
            (SD_CONST_SP) | 
            (SD_CONST_EIB) | 
            (SD_CONST_BZ4) | 
            (SD_CONST_GM) | 
            (SD_CONST_DOTCODE));

            public static final int SD_CONST_LINEAR_SYMBOLOGIES_GROUP = ((SD_CONST_UPC) | 
            (SD_CONST_C11) | 
            (SD_CONST_C128) | 
            (SD_CONST_C39) | 
            (SD_CONST_C93) | 
            (SD_CONST_CB) | 
            (SD_CONST_I25) | 
            (SD_CONST_S25) | 
            (SD_CONST_MSIP) | 
            (SD_CONST_PDF) | 
            (SD_CONST_PHARMA) | 
            (SD_CONST_RSS));

            public static final int SD_CONST_LINEAR_SYMBOLOGIES_GROUP_EX = ((SD_CONST_M25)   | 
            (SD_CONST_TP)    | 
            (SD_CONST_NEC25) | 
            (SD_CONST_TRIOPTIC) | 
            (SD_CONST_HK25) | 
            (SD_CONST_KP) | 
            (SD_CONST_LC));

            public static final int SD_CONST_POSTAL_SYMBOLOGIES_GROUP = ((SD_CONST_AP) | 
            (SD_CONST_JP) | 
            (SD_CONST_PL) | 
            (SD_CONST_PN) | 
            (SD_CONST_UPU) | 
            (SD_CONST_RM) | 
            (SD_CONST_USPS4CB));

            public static final int SD_CONST_POSTAL_SYMBOLOGIES_GROUP_EX  = ((SD_CONST_INFOMAIL) | 
            (SD_CONST_CP) | 
            (SD_CONST_SP) | 
            (SD_CONST_EIB) | 
            (SD_CONST_BZ4));
}
