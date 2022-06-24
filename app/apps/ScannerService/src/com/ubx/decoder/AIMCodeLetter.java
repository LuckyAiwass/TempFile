package com.ubx.decoder;

final public class AIMCodeLetter {
    public static final char AIM_CODELETTER_UNKNOWN = '\000';
    public static final char AIM_CODELETTER_CODE39 = 'A';
    public static final char AIM_CODELETTER_TELEPEN = 'B';
    public static final char AIM_CODELETTER_CODE128 = 'C';
    public static final char AIM_CODELETTER_GS1_128 = 'C';
    public static final char AIM_CODELETTER_CODE1 = 'D';
    public static final char AIM_CODELETTER_UPC = 'E';
    public static final char AIM_CODELETTER_CODABAR = 'F';
    public static final char AIM_CODELETTER_CODE93 = 'G';
    public static final char AIM_CODELETTER_CODE11 = 'H';
    public static final char AIM_CODELETTER_I25 = 'I';
    public static final char AIM_CODELETTER_CODE16K = 'K';
    public static final char AIM_CODELETTER_PDF417 = 'L';
    public static final char AIM_CODELETTER_TLC39 = 'L';
    public static final char AIM_CODELETTER_MSI = 'M';
    public static final char AIM_CODELETTER_ANKER = 'N';
    public static final char AIM_CODELETTER_CODABLOCK = 'O';
    public static final char AIM_CODELETTER_PLESSEY = 'P';
    public static final char AIM_CODELETTER_QRCODE = 'Q';
    public static final char AIM_CODELETTER_S25_2BAR = 'R';
    public static final char AIM_CODELETTER_S25_3BAR = 'S';
    public static final char AIM_CODELETTER_CODE49 = 'T';
    public static final char AIM_CODELETTER_MAXICODE = 'U';
    public static final char AIM_CODELETTER_OTHER = 'X';
    public static final char AIM_CODELETTER_EXPANSION = 'Y';
    public static final char AIM_CODELETTER_NONBARCODE = 'Z';
    public static final char AIM_CODELETTER_CHANNELCODE = 'c';
    public static final char AIM_CODELETTER_DATAMATRIX = 'd';
    public static final char AIM_CODELETTER_RSS = 'e';
    public static final char AIM_CODELETTER_GS1_DATABAR = 'e';
    public static final char AIM_CODELETTER_COMPOSITE = 'e';
    public static final char AIM_CODELETTER_OCR = 'o';
    public static final char AIM_CODELETTER_POSICODE = 'p';
    public static final char AIM_CODELETTER_SUPERCODE = 's';
    public static final char AIM_CODELETTER_ULTRACODE = 'v';
    public static final char AIM_CODELETTER_AZTEC_CODE = 'z';

    //-----------------------------------------------------------------------------
    //  AIM Symbology ID characters
    /*Bookland EAN, ISSN EAN, Trioptic Code 39, Chinese 2 of 5,
    Matrix 2 of 5, Korean 3 of 5, US Postnet, US Planet, UK Postal, Japan
    Postal, Australia Post, Netherlands KIX Code, USPS 4CB/One Code/
    Intelligent Mail, UPU FICS Postal, Signature Capture*/
    //X Modifier 0-F may be assigned by the decoder manufacturer
    //-----------------------------------------------------------------------------
    public static final char AIMID_AZTEC        = 'z';
    public static final char AIMID_CODABAR      = 'F';
    public static final char AIMID_CODE11       = 'H';
    public static final char AIMID_CODE128      = 'C';
    public static final char AIMID_CODE39       = 'A';
    public static final char AIMID_CODE49       = 'T';
    public static final char AIMID_CODE93       = 'G';
    public static final char AIMID_COMPOSITE    = 'e';
    public static final char AIMID_DATAMATRIX   = 'd';
    public static final char AIMID_EAN          = 'E';
    public static final char AIMID_INT25        = 'I';
    public static final char AIMID_MAXICODE     = 'U';
    public static final char AIMID_MICROPDF     = 'L';
    public static final char AIMID_PDF417       = 'L';
    public static final char AIMID_OCR          = 'o';
    public static final char AIMID_QR           = 'Q';
    public static final char AIMID_RSS          = 'e';
    public static final char AIMID_UPC          = 'E';
    public static final char AIMID_POSTNET      = 'X';
    public static final char AIMID_ISBT         = 'C';
    public static final char AIMID_BPO          = 'X';
    public static final char AIMID_CANPOST      = 'X';
    public static final char AIMID_AUSPOST      = 'X';
    //with two bar start/stop codes
    public static final char AIMID_IATA25       = 'R';
    public static final char AIMID_CODABLOCK    = 'O';
    public static final char AIMID_JAPOST       = 'X';
    public static final char AIMID_PLANET       = 'X';
    public static final char AIMID_DUTCHPOST    = 'X';
    public static final char AIMID_MSI          = 'M';
    public static final char AIMID_TLC39        = 'L';
    public static final char AIMID_TRIOPTIC     = 'X';
    public static final char AIMID_CODE32		= 'X';
    //with three bar start/stop codes
    public static final char AIMID_STRT25		= 'S';
    public static final char AIMID_MATRIX25		= 'X';
    public static final char AIMID_PLESSEY		= 'P';
    public static final char AIMID_CHINAPOST	= 'X';
    public static final char AIMID_KOREAPOST	= 'X';
    public static final char AIMID_TELEPEN		= 'B';
    public static final char AIMID_CODE16K		= 'K';
    public static final char AIMID_POSICODE		= 'p';
    public static final char AIMID_COUPONCODE	= 'E';
    public static final char AIMID_USPS4CB      = 'X';
    public static final char AIMID_IDTAG		= 'X';
    public static final char AIMID_LABEL		= 'X';
    public static final char AIMID_GS1_128      = 'C';
    public static final char AIMID_HANXIN       = 'X';
    public static final char AIMID_GRIDMATRIX   = 'g';
    public static final char AIMID_CHannelCode  = 'c';
    public static final char AIMID_SuperCode    = 's';
    public static final char AIMID_Non_barCode  = 'Z';
    public static final char AIMID_System_expansion  = 'Y';
    public static final char AIMID_Anker        = 'N';
}
