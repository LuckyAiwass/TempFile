package com.ubx.decoder;

/*
 * Copyright (C) 2019 = Urovo Ltd
 *
 * Licensed under the Apache License = Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing = software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND = either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @Author: rocky
 * @Date: 19-11-4下午4:54
 */
public class SDSProperties {
    private static final int PRO_BASE_MIN_RESERVED = 300;
    /**
     * Upper left X coordinate setting
     */
    public static final int UpperLeftX = PRO_BASE_MIN_RESERVED + 0;
    /**
     * Upper left Y coordinate setting
     */
    public static final int UpperLeftY = PRO_BASE_MIN_RESERVED + 1;
    /**
     * Lower right X coordinate setting
     */
    public static final int LowerRightX = PRO_BASE_MIN_RESERVED + 2;
    /**
     * Lower right Y coordinate setting
     */
    public static final int LowerRightY = PRO_BASE_MIN_RESERVED + 3;
    public static final int CODE11_LENGTH1 = PRO_BASE_MIN_RESERVED + 4;
    public static final int CODE11_LENGTH2 = PRO_BASE_MIN_RESERVED + 5;
    public static final int I25_LENGTH1 = PRO_BASE_MIN_RESERVED + 6;
    public static final int I25_LENGTH2 = PRO_BASE_MIN_RESERVED + 7;
    public static final int M25_LENGTH1 = PRO_BASE_MIN_RESERVED + 8;
    public static final int M25_LENGTH2 = PRO_BASE_MIN_RESERVED + 9;
    public static final int CODABAR_LENGTH1 = PRO_BASE_MIN_RESERVED + 10;
    public static final int CODABAR_LENGTH2 = PRO_BASE_MIN_RESERVED + 11;
    public static final int CODE93_LENGTH1 = PRO_BASE_MIN_RESERVED + 12;
    public static final int CODE93_LENGTH2 = PRO_BASE_MIN_RESERVED + 13;
    public static final int CODE128_LENGTH1 = PRO_BASE_MIN_RESERVED + 14;
    public static final int CODE128_LENGTH2 = PRO_BASE_MIN_RESERVED + 15;
    public static final int MSI_LENGTH1 = PRO_BASE_MIN_RESERVED + 16;
    public static final int MSI_LENGTH2 = PRO_BASE_MIN_RESERVED + 17;
    public static final int GS1_EXP_LENGTH1 = PRO_BASE_MIN_RESERVED + 18;
    public static final int GS1_EXP_LENGTH2 = PRO_BASE_MIN_RESERVED + 19;
    public static final int DATAMATRIX_LENGTH1 = PRO_BASE_MIN_RESERVED + 20;
    public static final int DATAMATRIX_LENGTH2 = PRO_BASE_MIN_RESERVED + 21;
    public static final int CODE39_LENGTH1 = PRO_BASE_MIN_RESERVED + 22;
    public static final int CODE39_LENGTH2 = PRO_BASE_MIN_RESERVED + 23;
    public static final int D25_LENGTH1 = PRO_BASE_MIN_RESERVED + 24;
    public static final int D25_LENGTH2 = PRO_BASE_MIN_RESERVED + 25;
    public static final int GS1_128_ENABLE = PRO_BASE_MIN_RESERVED + 26;
    public static final int ISBT128_ENABLE = PRO_BASE_MIN_RESERVED + 27;
    public static final int ISBT128_CHECK_TABLE = PRO_BASE_MIN_RESERVED + 28;
    public static final int UPCA_ENABLE = PRO_BASE_MIN_RESERVED + 29;
    public static final int UPCA_SEND_CHECK = PRO_BASE_MIN_RESERVED + 30;
    public static final int UPCA_SEND_SYS = PRO_BASE_MIN_RESERVED + 31;
    public static final int UPCA_TO_EAN13 = PRO_BASE_MIN_RESERVED + 32;
    public static final int UPCE_ENABLE = PRO_BASE_MIN_RESERVED + 33;
    public static final int UPCE_SEND_CHECK = PRO_BASE_MIN_RESERVED + 34;
    public static final int UPCE_SEND_SYS = PRO_BASE_MIN_RESERVED + 35;
    public static final int UPCE_TO_UPCA = PRO_BASE_MIN_RESERVED + 36;
    public static final int UPCE1_ENABLE = PRO_BASE_MIN_RESERVED + 37;
    public static final int UPCE1_SEND_CHECK = PRO_BASE_MIN_RESERVED + 38;
    public static final int UPCE1_SEND_SYS = PRO_BASE_MIN_RESERVED + 39;
    public static final int UPCE1_TO_UPCA = PRO_BASE_MIN_RESERVED + 40;
    public static final int EAN13_ENABLE = PRO_BASE_MIN_RESERVED + 41;
    public static final int EAN13_BOOKLANDEAN = PRO_BASE_MIN_RESERVED + 42; //BOOKLANDEAN
    public static final int EAN13_BOOKLAND_FORMAT = PRO_BASE_MIN_RESERVED + 43; //df 0x00 ISBN-100x01 ISBN-13
    public static final int EAN13_SEND_CHECK = PRO_BASE_MIN_RESERVED + 44;
    public static final int EAN8_ENABLE = PRO_BASE_MIN_RESERVED + 45;
    public static final int EAN8_SEND_CHECK = PRO_BASE_MIN_RESERVED + 46;
    public static final int EAN8_TO_EAN13 = PRO_BASE_MIN_RESERVED + 47; //EAN-8 Zero Extend
    //public static final int EAN_EXT_ENABLE_2_5_DIGIT = 49;
    public static final int GS1_DATABAR14_TO_UPC = PRO_BASE_MIN_RESERVED + 48;
    public static final int CODABAR_CLSI_Editing = PRO_BASE_MIN_RESERVED + 49;
    public static final int CODABAR_NOTIS_Editing = PRO_BASE_MIN_RESERVED + 50;
    public static final int I25_TO_EAN13 = PRO_BASE_MIN_RESERVED + 51;
    public static final int CODE39_TO_CODE32 = PRO_BASE_MIN_RESERVED + 52;
    public static final int CODE32_PREFIX = PRO_BASE_MIN_RESERVED + 53;
    public static final int Composite_GS1_128 = PRO_BASE_MIN_RESERVED + 54;
    public static final int Composite_GS1_Databar = PRO_BASE_MIN_RESERVED + 55;
    public static final int Composite_Code39 = PRO_BASE_MIN_RESERVED + 56;
    public static final int Composite_CC_A = PRO_BASE_MIN_RESERVED + 57;
    public static final int Composite_CC_B = PRO_BASE_MIN_RESERVED + 58;
    public static final int Composite_CC_C = PRO_BASE_MIN_RESERVED + 59;
    public static final int ADD_SEARCH_TIME_UPC_COMPOSITE = PRO_BASE_MIN_RESERVED + 60;
    public static final int COMPOSITE_WITH_UPC_ENABLED = PRO_BASE_MIN_RESERVED + 61;
    public static final int COMPOSITE_WITH_UPC_MODE = PRO_BASE_MIN_RESERVED + 62;
    public static final int UDI_DECODE_MODE = PRO_BASE_MIN_RESERVED + 63;
    public static final int ISBT_GS1_Concatenation = PRO_BASE_MIN_RESERVED + 64;
    public static final int PRO_BASE_MAX_RESERVED = ISBT_GS1_Concatenation + 1;
    //Miscellaneous properties

    /*Description
    It configures how the finder scans will be done on decoder.
    For linear code = when cycling finder is enabled the decoder focuses its search for bar codes on the center of the
    image. This is appropriate for handheld readers and applications where the bar code to be read is expected to
    always appear near the center of the image. When this property is disabled = the decoder performs a uniform search
    over the entire image.
    This property has no effect if SD_PROP_MISC_PASS_THROUGH is activated.
            When this property is enabled = bar codes located far from the image center may not be decoded.
    The property values are:
            0: disable
            1: enable
    default value: 0
    See also
        SD_PROP_MISC_PASS_THROUGH
            SD_PROP_IMAGE_SEARCH_CENTER_X
        SD_PROP_IMAGE_SEARCH_CENTER_Y
        */
    public static final int SD_PROP_CYCLING_FINDER = 0x40100006;
    /*Description
    This property specifies whether DPM decoding is enabled during the execution of Decode.
    The property value should be set as follows:
            0: Disable DPM decoding
        1: Dotpeen DPM decoding
        2: Reflective DPM decoding
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = DPM
            Example
    *//* enable reflective DPM decoding *//*
    DecodeSet( SD_PROP_DPM_ENABLED = (void *) 0x02);
    See also
    SD_PROP_DPM_REFLECTIVE_SIZE*/
    public static final int SD_PROP_DPM_ENABLED = 0x40012903;
    /**
     * Description
     *     This property can improve decoding of reflective DPM symbols when the size is small.
     *     The property is ignored if SD_PROP_DPM_ENABLED is not set to 2 (Reflective DPM decoding).
     *     The property value should be set as follows:
     *             0: Normal reflective DPM size.
     *             1: Small reflective DPM size.
     *     Property Data Type: int
     *     Set By: Value
     *     Initial Value: 0
     *     Required Components: CORE = DPM
     *             Example
     *     /* enable small reflective DPM size
     *
        DecodeSet(SD_PROP_DPM_REFLECTIVE_SIZE = (void *) 0x01);
     *
         See also
     *SD_PROP_DPM_ENABLED
     */
    public static final int SD_PROP_DPM_REFLECTIVE_SIZE=0x40012904;
    /*Description
    This property allows to increase depth of field for linear codes with a tradeoff of longer computation time.
    0: Disable enhanced depth of field
    1: Enable enhanced depth of field
    Property Data Type: int
    Set By: Value
    Initial Value: 1
    Required Components: CORE
    Example
    /* Enable enhanced depth of field for linear codes
    SD_Set(Handle = SD_PROP_EDGE_DETECTOR = (void *) SD_CONST_ENABLED);
     */
    public static final int SD_PROP_EDGE_DETECTOR = 0x40100003;
    /*
    * Description
    This property may reduce the time it takes to issue a symbol during unattended operating mode operation.
    When enabled = this propery may reduce the overall time required to located and decode symbols within an image
    while increasing the overall time to process the entire image. This option should only be enabled when the amount
    of time prior to issuing a bar code is more important than the overall time to process an entire image buffer.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = UNOP
            Example
    SD_Set(Handle = SD_PROP_MISC_DECODE_LATENCY = (void *) SD_CONST_ENABLED);
    * */
    public static final int SD_PROP_MISC_DECODE_LATENCY = 0x40005021;


    public static final int SD_PROP_MISC_ECI_HANDLING = 0x40005018;
    /*Description
    This property specifies how ECI codewords will be handled when encoded within a symbol.
    The SD_PROP_MISC_ECI_HANDLING property is used to determine how ECI data sequences in the encoded
    data stream are handled.
    The property should be set as follows:
            0: ECI handling will be done per symbology. Each symbology has a different behavior. This option is provided to
    maintain compatibility with previous versions of the decoder.
            1: Process ECI information in the encoded data stream and include the results in the result string. All backslashes
            ('\') will be returned as a double occurrence of the backslash character ('\\').
                     2: Ignore ECI information in the encoded data stream.
                    Property Data Type: int
                     Set By: Value
                     Initial Value: 0
                     Required Components: CORE
                     Example
                     SD_Set(Handle = SD_PROP_MISC_ECI_HANDLING = (void *) SD_CONST_ECI);
    See also*/
    public static final int SD_PROP_MISC_IMPROVEMENTS = 0x40005011;
    /*Description
    This property specifies whether certain improvements should be enabled.
    The property value is a bit field defined as follow:
    b0: Enable improved positioning of the bounds for POSTNET and PLANET code symbols.
    The values returned by SD_PROP_RESULT_BOUNDS will indicate the starting and ending positions of the
    symbol with greatly improved accuracy.
            b1: Enable improved symbol locating algorithms = especially at lower sampling density.
            b2: For Code 128 = enable improved compliance with sections 2.3.4 ("Special Characters") and 2.7 ("Transmitted
    Data") of the Uniform Symbology Specification â€“ Code 128 (June 1993 edition).
    All other fields are presently reserved.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    Enable improved compliance for code 128:
    SD_Set(Handle = SD_PROP_MISC_IMPROVEMENTS = (void *) 0x04);
    See also*/
    public static final int SD_PROP_MISC_ISSUE_IDENTICAL_SPACING = 0x40005019;
    /*Description
    This property specifies the minimum distance required between identical symbols.
    Normally = during the SD_Decode or SD_ProgressiveDecode function = the customer defined callback function
    SD_CB_Result is called for each unique symbol decoded. When
    SD_PROP_MISC_ISSUE_IDENTICAL_SYMBOLS is enabled = multiple results may be issued for the same
    symbol. The SD_PROP_MISC_ISSUE_IDENTICAL_SPACING property requires a minimum distance between
    the centers before issuing a second instance of a result.
    NOTE: This property is only used when SD_PROP_MISC_ISSUE_IDENTICAL_SYMBOLS is set to
    SD_CONST_ENABLED and multiple instances of a unique symbol are decoded.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    *//* Require 300 pixels between identical results *//*
    SD_Set(Handle = SD_PROP_MISC_ISSUE_IDENTICAL_SPACING = (void *) 300);
    See also
    SD_PROP_MISC_ISSUE_IDENTICAL_SYMBOLS*/
    public static final int SD_PROP_MISC_ISSUE_IDENTICAL_SYMBOLS = 0x40005004;
    /*Description
    This property specifies whether identical symbols should be issued.
            Normally = during the SD_Decode function = the customer defined callback function SD_CB_Result is called for
    each unique symbol decoded. A symbol is considered non-unique if it has the exact same length = symbology,
    modifier = and string as a symbol already issued during this SD_Decode call.
    The property value should be set as follows:
            0: Disable issuing identical symbols.
            1: Enable issuing identical symbols.
    Note that when the property is enabled = the decoder may occasionally produce multiple identical decodes in cases
    where only one decode would be expected. This possibility increases for damaged linear symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    SD_Set(Handle = SD_PROP_MISC_ISSUE_IDENTICAL_SYMBOLS,
                   (void *) SD_CONST_ENABLED);
    See also
    SD_PROP_MISC_ISSUE_IDENTICAL_SPACING*/
    public static final int SD_PROP_MISC_LINEAR_BOUNDARY_CHECK = 0x40005026;
    /*Description
    Enables intelligent processing of quiet zones where we will allow very small quiet zones per a specific symbology
    allowing small quiet zones = but can ensure that a read cannot occur within another bar code. Used with Interleaved
2 of 5 = Code 39 = Code 128 = and GS1-Databar Limited.
            0: disabled
1: no-minimum area checked
2: requires a full specified quiet zone area to be within the image (for GS1-Databar Limited Honeywell specifies
            7X)
3: requires 2X the full specified quiet zone area to be within the image (for GS1-Databar Limited Honeywell
            specifies 14X)
(defaults to 2)
    See also*/
    public static final int SD_PROP_MISC_LINEAR_DAMAGE_IMPROVEMENTS = 0x40005025;
    /*Description
    This property may improve the decoder's ability to decode heavily damaged linear and stacked linear bar codes.
    Enabling this property may increase the average decode time.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    Example*//* Enable enhanced damage handling for linear bar codes *//*
    SD_Set(Handle = SD_PROP_MISC_LINEAR_DAMAGE_IMPROVEMENTS = (void *)1);
    See also*/
    public static final int SD_PROP_MISC_LINEAR_DECODE_POLARITY = 0x40005027;
    /*Description
    This command instructs the decoder how to handle linear codes printed with inverted polarity - i.e. light colored
    bars on a dark background.
    The property value should be set as follows:
            0: Disabled - Setting this will only decode codes that have dark bars printed on light backgrounds
    1: Enabled - Setting this will only decode codes that have light bars printed on dark backgrounds
    2: Auto-Discriminate - Setting this will handle decoding of both light codes on dark backgrounds and dark codes
    on light backgrounds. There is a very slight time penalty associated with this.
    Note: This applies only to symbologies that have been enabled per the specific symbology settings
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example*/
    /* Enable linear decode polarity auto-discriminate */
   /* SD_Set(Handle = SD_PROP_MISC_LINEAR_DECODE_POLARITY = (void *)2);
    See also*/
    public static final int SD_PROP_MISC_LOW_ASPECT_RATIO = 0x40005005;
    /*Description
    This property specifies whether a special algorithm for locating low aspect ratio 1D symbols (including PDF417
and MicroPDF417) should be used.
    This property should be enabled when the decoder must locate linear symbols (including PDF417 and
            MicroPDF417) with a low symbol height to symbol length ratio. Specifically = read rate improvements may be
    achieved for symbols with heights smaller than the length of approximately two symbol codewords.
    Enabling this property substantially increases (approximately triples) the processor cycles needed to find symbols.
    Also = since this property is not guaranteed to improve the decoder's ability to locate low aspect ratio symbols in all
    cases = a careful determination should be made that an acceptable read rate is achieved in specific customer
    circumstances.
    The property value should be set as follows:
            0: Disable the special low aspect ratio 1D symbol locating algorithm.
1: Enable the special low aspect ratio 1D symbol locating algorithm.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and any component in the Linear Symbologies Group
            Example
    SD_Set(Handle = SD_PROP_MISC_LOW_ASPECT_RATIO = (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_MISC_LOW_CONTRAST_IMPROVEMENTS = 0x40005006;
    /*Description
    This property specifies whether certain algorithms for improved locating and/or decoding of low contrast symbols
    should enabled.
    Low contrast images may occur for many reasons = including printing problems = lighting problems = motion blur,
    oblique camera angles = low sample density = etc.
    The property value is a bit field defined as follow:
    b0: Enable improved locating for low contrast linear symbols (including PDF417 and MicroPDF417) - note 1
    b1: Enable improved decoding for low contrast Interleaved 2 of 5 symbols - note 2
    b2: Enable improved decoding for low contrast QR = Maxicode and Aztec
    note 1: This algorithm should be used only if necessary to achieve improved performance. It requires significantly
    more processor cycles than the "standard" algorithm = and may therefore overload the processor = or "starve" the
    decoding of located symbols. Customers should carefully examine the processor cycle increase and determine that
    a genuine benefit has been achieved before deciding to use the "improved" algorithm.
            note 2: This algorithm should be used only to obtain better decoding performance on Interleaved 2 of 5 symbols.
    There may be slight increases in processor cycles = and slight increases in misread rates when this algorithm is
    enabled. This algorithm will often be used in conjunction with the "b0" algorithm described above = in which case,
    the property value would be set to 0x03.
    When all bits are 0 = all special low contrast algorithms are disabled (i.e. use standard algorithms)
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    SD_Set(Handle = SD_PROP_MISC_LOW_CONTRAST_IMPROVEMENTS = (void *) 0x03);
    See also*/
    public static final int SD_PROP_MISC_MAX_1D_LENGTH = 0x40005007;
    /*Description
    This property specifies the maximum expected length of 1D bar codes (including PDF417 and MicroPDF417) in
    units of millimeters.
    When operating in unattended mode = and any symbology in the Linear Symbologies Group is enabled = and the
    SD_ProgressiveDecode function is used = this property must be set appropriately.
    This property is a hint to the decoder. Symbols of more than the maximum length may sometimes be decoded and
    issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 200
    Required Components: CORE = UNOP = and any component in the Linear Symbologies group
    Example
    SD_Set(Handle = SD_PROP_MISC_MIN_1D_LENGTH,
    (void *) (int) (25.4 * 4 *//* inch *//*));
    See also*/
    public static final int SD_PROP_MISC_MIN_1D_HEIGHT = 0x40005002;
    /*Description
    This property specifies the minimum expected height of 1D bar codes (including PDF417 and MicroPDF417) in
    units of millimeters.
    When operating in unattended mode = and any symbology in the Linear Symbologies Group is enabled = this
    property must be set appropriately.
    This property is a hint to the decoder. Symbols of less than the minimum height may sometimes be decoded and
    issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 13
    Required Components: CORE = UNOP = and any component in the Linear Symbologies group
    Example
    SD_Set(Handle = SD_PROP_MISC_MIN_1D_HEIGHT,
(void *) (int) (25.4 * 1 *//* inch *//*));
    See also*/
    public static final int SD_PROP_MISC_MISENCODED_SYMBOLS = 0x40005009;
    /*Description
    This property specifies whether to issue information regarding misencoded symbols.
    For certain symbologies = it is possible to misencode a symbol. We define a misencoded symbol as a symbol where
    it is impossible to convert the symbol's codeword sequence to a text message. For example = consider the following
    scenario:
            1.The decoder locates a Data Matrix symbol.
2.The decoder locates each module in the symbol and determines whether it is black or white.
            3.The decoder groups the black and white modules into a codeword sequence.
            4.The decoder performs error correction on this codeword sequence = and determines that the codeword sequence
    does not exceed the error correction capacity of the symbol = i.e. error correction is successful.
            5.The decoder attempts to convert the message portion of the corrected codeword sequence to a text message = but
    fails upon encountering an impossible condition. For example = while in ASCII encodation mode = a codeword with
    the value 250 is encountered. But the decoding for codewords 242 through 255 is undefined in ASCII encodation
    mode and therefore = the symbol is considered misencoded.
    Misencoded symbols are caused by errors in encoding software = and in Honeywell's experience = such errors are not
    uncommon for complex symbologies such as Data Matrix and PDF417. By default = the decoder will ignore
    misencoded symbols = and will not call the SD_CB_Result callback function defined by the customer. However,
    using SD_PROP_MISENCODED_SYMBOLS = it is possible to change the default behavior so that a decode result
    is produced for a misencoded symbol. The result for a misencoded symbol is distinguished from a decoded
            (normal) symbol result by the value of the SD_PROP_RESULT_MODIFIER property = which will be a negative
    number in the range -1000 through -1999. Also = the SD_PROP_RESULT_STRING property will return a string
    containing the text message "MISENCODED SYMBOL". However = since it is possible = although unlikely = that
this string could be intentionally encoded in a symbol = the distinction between misencoded and normal symbol
    results should always be based on the modifier value.
    The following property values may be used:
    SD_CONST_DISABLED (0) - no misencoded symbol results will be issued for any symbology.
    Or = one or more3 of the following values may be summed to enable issuing misencoded symbol results for the
    corresponding symbology:
            +SD_CONST_DM: Enable issuing results for misencoded Data Matrix symbols.
            +SD_CONST_PDF: Enable issuing results for misencoded PDF417 symbols.
+SD_CONST_QR: Enable issuing results for misencoded QR symbols.
    Note that it is only meaningful to enable issuing misencoded symbol results for a symbology if the symbology
    itself is enabled. For example = if property SD_PROP_DM_ENABLED is set to the value
    SD_CONST_DISABLED = then enabling issuing misencoded Data Matrix symbols will have no effect.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    *//* enable Data Matrix decoding and enable issuing misencoded symbol results *//*
    SD_Set(Handle = SD_PROP_DM_ENABLED = (void *) SD_CONST_ENABLED +
    SD_CONST_INVERSE_ENABLED);
    SD_Set(Handle = SD_PROP_MISC_MISENCODED_SYMBOLS = (void *) (SD_CONST_DM));
    See also*/
    public static final int SD_PROP_MISC_OP_MODE = 0x40005003;
    /*Description
    This property specifies the operating mode for decoding.
    Provided the relevant components are present = the decoder supports two operating modes - manual and unattended.
    The operating mode affects how the decoder locates symbols in the image frame. Generally = customers should use
    manual mode when the image frame is produced either by a hand scanner = or by manually presenting a symbol to a
    fixed position scanner. In these systems = the magnification is generally highly variable = and the position and
    orientation of the symbol in the image is generally unpredictable. In unattended operation = the symbol is generally
    presented at a fixed magnification and may have known properties such as size = orientation = etc.
    Using unattended operation mode allows (and in many cases requires) setting additional properties describing the
    symbol image. For example = the property SD_PROP_IMAGE_TYP_DENSITY is relevant for many symbologies
            (e.g. postal symbologies) in unattended mode. Generally = when various properties of the image are predictable,
    users should consider using unattended mode decoding = which may produce some decode rate gains and often
    significantly reduces decoding time. It is sometimes appropriate to experiment with both manual and unattended
    mode to empirically determine how to obtain best performance from the decoder.
    The property may be set to the following values only:
    SD_CONST_MANOP: Manual operating mode.
            SD_CONST_UNOP: Unattended operating mode.
    Property Data Type: int
    Set By: Value
    Initial Value: The initial value is SD_CONST_MANOP = unless component MANOP is either not present or not
    authorized = in which case the initial value is SD_CONST_UNOP.
    Required Components: CORE. Also = MANOP is required to set the property to SD_CONST_MANOP and UNOP
    is required to set the property to SD_CONST_UNOP.
    Example

    SD_Set(Handle = SD_PROP_MISC_OP_MODE = (void *) SD_CONST_UNOP);
    See also
    SD_PROP_IMAGE_TYP_DENSITY*/
    public static final int SD_PROP_MISC_PASS_THROUGH = 0x40005016;
    /*Description
    This property = when enabled = optimizes the decoder for scanning systems in which bar codes are passed rapidly
    through the field-of-view. In this mode = the decoder searches the entire image for bar codes rather than focusing
    its search on the central region of the image = as it does by default.
    The decoder will = on average = take less time to process images when passthrough mode is enabled. However = it
    may not always detect high-density and / or truncated bar codes in this mode.
    The property values are:
            0: disable
1: enable
    default value: 0
    See also
    SD_PROP_CYCLING_FINDER*/
    public static final int SD_PROP_MISC_SUBREGION_PROCESSING = 0x40005010;
    /*Description
    This property specifies whether certain groups of symbologies will be processed only within defined subregions.
            Normally = in unattended processing mode = all enabled symbologies are processed over the full extent of the image.
    The properties SD_PROP_IMAGE_POINTER = SD_PROP_IMAGE_WIDTH = and SD_PROP_IMAGE_HEIGHT
    define the origin and extent of the processed image. The SD_PROP_MISC_SUBREGION_PROCESSING
    property may be used to indicate that for certain groups of symbologies = only a specified subregion of the image
    should be processed. When specified = a subregion is always a proper subregion of the image = i.e. no pixel inside
    the defined subregion is outside of the defined image. Subregion processing can save processor cycles that would
    otherwise be wasted on locating and decoding symbols in "don't care" portions of an image. Although it may be
    tempting = Honeywell strongly recommends that processing subregions not be defined too aggressively (i.e. small)
    because in many systems symbols frequently appear outside of their expected bounds (i.e. specifications are often
            disobeyed).
    The following property values may be used:
    SD_CONST_DISABLED (0) - subregion processing is disabled and therefore = any and all enabled symbologies
    are processed over the entire image.
    Or = one or more of the following values may be summed to disable processing over the entire image for the
    specified group of symbologies = and instead = process that group of symbologies over a defined subregion:
    August 28 = 2019 - Page 12Honeywell - ID decoder Properties
+1: Subregion processing for postal symbologies is enabled = and the postal symbology processing subregion will
    be specified using the following properties:
            - SD_PROP_POSTAL_SUBREGION_LEFT
- SD_PROP_POSTAL_SUBREGION_TOP
- SD_PROP_POSTAL_SUBREGION_WIDTH
- SD_PROP_POSTAL_SUBREGION_HEIGHT
+2: Subregion processing for linear symbologies (including PDF417 and MicroPDF417) is enabled = and the linear
    symbology processing subregion will be specified using the following properties:
            - SD_PROP_LINEAR_SUBREGION_LEFT
- SD_PROP_LINEAR_SUBREGION_TOP
- SD_PROP_LINEAR_SUBREGION_WIDTH
- SD_PROP_LINEAR_SUBREGION_HEIGHT
+4: Subregion processing for Data Matrix (1) symbols is enabled = and the Data Matrix symbology processing
    subregion will be specified using the following properties:
            - SD_PROP_DM_SUBREGION_LEFT
- SD_PROP_DM_SUBREGION_TOP
- SD_PROP_DM_SUBREGION_WIDTH
- SD_PROP_DM_SUBREGION_HEIGHT
            (1): Data Matrix is the sole member of its symbology group.
    Note that if a particular symbology is not enabled = then no processing for that symbology will take place,
    regardless of this property's value. Also = this property is ignored in manual operating mode.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = UNOP
            Example
    *//* Enable defined subregion processing for postal and Data Matrix symbols = but process any and all other enabled
    symbologies over the entire image.
    Note: SD_PROP_POSTAL_SUBREGION_*** and SD_PROP_DM_SUBREGION_*** must also be set correctly
    to define the processing subregions *//*
    SD_Set(Handle = SD_PROP_MISC_SUBREGION_PROCESSING = (void *) 5);
    See also
    SD_PROP_POSTAL_SUBREGION_LEFT
            SD_PROP_POSTAL_SUBREGION_TOP
    SD_PROP_POSTAL_SUBREGION_WIDTH
            SD_PROP_POSTAL_SUBREGION_HEIGHT
    SD_PROP_LINEAR_SUBREGION_LEFT
            SD_PROP_LINEAR_SUBREGION_TOP
    SD_PROP_LINEAR_SUBREGION_WIDTH
            SD_PROP_LINEAR_SUBREGION_HEIGHT
    SD_PROP_DM_SUBREGION_LEFT
            SD_PROP_DM_SUBREGION_TOP
    SD_PROP_DM_SUBREGION_WIDTH
            SD_PROP_DM_SUBREGION_HEIGHT*/
    public static final int SD_PROP_MISC_TAG = 0x40005001;
    /*Description
    This property specifies a customer-defined value.
    The customer may use this property to associate a customer-defined value with the decoder object. When used = this
    property is typically assigned immediately after the object is created. The property is most often used to either
    point to customer-specific per-object data = or as an integer index into a customer-defined array of
    customer-specific per-object data. The property is typically read during the execution of a the decoder Callback
    Function to determine which customer-specific data is associated with the object causing the callback.
    While the property data type is defined as void * and the property is defined as set by reference = the example below
    shows the proper method of assigning and retrieving an integer index as the property value.
    Property Data Type: void *
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example*/

    public static final int SD_PROP_MISC_UNDECODABLE_SYMBOLS = 0x40005008;
    /*Description
    This property specifies whether to issue information regarding undecodable symbols.
    The decoder can sometimes detect the presence of a symbol = even though it is unable to successfully decode it.
    Usually = a symbol is undecodable because it is damaged = or only a partial image of the symbol has been obtained.
    The decoder can issue some information about such symbols = even though they are undecodable. For example = The
    decoder can determine (approximately) the bounds of the symbol = and its symbology. Normally = no result is issued
for undecodable symbols. However = the SD_PROP_MISC_UNDECODABLE_SYMBOLS property may be used
    to select certain symbologies for which undecodable symbol results will be issued.
    The following property values may be used:
    SD_CONST_DISABLED (0) - no undecodable symbol results will be issued for any of the symbologies listed
            below
    Or = one or more of the following values may be summed to enable issuing undecodable symbol results for the
    corresponding symbology:
    b3: (+SD_CONST_UNDECODABLE_POSTAL) Enable issuing results for undecodable postal symbols. See the
    discussion below regarding the undecodable postal symbol detection region.
            b4: (+SD_CONST_C128) Enable issuing results for undecodable Code 128 symbols.
            b5: (+SD_CONST_C39) Enable issuing results for undecodable Code 39 symbols.
            b6: (+SD_CONST_CB) Enable issuing results for undecodable Codabar symbols.
            b9: (+SD_CONST_I25) Enable issuing results for undecodable Interleaved 2 of 5 symbols.
            b12: (+SD_CONST_PDF) Enable issuing results for undecodable PDF417 symbols.
            b28: (+SD_CONST_C11) Enable issuing results for undecodable Code 11 symbols.
    Note that it is only meaningful to enable issuing undecodable results for a symbology if the symbology itself is
    enabled. For example = if SD_PROP_C128_ENABLED is set to SD_CONST_DISABLED = then enabling issuing
    undecodable Code 128 symbols will have no effect.
    Each undecodable symbol detected causes the decoder to call the SD_CB_Result callback function defined by the
    customer. From within this callback function = customer-supplied software may use SD_Get to get the values of the
    following result properties = which are defined for undecodable symbols4:
    SD_PROP_RESULT_BOUNDS: The value of this property indicates the approximate bounds of the undecoded
    symbol. Note that especially for damaged symbols = the bounds may not be determined with great accuracy. Note
    that for undecodable postal symbols = the reported bounds may be rotated either 0 or 180 degrees relative to the
"true" symbol orientation because the decoder is not able to reliably determine the "starting end" of an
    undecodable postal symbol.
            SD_PROP_RESULT_CENTER: The value of this property indicates the approximate center of the undecoded
    symbol. Note that especially for damaged symbols = the center may not be determined with great accuracy.
    SD_PROP_RESULT_LENGTH: The value of this property will be -1 for all undecodable symbol results.
    Generally = in the SD_CB_Result callback function implementation = this property should be read first to determine
    whether the decoder is issuing a decodable (i.e. normal) or undecodable symbol result.
    SD_PROP_RESULT_SYMBOLOGY: The value of this property indicates the symbology type of the undecoded
    symbol. For an undecodable postal symbol = the specific postal symbology is not determined = and the value of this
    property will be SD_CONST_UNDECODABLE_POSTAL(see note).
    SD_PROP_RESULT_QUALITY: The value of this property indicates the likelihood that an undecodable symbol
    has been correctly detected = as opposed to a "false alarm".
    Note: checking that the SD_PROP_RESULT_LENGTH property value is -1 is the only correct way of determining
    that the result is for an undecodable symbol.
    Undecodable symbol results are issued just before the end of frame processing = and always after all decodable (i.e.
                                                                                                                           normal) symbol results are issued. Undecodable symbol results = if any = are issued even if the value of the
    SD_PROP_PROGRESS_CANCEL property has been set to SD_CONST_PROGRESS_CANCEL.
    The decoder is not guaranteed to detect every undecodable symbol. Likewise = the decoder may infrequently falsely
    detect an undecodable symbol. False undecodable postal symbol detections are more frequent than undecodable
    symbol detections for linear symbologies.
    Undecodable postal symbols are only detected within a region defined by the properties
            SD_PROP_POSTAL_UNDECODABLE_LEFT = SD_PROP_POSTAL_UNDECODABLE_TOP,
    SD_PROP_POSTAL_UNDECODABLE_WIDTH = and SD_PROP_POSTAL_UNDECODABLE_HEIGHT. This
    region definition is independent of the setting of SD_PROP_IMAGE_MIRRORED. If the center of a successfully
    decoded postal symbol (as specified by SD_PROP_RESULT_CENTER) is within the defined region = then no
    undecodable postal symbols will be issued for the image. At most one undecodable postal symbol
    result will be issued for the image = corresponding to the undecodable postal symbol having highest quality metric
            (as specified by SD_PROP_RESULT_QUALITY) within the defined region.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example*/
    /* enable Code 39 and Code 128 decoding and enable issuing undecodable symbol results */
    /*SD_Set(Handle = SD_PROP_C39_ENABLED = (void *) SD_CONST_ENABLED);
    SD_Set(Handle = SD_PROP_C128_ENABLED = (void *) SD_CONST_ENABLED);
    SD_Set(Handle = SD_PROP_MISC_UNDECODABLE_SYMBOLS = (void *) (SD_CONST_C39 +
    SD_CONST_C128);
    See also
    SD_PROP_MISC_UNDECODABLE_SYMBOLS_EX*/
    public static final int SD_PROP_MISC_UNDECODABLE_SYMBOLS_EX = 0x40005014;
    /*Description
    This property extends the SD_PROP_MISC_UNDECODABLE_SYMBOLS property by 32 bits to provide
    expansion room for new symbologies.
    The following property value may be used:
    SD_CONST_DISABLED (0) - no undecodable symbol results will be issued for any of the symbologies listed
            below
    Or = one or more of the following values may be summed to enable issuing undecodable symbol results for the
    corresponding symbology:
            +SD_CONST_M25: Enable issuing results for undecodable Matrix 2 of 5 symbols.
+SD_CONST_NEC25: Enable issuing results for undecodable NEC 2 of 5 symbols.
+SD_CONST_TP: Enable issuing results for undecodable Telepen symbols.
+SD_CONST_TRIOPTIC: Enable issuing results for undecodable Trioptic Code 39 symbols.
    In all other respects = the operation of this property is identical to the operation of
    SD_PROP_MISC_UNDECODABLE_SYMBOLS.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    *//* enable Matrix 2 of 5 decoding and enable issuing undecodable symbol results *//*
    SD_Set(Handle = SD_PROP_M25_ENABLED = (void *) SD_CONST_ENABLED);
    SD_Set(Handle = SD_PROP_MISC_UNDECODABLE_SYMBOLS_EX = (void *) (SD_CONST_M25);
    See also
    SD_PROP_MISC_UNDECODABLE_SYMBOLS*/
    public static final int SD_PROP_ROI_MODE = 0x40008015;
    /*Description
    When set = only the region(s) of interest is(are) processed by the decoder.
            0: Disable(default value). The entire original image is sent to the Decoder.
1: Standard
- Use the aimer position to weight activity.
- Activity calculated on the row and the column in the middle of each cell.
            - The ROI window may not include the aimer.
            - This is the default mode recommended for hand held scanner when used in Manual Trigger mode.
2: Standard = aimer centered.
            - Activity calculated on the row and the column in the middle of each cell.
            - The ROI window will always include the aimer.
            - This is the default mode recommended for Andaman.
3: DPM = aimer centered.
            - Activity calculated on 4 rows and 2 columns in each cell.
            - The ROI window will always include the aimer.
4: Kiosk/presentation application.
            - Ignore aimer position = no weight activity.
- Activity calculated on the row and the column in the middle of each cell.
            - The ROI window may not include the aimer.
            - This is the mode recommended for hand held scanner when used in Presentation mode.
    NB : only DPM ROI mode could be used when DPM feature is active.
    See also*/
    public static final int SD_PROP_SUBPIXEL_FINDER = 0x40100004;
    /*Description
    This property enables the decoder to find bar code symbols that have a very small element size in pixels. The
    small element size may be due to the printing of a symbol or due to it being imaged from far away. Enabling this
    property may increase the average time to decode.
    This property only affects the following symbologies:
            * Code 39
            * Codablock A
            * Code 128
            * UPC-A
            * UPC-E
            * EAN-13
            * EAN-8
            * PDF417
    The property values are:
            0: disable
            1: enable
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    *//* enable sub-pixel finding algorithms for select symbologies *//*
    SD_Set(Handle = SD_PROP_SUBPIXEL_FINDER = (void *)1);
    See also*/
    public static final int SD_PROP_USE_DISTANCE_MAP = 0x40100002;
    /*Description
    This enables an optimization for MLD decoding where instead of doing a straight linear search = a tree type of
    search is used. Although the benefit is typically orders of magnitude speed improvement = it does introduce a slight
    possibility of failing to find the right answer.
    The property value is a bit field defined as follows:
    b2: Enable Distance Maps for UPC
    b4: Enable Distance Maps for Code 128
    b5: Enable Distance Maps for Code 39
    Property Data Type: int
    Set By: Value
    Initial Value: 0x34 (Use distance maps for UPC = Code 128 = and Code 39)
    Required Components: CORE and supported symbologies
    Example
    SD_Set(Handle = SD_PROP_USE_DISTANCE_MAP = (void *) 0x34); // Enable Distance Maps for UPC = Code 128
    and Code 39
    See also
    SD_PROP_USE_MLD*/
    public static final int SD_PROP_USE_MLD = 0x40100001;
    /*Description
    This setting is a bitmask used to enable a probability based decoding algorithm = known as "Maximum Likelihood
    Decoding" or MLD = for symbologies that support it. Typically = this decoding algorithm offers better performance
    and is better able to handle print anomalies and/or damage.
    b2: Enable MLD for UPC
    b4: Enable MLD for Code 128
    b5: Enable MLD for Code 39
    Property Data Type: int
    Set By: Value
    Initial Value: 0x34 (MLD enabled for UPC = Code 128 = and Code 39)
    Required Components: CORE and supported symbologies
    Example
    SD_Set(Handle = SD_PROP_USE_MLD = (void *) 0x34); // Enable MLD for UPC = Code 128 and Code 39
    See also
            SD_PROP_USE_DISTANCE_MAP*/
    public static final int SD_PROP_USE_VESTA = 0x40100012;
    /*Description
    This property specifies whether Vesta decoding (extended range) is enabled during the execution of Decode.
    The property value should be set as follows:
            0x00: Disable vesta decoding for all symbologies
            0x20: Enable Code 39 vesta decoding
    Initial value: 0x20
    See also*/
    //2. Result properties
    public static final int SD_PROP_RESULT_2D_MODULESX = 0x40007020;
    /*Description
    This property value indicates the total number of modules present in a 2D symbol along its X axis.
    The X axis is typically associated with the width of a symbol. To fully describe rectangular 2D symbols,
    SD_PROP_RESULT_2D_MODULESY is provided as well. For square 2D symbols = the x and y module
    properties will contain the same value.
    This property is currently defined for the following symbologies:
            - Aztec Code
            - Data Matrix
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only for
    the symbologies listed above. Using SD_Get to retrieve this property at any other time causes error
    SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE
            Example
    int ModulesOnASide;
    SD_Get(Handle = SD_PROP_RESULT_2D_MODULESX = &ModulesOnASide);
    See also*/
    public static final int SD_PROP_RESULT_2D_MODULESY = 0x40007021;
    /*Description
    This property value indicates the total number of modules present in a 2D symbol along its Y axis.
    The Y axis is typically associated with the height of a symbol. To fully describe rectangular 2D symbols,
    SD_PROP_RESULT_2D_MODULESX is provided as well. For square 2D symbols = the x and y module
    properties will contain the same value.
    This property is currently defined for the following symbologies:
            - Aztec Code
            - Data Matrix
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only for
    the symbologies listed above. Using SD_Get to retrieve this property at any other time causes error
    SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE
            Example
    int ModulesOnASide;
    SD_Get(Handle = SD_PROP_RESULT_2D_MODULESY = &ModulesOnASide);
    See also*/
    public static final int SD_PROP_RESULT_BOUNDS = 0x70007001;
    /*Description
    This property indicates the vertices of a quadrilateral that approximately bounds the decoded symbol.
    The order of the array elements (i.e. points) is as follows:
            0: top left
            1: top right
            2: bottom right
            3: bottom left
    Note that these designations (e.g. top left) refer to the corners of the symbol = not as it appears in a particular image,
    but rather as it appears (most often) in its symbology specification. For example = for Data Matrix = array element 3,
    which contains the coordinates of the bottom left vertex of the symbol boundary = will always be proximate the
    intersection of the two lines which form the "L" of the symbol = regardless of the actual orientation (or mirroring) of
    the symbol in the image submitted to the decoder. This enables customer-supplied software to locate other image
    features that have a known position relative to a printed symbol = regardless of the symbol's orientation.
    On UPC/EAN/JAN symbols with a decoded supplemental symbol = the property value indicates the approximate
    bounds of the supplemental symbol.
    Property Data Type: SD_STRUCT_BOUNDS (see file SD.H for the definition of this type)
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    SD_STRUCT_BOUNDS Bounds;
    SD_Get(Handle = SD_PROP_RESULT_BOUNDS = &Bounds);
    printf("Top Left = (%d,%d)
           Top Right = (%d,%d)
            Bottom Right = (%d,%d)
                Bottom Left = (%d,%d)",
            Bounds.Point[0].X = Bounds.Point[0].Y,
            Bounds.Point[1].X = Bounds.Point[1].Y,
            Bounds.Point[2].X = Bounds.Point[2].Y,
            Bounds.Point[3].X = Bounds.Point[3].Y);
        See also*/
    public static final int SD_PROP_RESULT_CENTER = 0x70007002;
    /*Description
    This property indicates the coordinates of a point at the approximate center of the decoded symbol.
    On UPC/EAN/JAN symbols with a decoded supplemental symbol = the property value indicates the approximate
    center of the supplemental symbol.
    Property Data Type: SD_STRUCT_POINT (see file SD.H for the definition of this type)
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    SD_STRUCT_POINT Center;
    SD_Get(Handle = SD_PROP_RESULT_CENTER = &Center);
    printf("Center = (%d,%d)\n" = Center.X = Center.Y);
    See also*/
    public static final int SD_PROP_RESULT_CODABLOCK_CONFLICT = 0x40007015;
    /*Description
    This property value indicates whether this symbol may be part of a Codablock A or Codablock F.
    This property is valid only when the SD_PROP_RESULT_SYMBOLOGY value is either SD_CONST_C128 or
    SD_CONST_C39. This property will contain SD_CONST_TRUE if the symbol being issued may be part of a
    Codablock A or Codablock F symbol. Symbols issued when this property contains SD_CONST_TRUE can be
    suppressed by enabling SD_PROP_C128_SUPPRESS_CB_CONFLICT when issuing a Code 128 symbol or
    SD_PROP_C39_SUPPRESS_CB_CONFLICT when issuing a Code 39 symbol.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only
    when the symbology is SD_CONST_C128 or SD_CONST_C39. Using SD_Get to retrieve this property at any
    other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE = C128 = C39
            Example
    int CodablockConflict;
    SD_Get(Handle = SD_PROP_RESULT_CODABLOCK_CONFLICT = &CodablockConflict);
    See also*/
    public static final int SD_PROP_RESULT_CODEWORDS = 0x40007023;
    /*Description
    This property value is an array of integers containing codewords read from a decoded symbol.
    This property is supported for some 1D and 2D symbologies (Datamatrix = Code 128 = QR code ...). Error correction
    codewords will not be included. Ensure that the appropriate amount of memory is allocated before reading this
    property. The amount of memory needed is determined by calling SD_PROP_RESULT_CODEWORD_COUNT.
    If the returned count is zero = then the propery is not supported for the current symbology
    Property Data Type: int *
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE
            Example
    int CodewordCount;
    int *Codewords;
    SD_Get(Handle = SD_PROP_RESULT_CODEWORD_COUNT = &CodewordCount);
    Codewords = malloc(CodewordCount * sizeof(int));
    SD_Get(Handle = SD_PROP_RESULT_CODEWORDS = Codewords);
    See also
    SD_PROP_RESULT_CODEWORD_COUNT*/
    public static final int SD_PROP_RESULT_CODEWORD_COUNT = 0x40007024;
    /*Description
    This property indicates the number of codewords read in a decoded symbol.
    This property is supported for some 1D and 2D symbologies (Datamatrix = Code128 = QRcode ...). For any non
    supported symbology = this value will be zero. Error correction codewords are not included.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE
            Example
    int CodewordCount;
    SD_Get(Handle = SD_PROP_RESULT_CODEWORD_COUNT = &CodewordCount);
    See also
    SD_PROP_RESULT_CODEWORDS*/
    public static final int SD_PROP_RESULT_LENGTH = 0x40007004;
    /*Description
    This property indicates the length (in bytes) of the decode result string.
    There is no terminator character for a result string = and thus = no terminator character is included in the length. For
    undecodable symbol results (if they have been enabled using
            SD_PROP_MISC_UNDECODABLE_SYMBOLS) = the property value will be -1.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    int Length;
    SD_Get(Handle = SD_PROP_RESULT_LENGTH = &Length);
    See also*/
    public static final int SD_PROP_RESULT_LINKAGE = 0x40007005;
    /*Description
    This property value indicates whether this symbol is a linked component of a composite symbol.
    This property is valid only when the SD_PROP_RESULT_SYMBOLOGY value is one of the following:
    SD_CONST_C128
            SD_CONST_C39
    SD_CONST_PDF
            SD_CONST_RSS_14
    SD_CONST_RSS_14_ST
            SD_CONST_RSS_14_LIM
    SD_CONST_RSS_EXP
            SD_CONST_RSS_EXP_ST
    The property has the following values:
    SD_CONST_NOT_LINKED: The symbol is not a linked component of a composite symbol.
            SD_CONST_LINKED_C128: The symbol is the 1-D Code 128 component of a composite symbol.
            SD_CONST_LINKED_C39: The symbol is the 1-D Code 39 component of a composite symbol.
            SD_CONST_LINKED_RSS: The symbol is the 1-D RSS component of a composite symbol.
            SD_CONST_LINKED_CC_A: The symbol is the 2-D CC-A component of a composite symbol.
    SD_CONST_LINKED_CC_B: The symbol is the 2-D CC-B component of a composite symbol.
    SD_CONST_LINKED_CC_C: The symbol is the 2-D CC-C component of a composite symbol.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only
    when the symbology is one of the symbologies mentioned in the remarks above. Using SD_Get to retrieve this
    property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    int Linkage;
    SD_Get(Handle = SD_PROP_RESULT_LINKAGE = &Linkage);
    See also*/
    public static final int SD_PROP_RESULT_MODIFIER = 0x40007006;
    /*Description
    This property value is normally equal to AIM/AIMI symbology modifier that is transmitted as the last (i.e. third)
    character of a symbology identifier preamble as described in International Technical Specification - Symbology
    Identifiers. The exceptions are as follows:
    For Matrix 2-of-5 symbols:
            '2': Checksum not checked
            '3': Checksum checked and included in output string
            '4': Checksum checked and stripped from output string
                For NEC 2-of-5 symbols:
                        '5': Checksum not checked
            '6': Checksum checked and included in output string
            '7': Checksum checked and stripped from output string
                For postal symbologies:
                        '0': POSTNET
            '1': Japan Post
            '2': Australia Post
            '3': PLANET
            '4': Royal Mail
            '5': KIX Code
            '6': UPU (57-bar) see note
            '7': UPU (75-bar)
            '8': USPS 4CB
            '9': Royal Mail InfoMail A
            'A': Royal Mail InfoMail B
            'D': Royal Mail EIB Barcode S
            'E': Royal Mail EIB Barcode C
            'F': Royal Mail EIB Barcode L
    Note: In certain instances = a 75-bar UPU symbol may be reported as a 57-bar UPU symbol = depending on the
    number of missing or damaged bars in the symbol. For example = when more than 12 end bars of a 75-bar UPU are
    obscured (as might happen with a windowed envelope) = a 57-bar decode may still be extracted in some cases.
    For UPC/EAN symbols:
            'A': UPC A
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
            '4': EAN/JAN13 with Five Character
    For MaxiCode Mode 0 symbols = there is no AIMI defined modifier. The decoder uses the modifier 'Z'.
    For Data Matrix = if the symbol's first codeword is the "Reader Programming character" (codeword 234 decimal),
    then the modifier will have the non-standard value 'R'.
    For Code 128 symbols = the modifier will follow the AIM specification (June 1993 edition) if the bit b2 is set in
    SD_PROP_MISC_IMPROVEMENTS = but refer also to Code 128 Additional Information = for additional
    information about the FNC1-FNC4 symbols.
    For Pharmacode symbols = the modifier will always be '1'.
    For Codablock A symbols = modifier 6 is supported.
    For Codablock F symbols = modifiers 4 and 5 are supported. Also = when a FNC2 is encountered during decoding,
            2^28 will be added to the modifier value. When a FNC3 is encountered during decoding = 2^29 will be added to the
    modifier value.
    For Trioptic Code 39 = the modifier will always be '8'.
    For Hong Kong 2 of 5 = the modifier will always be '9'.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    int Modifier;
    SD_Get(Handle = SD_PROP_RESULT_MODIFIER = &Modifier);
    See also
    SD_PROP_RESULT_MODIFIER_EX*/
    public static final int SD_PROP_RESULT_MODIFIER_EX = 0x40007019;
    /*Description
    This property value indicates information about the type of bar code decoded in addition to the information
    provided in the AIM/AIMI symbology modifier returned in the SD_PROP_RESULT_MODIFIER property.
    One or more of the following values may be summed to return extended information about the decoded symbol:
    For Code 128 = Code 39 = or Interleaved 2 of 5 symbols:
            0: Full symbol is decoded
            +1: Partial left-half symbol issued
            +2: Partial right-half symbol issued
                For QR Code or MicroQR Code symbols:
                        +1: MicroQR Code
                For PDF417 or MicroPDF417 symbols:
            +1 MicroPDF417
            +2 Reader Initialization Symbol
            +4 Macro
    For OCR symbols templates must be defined that tell the OCR algorithms what kind of OCR text to look for.
    Multiple templates may be active at the same time. This property tells the user which template was used to produce
    the current output.
    The following values are defined:
            0: User Template
            1: Passport Lower Row
            2: Passport Upper Row
            3: ISBN
            4: Price Code
            5: MICR E-13B
            6: Format-A Visa Upper Row
            7: Format-A Visa Lower Row
            8: Format-B Visa Upper Row
            9: Format-B Visa Lower Row
            10: TD-2 Travel Document Upper Row
            11: TD-2 Travel Document Lower Row
            12: TD-1 Travel Document Upper Row
            13: TD-1 Travel Document Middle Row
            14: TD-1 Travel Document Lower Row
            15: French National ID Upper Row
            16: French National ID Lower Row
            17: Russian National ID Upper Row
            18: Russian National ID Lower Row
    In instances where OCR data may satisfy multiple active templates = the first active template seen starting from the
    top of the above table and working down will be associated with the output result.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE
            Example
    int ModifierEX;
    SD_Get(Handle = SD_PROP_RESULT_MODIFIER_EX = &ModifierEX);
    See also
    SD_PROP_RESULT_MODIFIER*/
    public static final int SD_PROP_RESULT_POSTAL_BARS_ANALYZED = 0x40007013;
    /*Description
    This property value indicates the number of bar positions analyzed to decode a POSTNET or PLANET code postal
    symbol.
            Typically = the number of bar positions analyzed is approximately ten greater than the number of bars in the actual
    symbol. For instance = for a 52 bar "C-field" POSTNET symbol = the property value is typically about 62.
    However = when the image area preceding and/or following the symbol is not quiet = the number of bar positions
    analyzed may be significantly greater. In some cases = when the number of bar positions analyzed is unusually high,
    there is a slightly increased likelihood that a symbol has been misdecoded.
    Note that misdecodings are especially rare on other than "A-field" 32 bar POSTNET symbols.
    Although Honeywell generally recommends against using this property = in some instances it may be used to "trap"
    possibly misdecoded symbols in systems where misdecodes are especially costly. Even then = Honeywell suggests
    only trapping 32 bar POSTNET symbols. When such trapping is implemented = it must be understood that some
    correct decodes may be sacrificed. In other words = implementing such a trap will inevitably cause some correctly
    decoded symbols to be falsely treated as misdecodes. Because misdecodes are statistically extremely rare it is
    difficult to estimate the value of attempting to trap them except by empirical side-by-side testing on millions of
    images = with the results evaluated against an accurate cost model of the value of a correct decode versus the cost of
    a misdecode.
    For decoded symbols other than POSTNET or PLANET = the property value is 0.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and either PN or PL or both
    Example
    int Bars;
    SD_Get(Handle = SD_PROP_RESULT_POSTAL_BARS_ANALYZED = &Bars);
    See also*/
    public static final int SD_PROP_RESULT_QRPARITY = 0x40007010;
    /*Description
    This property value indicates the "parity" of this QR Code symbol = if it belongs to a structured append sequence.
    This property is only valid for QR Code decodes (i.e. the value of SD_PROP_RESULT_SYMBOLOGY is
            SD_CONST_QR). If the symbol is not part of a structured append sequence then the value is 0. If it is part of a
    structured append sequence = then the value ranges from 0 to 255 indicating the "parity" of the symbol. For
    additional information on structured append = refer to The International Symbology Specification - QR Code,
    Section 6 = beginning on page 74.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only for
    a decoded QR Code symbol. Using SD_Get to retrieve this property at any other time causes error
    SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE = QR
            Example
    int QRParity;
    SD_Get(Handle = SD_PROP_RESULT_QR_PARITY = &QRParity);
    See also*/
    public static final int SD_PROP_RESULT_QRPOSITION = 0x40007008;
    /*Description
    This property value indicates the position of this QR Code symbol in a structured append sequence.
    This property is only valid for QR Code decodes (i.e. the value of SD_PROP_RESULT_SYMBOLOGY is
            SD_CONST_QR). If the symbol is not part of a structured append sequence then the value is 0. If it is part of a
    structured append sequence = then the value ranges from 1 to 16 = indicating its position in the sequence. For
    additional information on structured append = refer to The International Symbology Specification - QR Code,
    Section 6 = beginning on page 74.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only for
    a decoded QR Code symbol. Using SD_Get to retrieve this property at any other time causes error
    SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE = QR
            Example
    int QRPosition;
    SD_Get(Handle = SD_PROP_RESULT_QR_POSITION = &QRPosition);
    See also*/
    public static final int SD_PROP_RESULT_QRTOTAL = 0x40007009;
    /*Description
    This property value indicates the total number of QR Code symbols in a structured append sequence.
    This property is only valid for QR Code decodes (i.e. the value of SD_PROP_RESULT_SYMBOLOGY is
            SD_CONST_QR). If the symbol is not part of a structured append sequence then the value is 0. If it is part of a
    structured append sequence = then the value ranges from 1 to 16 = indicating the total number of symbols in the
    sequence. For additional information on structured append = refer to The International Symbology Specification -
    QR Code = Section 6 = beginning on page 74.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result = and then = only for
    a decoded QR Code symbol. Using SD_Get to retrieve this property at any other time causes error
    SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE = QR
            Example
    int QRTotal;
    SD_Get(Handle = SD_PROP_RESULT_QR_TOTAL = &QRTotal);
    See also*/
    public static final int SD_PROP_RESULT_STRING = 0x80007007;
    /*Description
    This property value is the decoded message string of the symbol.
    In some symbologies = the message string may contain the ASCII value 0 which is normally considered a string
    terminator in the 'C' language = but should not be considered a terminator of this property's value. Rather = the
    SD_PROP_RESULT_LENGTH property should determine the storage required for the string and the location of
    the end of the string.
    Unlike the original decoder API = the result string for API Level 2 does not contain the AIM/AIMI standard
    symbology identifier preamble. However = this preamble can be easily generated by customer-supplied software
    from the property values for SD_PROP_RESULT_SYMBOLOGY and SD_PROP_RESULT_MODIFIER.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
            Example
    int Length;
    char *String
    SD_Get(Handle = SD_PROP_RESULT_LENGTH = &Length);
    String = malloc(Length);
    SD_Get(Handle = SD_PROP_RESULT_STRING = String);
    See also
    SD_PROP_RESULT_SYMBOLOGY
            SD_PROP_RESULT_SYMBOLOGY_EX*/
    public static final int SD_PROP_RESULT_SYMBOLOGY = 0x40007003;
    /*Description
    This property indicates the symbology type of a decoded symbol.
    The following symbology constants are defined:
            - 0: None from the list belowâ€”see = however = SD_PROP_RESULT_SYMBOLOGY_EX
            - SD_CONST_AP: Australia Post
            - SD_CONST_AZ: Aztec Code
            - SD_CONST_CB: Codabar
            - SD_CONST_CODABLOCK_A: Codablock A
            - SD_CONST_CODABLOCK_F: Codablock F
            - SD_CONST_C11: Code 11
            - SD_CONST_C128: Code 128
            - SD_CONST_C39: Code 39
            - SD_CONST_C93: Code 93
            - SD_CONST_DM: Data Matrix
            - SD_CONST_I25: Interleaved 2 of 5
                        - SD_CONST_JP: Japan Post
            - SD_CONST_KIX: KIX Code
            - SD_CONST_MC: MaxiCode
            - SD_CONST_MSIP: MSI Plessey
            - SD_CONST_PDF: PDF417 or MicroPDF417 (including the 2D Composite Components CC-A = CC-B = and
                        CC-C)
            - SD_CONST_PHARMA: Pharmacode
            - SD_CONST_PL: PLANET
            - SD_CONST_PN: POSTNET
            - SD_CONST_QR: QR Code or Micro QR Code
            - SD_CONST_RM: Royal Mail 4 State Customer Code (British Post)
            - SD_CONST_RSS_14: RSS-14 or RSS-14 Truncated
            - SD_CONST_RSS_14_ST: RSS-14 Stacked or RSS-14 Stacked Omnidirectional
            - SD_CONST_RSS_EXP: RSS Expanded
            - SD_CONST_RSS_EXP_ST: RSS Expanded Stacked
            - SD_CONST_RSS_EXP_STR: RSS Limited
            - SD_CONST_S25_2SS: Straight 2 of 5 (with two bar start/stop codes)
                        - SD_CONST_S25_3SS: Straight 2 of 5 (with three bar start/stop codes)
                        - SD_CONST_UPC: UPC/EAN/JAN
            - SD_CONST_UPU: UPU
    Note that when the property value read back is zero = then the actual symbology decoded is indicated by the
    SD_PROP_RESULT_SYMBOLOGY_EX property.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example*/
    /*static void SD_CB_Result(int Handle)
    {
        int Symbology;
        *//* Determine the symbology *//*
        SD_Get(Handle = SD_PROP_RESULT_SYMBOLOGY = &Symbology);
        switch (Symbology)
        {
            case 0: *//* it must be on the _EX property *//*
                SD_Get(Handle = SD_PROP_RESULT_SYMBOLOGY_EX = &Symbology);
                Switch (Symbology)
            {
                case 0:
                    *//* should not happen *//*
                    break;
                case SD_CONST_M25: . .
            }
            break;
            case SD_CONST_AP:
    }
    See also
    SD_PROP_RESULT_SYMBOLOGY_EX*/
    public static final int SD_PROP_RESULT_SYMBOLOGY_EX = 0x40007014;
    /*Description
    This property extends the SD_PROP_RESULT_SYMBOLOGY property by 32 bits to provide expansion room for
    new symbologies.
            The following symbology constants are defined:
        - 0: None from the list below - see = however = SD_PROP_RESULT_SYMBOLOGY
        - SD_CONST_HK25: Hong Kong 2 of 5
        - SD_CONST_M25: Matrix 2 of 5
        - SD_CONST_NEC25: NEC 2 of 5
        - SD_CONST_OCR: OCR
        - SD_CONST_TP: Telepen
        - SD_CONST_TRIOPTIC: Trioptic Code 39
        - SD_CONST_INFOMAIL: Royal Mail InfoMail
        - SD_CONST_KP: Korea Post
    - SD_CONST_EIB: Royal Mail EIB Barcode
    - SD_CONST_BZ4: Brazilian 4 State
    Note that when the property value read back is zero = then the actual symbology decoded is indicated by the
    SD_PROP_RESULT_SYMBOLOGY property.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
        Initial Value: Not applicable. The property is only valid during the execution of SD_CB_Result. Using SD_Get to
    retrieve this property at any other time causes error SD_ERR_PROPERTY_LIFETIME.
    Required Components: CORE and at least one Symbologies Group component
    Example
    static void SD_CB_Result(int Handle)
    {
        int Symbology;
        *//* Determine the symbology *//*
        SD_Get(Handle = SD_PROP_RESULT_SYMBOLOGY = &Symbology);
        switch (Symbology)
        {
            case 0: *//* it must be on the _EX property *//*
                SD_Get(Handle = SD_PROP_RESULT_SYMBOLOGY_EX = &Symbology);
                Switch (Symbology)
            {
                case 0:
                    *//* should not happen *//*
                    break;
                case SD_CONST_M25: . .
            }
            break;
            case SD_CONST_AP:
        }
        See also
        SD_PROP_RESULT_SYMBOLOGY*/
        //3. OCR properties
    public static final int SD_PROP_OCR_ACTIVE_TEMPLATES = 0x40012303;
    /*Description
    This property specifies which templates are active during the current OCR decode cycle.
        The property value is a bit field defined as follows:
    b0: User
    b1: Passport
    b2: ISBN
    b3: Price Field
    b4: MICR E-13B
    Normally SD_PROP_OCR_ACTIVE_TEMPLATES can be set to any combination of the above bit values to get
    the desired active templates. The one exception is Passport. When Passport mode is desired = only that template
    may be enabled. Attempts to set any other template active along with passport will generate an
    SD_ERR_PROPERTY_VALUE error. Also = setting the property to 0 has the same effect as setting
    SD_PROP_OCR_ENABLED to disabled.
        Property Data Type: int
    Set By: Value
    Initial Value: 1
    Required Components: CORE = OCR
    Example*/
    /* enable the User template along with ISBN */
    /*SD_Set(Handle = SD_PROP_OCR_ACTIVE_TEMPLATES = (void *)(1 + 4));
    See also*/
    public static final int SD_PROP_OCR_BUSY_BACKGROUND = 0x40012308;
    /*Description
    This property improves OCR performance when the background field on which the OCR text is printed is not a
    uniform color (typically white).
    The property value should be set as follows:
    0: Assume uniform background color.
    1: Assume busy background patterns.
    This property is useful when using the internal passport template as many issuing countries have patterns behind
    their OCR text which can interfere with OCR decoding.
        Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example
    *//* Handle busy backgrounds *//*
    SD_Set(Handle = SD_PROP_OCR_BUSY_BACKGROUND = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_CHAR_DISABLE = 0x40012307;
    /*Description
    This property allows certain characters to be globally disabled from all templates.
    The property value may currently be set to any of the following values:
    0: None
    1: Period (".")
    All other values are illegal. When any characters are disabled using this property = they are removed from the "Any
    Wildcard" control group. If a disabled character appears in a user template = the character is ignored during
    processing.
            Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example
    *//* Don't allow any periods *//*
    SD_Set(Handle = SD_PROP_OCR_CHAR_DISABLE = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_ENABLED = 0x40012301;
    /*Description
    This property specifies whether OCR decoding is enabled during the execution of the decoder.
    Recognition may be separately enabled or disabled for normal and inverse video OCR text. Normal video text is
    printed in black on a white substrate. Inverse video text is printed in white on a black substrate.
        The property value is a bit field defined as follows:
        b0: Enable normal video OCR recognition.
        b1: Enable inverse video OCR recognition.
        Note that when OCR decoding is enabled = a valid template must be defined to produce OCR output.
        Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example*/
    /* enable OCR decoding */
    /*SD_Set(Handle = SD_PROP_OCR_ENABLED = (void *) 0x01);
    See also*/
    public static final int SD_PROP_OCR_IGNORE_PASSPORT_CHECKSUMS = 0x40012310;
    /*Description
    This property allows OCR output to be generated when machine readable travel documents such as Passports,
    Visas and Travel Documents do not conform to the ICAO checksum standard. Normally = if a travel document
    internal checksum fails = no OCR output is output for that row.
    The property value should be set as follows:
    0: Perform checksum calculations and suppress OCR output for rows with bad checksums.
    1: Checksum calculations are not performed. OCR output with bad checksums may be issued.
    If the scanned documents follow ICAO standards = checksum calculations should be enabled to both reduce the
    likelihood of misreads and allow more aggressive reading by the OCR algorithms. However = if the scanned
    document has an incorrect checksum printed in the OCR data = then this property must be enabled to allow the data
    to be issued.
        Given that the internal OCR algorithms use the checksum digits to improve overall read rate = it is recommended
    that an initial pass with this property disabled be performed first. If the first pass does not produce all the expected
    data = then a second pass may be performed with the property enabled to see if the noread was due to an invalid
    checksum in the travel document.
        This property currently only affects the internal passport template. It will be ignored for all other OCR templates.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example
    *//* Ignore bad checksums *//*
    SD_Set(Handle = SD_PROP_OCR_IGNORE_PASSPORT_CHECKSUMS = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_ISSUE_UNCERTAIN = 0x40012309;
    /*Description
    This property allows OCR output to be generated when not all of the individual characters could be determined
    with reasonable certainty.
        The property value should be set as follows:
    0: Do not issue if any characters are uncertain.
    1: Allow issue with uncertain characters.
        Uncertain characters are issued as a group starting with ASCII value 29 (Group Seperator) = followed by zero or
    more ASCII characters = with the first being the most likely = followed by decreasing likely characters. The group is
    terminated by another GS character. If there are no characters between a pair of GS characters = then no possible
    character for that position was determined.
    Counting each uncertain group as a single character = the total number of characters output will match the length of
    the winning OCR template.
    This property currently only affects the internal passport template. It will be ignored for all other OCR templates.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example
    *//* Issue uncertain output *//*
    SD_Set(Handle = SD_PROP_OCR_ISSUE_UNCERTAIN = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_PREFERRED_ORIENTATION = 0x40012305;
    /*Description
    This property specifies how to orient decoded OCR text when the orientation can not be determined from the text
    itself.
            OCR text does not provide true omnidirectionality due to a number of rotationally symmetric characters. For
    examples = given the following OCR-B text
        HOHOHOHO
    one would not know if the final output should be HOHOHOHO (right orientation) or OHOHOHOH (left
    orientation) given that both H and O are rotationally symmetric. This property tells the OCR algorithms how to
    interpret rotationally ambiguous results.
    The property may take on the following values:
    0: UP - Text normally flows from the bottom to the top of the image.
    1: DOWN - Text normally flows from the top to the bottom of the image.
    2: LEFT - Text normally flow right to left in the image.
    3: RIGHT - Text normally flows left to right in the image.
    4: NONE - Do not issue rotationally ambiguous results.
        Users are cautioned that if the orientation can not be determined from the OCR text (as is the case with the string
    shown above) = and the SD_PROP_OCR_PREFERRED_ORIENTATION is not set properly = then incorrect OCR
    results may be produced. If misreads are not acceptable and there is no guarantee on the orientation of the OCR
    text = then setting the property to 4 will suppress answers where orientation can not be determined.
            Property Data Type: int
    Set By: Value
    Initial Value: 3 - RIGHT
    Required Components: CORE = OCR
    Example
    *//* set OCR default orientation from left to right *//*
    SD_Set(Handle = SD_PROP_OCR_PREFERRED_ORIENTATION = (void *) 3);
    See also*/
    public static final int SD_PROP_OCR_SINGLE_ROW = 0x40012304;
    /*Description
    This property specifies whether a row that matches a single row OCR template will have white space above and
    below it verified before considering it a valid read.
            The property value should be set as follows:
    0: Disable single row.
    1: Enable single row.
            If this property is disabled = then a single row will be issued only if there is no text immediately above and below
    that line. If this property is enabled = then the area above and below the line is not checked. Note that single row
    templates that match individual rows of multi-row templates will take priority if SD_PROP_OCR_SINGLE_ROW
    is enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: 1
    Required Components: CORE = OCR
    Example
    *//* enable OCR single row mode *//*
    SD_Set(Handle = SD_PROP_OCR_SINGLE_ROW = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_STRIP_CHECKSUMS = 0x40012306;
    /*Description
    This property specifies that any checksums (row or block) within a user specified OCR template should be
    removed from the decoded text before issue.
            The property value should be set as follows:
    0: Disable stripping of checksums.
    1: Enabled stripping of checksums.
    Both row and block checksums may be specified in a user defined template. If this property is disabled = then
    checksum characters are output along with the rest of the decoded data. If this property is enabled = then only the
    non checksum portion of the message is output. In symbols where any of the checksum calculations fail = no output
    is generated.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE = OCR
    Example
    *//* enabled stripping of checksum characters from output string *//*
    SD_Set(Handle = SD_PROP_OCR_STRIP_CHECKSUMS = (void *) 1);
    See also*/
    public static final int SD_PROP_OCR_USER_TEMPLATES = 0x50012302;
    /*Description
    This property allows the user to define custom OCR templates.
    The property is a NULL terminated string meeting the rules as outlined above. If the string contains any syntax
    problems = the SD_Set call will fail and a SD_ERR_PROPERTY_VALUE will be returned by SD_GetLastError.
    Valid user defined templates may then be activated by adding a 1 into the
    SD_PROP_OCR_ACTIVE_TEMPLATES property.
    The default user template is an 8 character single row made up of any alpha numeric character. The template will
    read either OCR-A or OCR-B.
            Property Data Type: char *
    Set By: Reference
    Initial Value: 1,3,7,7,7,7,7,7,7,7,0
    Required Components: CORE = OCR
    Example
    *//* Set the User Template to 7 OCR-B digits *//*
    unsigned char MyTemplate[] = {1,2,5,5,5,5,5,5,5,0};
    SD_Set(Handle = SD_PROP_OCR_USER_TEMPLATES = (void *)MyTemplate);
    See also*/
    /*4. AGC properties
    Description
    Automatic Gain Control*/
    public static final int SD_PROP_AGC_EXPOSURE_QUALITY = 0x40002001;
    /*Description
    This property indicates the optimal gain for the next exposure relative to the gain for the present exposure.
    This property value is updated after the SD_ComputeAGC function has returned. Note that the term gain here is a
    generic term that actually means the product of scene illumination, sensor exposure time, and a sensor output
    amplification factor. This property value is expressed as a percent, ranging from 1 to 10000. The value 100
    indicates that the optimal gain for the next exposure is the same as the gain of the present exposure. Likewise, the
    value 20 indicates that the next gain should be smaller by a factor of 5, and the value 500 indicates that the next
    gain should be larger by a factor of 5.
    Property Data Type: int
    Set By: Not applicable. This property is read-only.
    Initial Value: 100
    Required Components: CORE, AGC
            Example
    int GainFactor;
    *//* do the AGC computations *//*
    SD_ComputeAGC(Handle);
    *//* get the next gain *//*
    SD_Get(Handle, SD_PROP_AGC_NEXT_GAIN, &GainFactor);
    AdjustGain(GainFactor);
    See also*/
    public static final int SD_PROP_AGC_NEXT_GAIN = 0x40002002;
    /*Description
    This property indicates the optimal gain for the next exposure relative to the gain for the present exposure.
    This property value is updated after the SD_ComputeAGC function has returned. Note that the term gain here is a
    generic term that actually means the product of scene illumination, sensor exposure time, and a sensor output
    amplification factor. This property value is expressed as a percent, ranging from 1 to 10000. The value 100
    indicates that the optimal gain for the next exposure is the same as the gain of the present exposure. Likewise, the
    value 20 indicates that the next gain should be smaller by a factor of 5, and the value 500 indicates that the next
    gain should be larger by a factor of 5.
    Property Data Type: int
    Set By: Not applicable. This property is read-only.
    Initial Value: 100
    Required Components: CORE, AGC
            Example
    int GainFactor;
    *//* do the AGC computations *//*
    SD_ComputeAGC(Handle);
    *//* get the next gain *//*
    SD_Get(Handle, SD_AGC_NEXT_GAIN, &GainFactor);
    AdjustGain(GainFactor);
    See also
    SD_PROP_AGC_EXPOSURE_QUALITY*/
    public static final int SD_PROP_AGC_SAMPLING_OPTIONS = 0x40002003;
    /*Description
    This property is used to reduce the number of sampling points used in the AGC calculation.
    Changing the value of the SD_PROP_AGC_SAMPLING_OPTIONS property will reduce the amount of data used
    to perform the AGC calculation. As a result, the amount of time it takes to perform the AGC calculation will be
    reduced. However, the values of the SD_PROP_AGC_EXPOSURE_QUALITY and
    SD_PROP_AGC_NEXT_GAIN properties may be different due to the reduced amount of data used when
    performing the AGC calculations.
    The property value should be set as follows:
            0: Use the standard number of samples in the X and Y direction.
            1: Reduce the number of samples in the X direction.
            2: Reduce the number of samples in the Y direction.
            3: Reduce the number of samples in both the X and Y direction.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, AGC
            Example
    *//* decrease sampling in the X direction for the AGC computations *//*
    SD_Set(Handle, SD_PROP_AGC_SAMPLING_OPTIONS, (void *) 1);*/
    //5. Code 11 properties
    public static final int SD_PROP_C11_CHECKSUM = 0x40011802;
    /*Description
    This property specifies how Code 11 checksums are to be handled during the execution of the decoder.
    The property value is ignored if Code 11 decoding is not enabled using SD_PROP_C11_ENABLED.
    The property value should be set as follows:
            0: Two checksum digits checked.
            1: One checksum digit checked.
            2: Two checksum digits checked and stripped from the result string.
            3: One checksum digit checked and stripped from the result string.
            4: No checksum.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C11
            Example
    *//* enable checksum checking (one digit) and strip checksum *//*
    SD_Set(Handle, SD_PROP_C11_CHECKSUM, (void *) 3);
    See also*/
    public static final int SD_PROP_C11_ENABLED = 0x40011801;
    /*Description
    This property specifies whether Code 11 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Code 11 decoding.
            1: Enable Code 11 decoding.
    Note that when Code 11 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C11
            Example
    *//* enable Code 11 decoding *//*
    SD_Set(Handle, SD_PROP_C11_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C11_IMPROVE_BOUNDS = 0x40011803;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Code 11 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Code 11 symbol bounds.
            1: Enable improved Code 11 symbol bounds.
    Note: To improve the bounds of a Code 11 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C11
            Example
    *//* enable Code 11 improved bounds *//*
    SD_Set(Handle, SD_PROP_C11_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    //6. Code 128 properties
    public static final int SD_PROP_C128_ENABLED = 0x40010201;
    /*Description
    This property specifies whether Code 128 decoding is enabled during the execution of SD_Decode
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Code 128 decoding.
            1: Enable Code 128 decoding.
    Note that when Code 128 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C128
            Example
    *//* enable Code 128 decoding *//*
    SD_Set(Handle, SD_PROP_C128_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C128_IMPROVE_BOUNDS = 0x40010208;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Code 128 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Code 128 symbol bounds.
            1: Enable improved Code 128 symbol bounds.
    Note: To improve the bounds of a Code 128 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C128
            Example
    *//* enable Code 128 improved bounds *//*
    SD_Set(Handle, SD_PROP_C128_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C128_OUT_OF_SPEC_SYMBOL = 0x40010203;
    /*Description
    This property enables enhancements for reading difficult Code 128 bar codes during the execution of SD_Decode
    SD_ProgressiveDecode.
    The property value is a bit field defined as follows:
    b0: Enable Code 128 Enhancement for reading codes whose bars have inconsistent width from top to bottom.
            NOTE: This setting only works when SD_PROP_USE_MLD for Code 128 is disabled. Also, this setting can
    affect read performance when SD_PROP_MISC_IMPROVEMENTS bit +2 is set.
    b1: Enable Code 128 Enhancement for reading codes with extreme bar growth (i.e. over inking)
    b2: Enable reading of Code 128 barcodes with Out of Spec Start patterns (1st bar is 1x). NOTE: b0 and b2 cannot
    be activated at the same time.
    b3: Increase the codeword to codeword length tolerance. NOTE: Using this setting can decrease reading
    performances on low contrast or damaged barcodes.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C128
            Example
    SD_Set(Handle, SD_PROP_C128_OUT_OF_SPEC_SYMBOL, (void *) 2);
    See also
    SD_PROP_USE_MLD
            SD_PROP_MISC_IMPROVEMENTS*/
    public static final int SD_PROP_C128_PARITY_THRESHOLD = 0x40010204;
    /*Description
    This property specifies a threshold that affects the security of the Code 128 decoder. Larger values favor
    aggressive decoding, while smaller values favor security. By default, the property is set to its maximum value,
255, which disables the parity check and is the most aggressive option. Setting this property to a value that is too
    small can cause some or all Code 128 symbols to be unreadable.
    The property value should be set as follows:
        0-254: Variable parity threshold.
        255: Parity test disabled.
    Property Data Type: int
    Set By: Value
    Initial Value: 255 (parity test disabled)
    Required Components: CORE, C128
            Example
    *//* Disable Code 128 Parity Check *//*
    SD_Set(Handle, SD_PROP_C128_PARITY_THRESHOLD, (void *) 255);
    See also*/
    public static final int SD_PROP_C128_PARTIAL = 0x40010207;
    /*Description
    This property specifies whether a Code 128 result should be issued when only part of the symbol is present.
    This property should only be used if it is necessary to receive decode results when the full symbol is not present.
    This property specifies the minimum number of characters in the issued result string. Code 128 partial results will
    only be issued for strings containing at least four characters. When using this property the application
    must read the SD_PROP_RESULT_MODIFIER_EX property in the result callback to determine if the issued
    result is for the entire bar code or only a partial bar code.
    Note: When issuing partial symbols it is possible that the entire symbol may be issued after a partial result of the
    same bar code is issued. Enabling this property may adversely affect read rates on marginal symbols.
    Partial results will only be issued from the start character of a Code 128 symbol.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C128
            Example
    *//* issue partial Code 128 symbols with at least 10 characters *//*
    SD_Set(Handle, SD_PROP_C128_PARTIAL, (void *) 10);
    See also*/
    public static final int SD_PROP_C128_SECURITY = 0x40010209;
    public static final int SD_PROP_C128_SHORT_MARGIN = 0x40010202;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 128
    symbols during the execution of the Decoder.
            When this property is disabled, a standard length quiet zone is checked on both sides of a Code 128 symbol. When
    set, depending the value, a substandard length quiet zone is allowed on either end or both. Enabling this property is
    discouraged by Honeywell, unless absolutely necessary.
    The property value is ignored if Code 128 decoding is not enabled using SD_PROP_C128_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols on only one side.
            2: Allow short quiet zone symbols on both sides.
    Property Data Type: int
    Set By: Value
    Initial Value:0
    Required Components: CORE, C128
            Example
    *//* enable short quiet zone for Code 128 *//*
    SD_Set(Handle, SD_PROP_C128_SHORT_MARGIN, (void *)1);
    See also*/
    public static final int SD_PROP_C128_SUPPRESS_CB_CONFLICT = 0x40010206;
    /*Description
    This property specifies whether a Code 128 symbol should be issued if Codablock F is enabled and the symbol
    appears to be part of a Codablock F.
    This property should only be used when both SD_PROP_C128_ENABLED and
    SD_PROP_CODABLOCK_F_ENABLED are enabled. When this property is enabled a Code 128 symbol will be
    suppressed if it may be part of a Codablock F symbol. A symbol will be suppressed if the first codeword is a Start
    A character, the second codeword is a SHIFT, CODE B or CODE C character and the third codeword is a valid
    Codablock F row indicator.
    The property value is ignored if either Code 128 decoding or Codablock F decoding is not enabled.
    The property value should be set as follows:
            0: Issue symbols that may be part of a Codablock F.
            1: Do not issue symbols that may be part of a Codablock F.
    Property Data Type: int
    Set By: Value
    Initial Value: 1
    Required Components: CORE, C128
            Example
    *//* issue Code 128 symbols that may be part of a Codablock F *//*
    SD_Set(Handle, SD_PROP_C128_SUPPRESS_CB_CONFLICT, (void *) SD_CONST_DISABLED);
    See also*/
    //7. Code 39 properties
    public static final int SD_PROP_C39_CHECKSUM = 0x40010302;
    /*Description
    This property specifies how Code 39 checksums are to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value is ignored if Code 39 decoding is not enabled using SD_PROP_C39_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable checksum checking.
            2: Enable checksum checking and strip the checksum from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value:0
    Required Components: CORE, C39
            Example
    *//* enable checksum checking and strip checksum *//*
    SD_Set(Handle, SD_PROP_C39_CHECKSUM, (void *) SD_CONST_STRIPPED);
    See also*/
    public static final int SD_PROP_C39_ENABLED = 0x40010301;
    /*Description
    This property specifies whether Code 39 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Code 39 decoding.
            1: Enable Code 39 decoding.
    Note that when Code 39 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* enable Code 39 decoding *//*
    SD_Set(Handle, SD_PROP_C39_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C39_FULL_ASCII = 0x40010303;
    /*Description
    This property specifies whether Code 39 full ASCII decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value is ignored if Code 39 decoding is not enabled using SD_PROP_C39_ENABLED.
    The property value should be set as follows:
            0: Disable full ASCII Code 39 decoding.
            1: Enable full ASCII Code 39 decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* enable full ASCII Code 39 decoding *//*
    SD_Set(Handle, SD_PROP_C39_FULL_ASCII, (void *) 0x01);
    See also*/
    public static final int SD_PROP_C39_IMPROVE_BOUNDS = 0x40010310;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Code 39 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Code 39 symbol bounds.
            1: Enable improved Code 39 symbol bounds.
    Note: To improve the bounds of a Code 39 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* enable Code 39 improved bounds *//*
    SD_Set(Handle, SD_PROP_C39_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C39_PARTIAL = 0x40010309;
    /*Description
    This property specifies whether a Code 39 result should be issued when only part of the symbol is present.
    This property should only be used if it is necessary to receive decode results when the full symbol is not present.
    This property specifies the minimum number of characters in the issued result string. Code 39 partial results will
    only be issued for strings containing at least four characters. When using this property the application must read
    the SD_PROP_RESULT_MODIFIER_EX property in the result callback to determine if the issued result is for
    the entire bar code or only a partial bar code.
    Note: When issuing partial symbols it is possible that the entire symbol may be issued after a partial result of the
    same bar code is issued. Enabling this property may adversely affect read rates on marginal symbols.
    When SD_PROP_C39_FULL_ASCII is disabled, partial results may be issued from either the start or stop
    characters. However, when SD_PROP_C39_FULL_ASCII is enabled, partial results will only be issued from the
    start character.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* issue partial Code 39 symbols with at least 10 characters *//*
    SD_Set(Handle, SD_PROP_C39_PARTIAL, (void *) 10);
    See also*/
    public static final int SD_PROP_C39_SHORT_MARGIN = 0x40010304;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 39
    symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is enabled, a substandard length quiet zone is allowed on either end (but not both ends) of a
    Code 39 symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
    This property value is ignored if Code 39 decoding is not enabled using SD_PROP_C39_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* enable short quiet zone for Code 39 *//*
    SD_Set(Handle, SD_PROP_C39_SHORT_MARGIN, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C39_SUPPRESS_CB_CONFLICT = 0x40010306;
    /*Description
    This property specifies whether a Code 39 symbol should be issued if Codablock A is enabled and the symbol
    appears to be part of a Codablock A.
    This property should only be used when both SD_PROP_C39_ENABLED and
    SD_PROP_CODABLOCK_A_ENABLED are enabled. When this property is enabled a Code 39 symbol will be
    suppressed if it may be part of a Codablock A symbol. A symbol will be suppressed if the first and last codewords
    in the symbol are the same. The user is cautioned that this is not a terribly unique situation and that this property
    may cause valid Code 39 symbols that are not part of a Codablock A to be suppressed.
    The property value is ignored if either Code 39 decoding or Codablock A decoding is not enabled.
            0: Issue symbols that may be part of a Codablock A.
            1: Do not issue symbols that may be part of a Codablock A.
    Property Data Type: int
    Set By: Value
    Initial Value: 1
    Required Components: CORE, C39
            Example
    *//* do not issue Code 39 symbols that may be part of a Codablock A *//*
    SD_Set(Handle, SD_PROP_C39_SUPPRESS_CB_CONFLICT, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C39_UNCONV_INTER_CHAR = 0x40010313;
    /*Description
    This property specifies whether Code 39 barcodes with unconventional intercharacter gaps can be read.
    The property value should be set as follows:
            0: Disable reading of Code 39 barcodes with unconventional intercharacter gaps.
            1: Enable reading of Code 39 barcodes with unconventional intercharacter gaps.
    Initial Value: 0
    Example
        *//* Read Code 39 symbols with unconventional intercharacter gaps *//*
    SD_Set(Handle, SD_PROP_C39_UNCONV_INTER_CHAR, (void *) SD_CONST_ENABLED);
    See also*/
    //8. Code 93 properties
    public static final int SD_PROP_C93_ENABLED = 0x40011101;
    /*Description
    This property specifies whether Code 93 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Code 93 decoding.
            1: Enable Code 93 decoding.
    Note that when Code 93 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C93
            Example
    *//* enable Code 93 decoding *//*
    SD_Set(Handle, SD_PROP_C93_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C93_HIGH_DENSITY = 0x40011104;
    /*Description
    This property improves decoding of high density Code 93 barcodes. Enabling this property increases decoding
    time.
    The property value should be set as follows:
            0: Disable Code 93 high density decoding improvements.
            1: Enable Code 93 high density decoding improvements.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C93
            Example
    *//* Enable Code 93 high density decoding improvements *//*
    SD_Set(Handle, SD_PROP_C93_HIGH_DENSITY, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C93_IMPROVE_BOUNDS = 0x40011103;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Code 93 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Code 93 symbol bounds.
            1: Enable improved Code 93 symbol bounds.
    Note: To improve the bounds of a Code 93 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C93
            Example
    *//* enable Code 93 improved bounds *//*
    SD_Set(Handle, SD_PROP_C93_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_C93_SHORT_MARGIN = 0x40011102;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 93
    symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is enabled, a substandard length quiet zone is allowed on either end (but not both ends) of a
    Code 93 symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
    This property value is ignored if Code 93 decoding is not enabled using SD_PROP_C93_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C93
            Example
    *//* enable short quiet zone for Code 93 *//*
    SD_Set(Handle, SD_PROP_C93_SHORT_MARGIN, (void *) SD_CONST_ENABLED);
    See also*/
    //9. Callback properties
    public static final int SD_PROP_CALLBACK_PROGRESS = 0x40003002;
    /*Description
    This property specifies a pointer to a callback function that the decoder will call frequently during the execution of
    SD_Decode.
    The specified callback function will be called frequently during the execution of SD_Decode. See
    SD_CB_Progress for the requirements and recommendations regarding the callback function. If the property is
    NULL, then no progress callbacks will take place. The customer supplied callback function may alter the
    execution of SD_Decode by setting the SD_PROP_PROGRESS_CANCEL property value.
    Property Data Type: void (*)(int)
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example
    static void SD_CB_Progress(int Handle);
    SD_Set(Handle, SD_PROP_CALLBACK_PROGRESS, SD_CB_Progress);
    See also*/
    public static final int SD_PROP_CALLBACK_RESULT = 0x40003001;
    /*Description
    This property specifies a pointer to a callback function that the decoder will call for each decoded symbol.
    The specified callback function will be called 0 or more times during the execution of the SD_Decode function.
    The function is called once for each symbol decoded. See SD_CB_Result for the requirements and
    recommendations regarding the callback function. If the property value is NULL then no result callbacks will take
    place.
    Property Data Type: void (*)(int)
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example
    static void SD_CB_Result(int Handle);
    SD_Set(Handle, SD_PROP_CALLBACK_RESULT, SD_CB_Result);
    See also*/
    public static final int SD_PROP_CALLBACK_STATUS = 0x40003004;
    /*Description
    This property specifies a pointer to a callback function that the decoder will call during the execution of
    SD_Decode.
    The specified callback function will be called during the execution of SD_Decode . See SD_CB_Status for the
    requirements and recommendations regarding the callback function. If the property value is NULL, then no
    progress callbacks will take place. The customer-supplied callback function may alter the execution of SD_Decode
    by setting the SD_PROP_PROGRESS_CANCEL property value.
    Property Data Type: void (*)(int)
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example
    static void SD_CB_Status(int Handle, int Status);
    SD_Set(Handle, SD_PROP_CALLBACK_STATUS, SD_CB_Status);
    See also*/
    public static final int SD_PROP_CALLBACK_TIMER = 0x40003003;
    /*Description
    This property specifies a pointer to a callback function that the decoder may1 call during the execution of
    SD_Decode.
    The specified callback function will be called during the execution of SD_Decode . See SD_CB_Timer for the
    requirements and recommendations regarding the callback function. This callback function is normally called only
        for debugging and performance measuring purposes.
    Property Data Type: int (*)(int)
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example
    static void SD_CB_Timer(int Handle);
    SD_Set(Handle, SD_PROP_CALLBACK_TIMER, SD_CB_Timer);
    See also*/
    //10. Codabar properties
    public static final int SD_PROP_CB_CHECKSUM = 0x40010102;
    /*Description
    This property specifies how Codabar checksums are to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value is ignored if Codabar decoding is not enabled using SD_PROP_CB_ENABLED.
    The property value should be set as follows:
            0x00: Disable checksum checking.
            0x01: Enable checksum checking.
            0x02: Enable checksum checking and strip the checksum from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value: 0x00
    Required Components: CORE, CB
            Example
    *//* enable checksum checking and strip checksum *//*
    SD_Set(Handle, SD_PROP_CB_CHECKSUM, (void *)0x02);
    See also*/
    public static final int SD_PROP_CB_ENABLED = 0x40010101;
    /*Description
    This property specifies whether Codabar decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Codabar decoding.
            1: Enable Codabar decoding.
    Note that when Codabar decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, CB
            Example
    *//* enable Codabar decoding *//*
    SD_Set(Handle, SD_PROP_CB_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_CB_IMPROVE_BOUNDS = 0x40010104;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Codabar symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Codabar symbol bounds.
            1: Enable improved Codabar symbol bounds.
            Note: To improve the bounds of a Codabar symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, CB
            Example
    *//* enable Codabar improved bounds *//*
    SD_Set(Handle, SD_PROP_CB_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_CB_SHORT_MARGIN = 0x40010103;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Codabar
    symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is enabled, a substandard length quiet zone is allowed on either end (but not both ends) of a
    Codabar symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
    The property value is ignored if Codabar decoding is not enabled using SD_PROP_CB_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, CB
            Example
    *//* enable short quiet zone *//*
    SD_Set(Handle, SD_PROP_CB_SHORT_MARGIN, (void *) SD_CONST_ENABLED);
    See also*/
    //Codablock properties
    public static final int SD_PROP_CODABLOCK_A_ENABLED = 0x40010305;
    /*Description
    This property specifies whether Codablock A decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Codablock A decoding.
            1: Enable Codablock A decoding.
    Note that when Codablock A decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    When Codablock A and Code 39 decoding are enabled, there is some danger of mistakenly decoding a damaged
    Codablock A symbol as a Code 39 symbol. Therefore, whenever possible, Code 39 decoding should be disabled
    when Codablock A decoding is enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C39
            Example
    *//* enable Codablock A decoding *//*
    SD_Set(Handle, SD_PROP_CODABLOCK_A_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_CODABLOCK_F_ENABLED = 0x40010205;
    /*Description
    This property specifies whether Codablock F decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Codablock F decoding.
            1: Enable Codablock F decoding.
    Note that when Codablock F decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    When Codablock F and Code 128 decoding are enabled, there is some danger of mistakenly decoding a damaged
    Codablock F symbol as a Code 128 symbol. Therefore, whenever possible, Code 128 decoding should be disabled
    when Codablock F decoding is enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, C128
            Example
    *//* enable Codablock F decoding *//*
    SD_Set(Handle, SD_PROP_CODABLOCK_F_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    //12. Datamatrix properties
    public static final int SD_PROP_DM_BINARY_IMPROVEMENTS = 0x40010415;
    /*Description
    This property enhances decoding Data Matrix symbols within images that have been binarized.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. This property is a
    hint to the decoder. This property enhances the ability of the decoder to decode symbols within images that have
    been binarized.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* enable Data Matrix binary improvements *//*
    SD_Set(Handle, SD_PROP_DM_BINARY_IMPROVEMENTS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_DM_ENABLED = 0x40010401;
   /* Description
    This property specifies whether Data Matrix decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    Decoding may be separately enabled or disabled for normal and inverse video symbols. A normal video symbol
    has a black "L" finder pattern. An inverse video symbol has a white "L" finder pattern. Note that rectangular Data
    Matrix symbol decoding is separately controlled by the SD_PROP_DM_RECT property.
    The property value is a bit field defined as follows:
    b0: Enable normal video Data Matrix decoding
    b1: Enable inverse video Data Matrix decoding
    Note that only the ECC 200 style Data Matrix symbols are decoded by the decoder. The other styles are obsolete.
    Also note that when Data Matrix decoding is enabled in unattended operating mode, the values of the following
    properties must be set appropriately:
            - SD_PROP_IMAGE_TYP_DENSITY
            - SD_PROP_DM_MAX_MODULE_COUNT
            - SD_PROP_DM_MIN_MODULE_COUNT
            - SD_PROP_DM_MAX_MODULE_SIZE
            - SD_PROP_DM_MIN_MODULE_SIZE
            - SD_PROP_DM_ORIENTATIONS
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* enable inverse video Data Matrix decoding only *//*
    SD_Set(Handle, SD_PROP_DM_ENABLED, (void *)0x02);
    See also
    SD_PROP_DM_RECT*/
    public static final int SD_PROP_DM_ENHANCED_DAMAGE_MODE = 0x40010418;
    /*Description
    Enable decoding of damaged Data Matrix symbols where the lower-left corner of the finder pattern has been
    damaged or is missing.
    See also*/
    public static final int SD_PROP_DM_HIGH_SAMPLE_DENSITY = 0x40010417;
    /*Description
    Increase the maximum PPM of a warped Data Matrix symbols. The following values are supported:
    Value Maximum PPM
        0    25 PPM
        1    50 PPM
        2    100 PPM
            See also*/
    public static final int SD_PROP_DM_LOW_CONTRAST = 0x40010414;
    /*Description
    This property can improve decoding Data Matrix symbols when there is a low contrast image present.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. Low contrast images may occur for many
    reasons, including printing problems, lighting problems, motion blur, oblique camera angles, low sample density,
    etc. This property should only be used when encountering problems decoding Data Matrix symbols when a low
    contrast image is present.
    This algorithm should be used only if necessary to achieve improved performance. The algorithm may require
    significantly more processor cycles than the "standard" algorithm, and may therefore overload the processor, or
"starve" the decoding of located symbols. Customers should carefully examine the processor cycle increase and
    determine that a genuine benefit has been achieved before deciding to use the "improved" algorithm.
    The property value should be set as follows:
            0: Disable improvements for codes with low contrast.
            1: Enable improvements for codes with low contrast.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* improve decoding Data Matrix codes with low contrast *//*
    SD_Set(Handle, SD_PROP_DM_LOW_CONTRAST, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_DM_MAX_MODULE_COUNT = 0x40010403;
    /*Description
    This property specifies the maximum number of modules on a side for which Data Matrix symbols will be
    decoded. The count includes the finder pattern, but not the quiet zone. For rectangular symbols, the property value
    specifies the maximum length of the longer side.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Also, the property
    is ignored if SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP. Only values in the range 8 to 144
    inclusive are legal.
    Property Data Type: int
    Set By: Value
    Initial Value: 144
    Required Components: CORE, DM, UNOP
            Example
    *//* limit Data Matrix symbols to a maximum of 16 modules on a side *//*
    SD_Set(Handle, SD_PROP_DM_MAX_MODULE_COUNT, (void *) 16);
    See also
    SD_PROP_DM_MAX_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_SIZE
    SD_PROP_DM_MIN_MODULE_COUNT
            SD_PROP_DM_ORIENTATIONS
    SD_PROP_MISC_OP_MODE*/
    public static final int SD_PROP_DM_MAX_MODULE_SIZE = 0x40010404;
    /*Description
    This property specifies the maximum size of an individual module (i.e. square cell) of a Data Matrix symbol in
    units of microns (thousandths of a millimeter).
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Also, the property
    is ignored if SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP. This property is a hint to the decoder.
    Symbols with larger modules may sometimes be decoded and issued. In conjunction with using this property, it is
    vital to also accurately set the value of SD_PROP_IMAGE_TYP_DENSITY.
    Property Data Type: int
    Set By: Value
    Initial Value: 1270 (i.e. 0.050" or 50 mils)
    Required Components: CORE, DM, UNOP
            Example
    *//* limit Data Matrix module size to a maximum of 25 mils *//*
    SD_Set(Handle, SD_PROP_DM_MAX_MODULE_SIZE, (void *) 635);
    See also
    SD_PROP_DM_MIN_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_COUNT
    SD_PROP_DM_MAX_MODULE_COUNT
            SD_PROP_DM_ORIENTATIONS*/
    public static final int SD_PROP_DM_MIN_MODULE_COUNT = 0x40010405;
    /*Description
    This property specifies the minimum number of modules on a side for which Data Matrix symbols will be
    decoded. The count includes the finder pattern, but not the quiet zone. For rectangular symbols, the property value
    specifies the minimum length of the shorter side.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Also, the property
    is ignored if SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP. This property is a hint to the decoder.
    Symbols with a smaller number of modules may sometimes be decoded and issued. Only values in the range 8 to
        144 inclusive are legal.
    Property Data Type: int
    Set By: Value
    Initial Value: 8
    Required Components: CORE, DM, UNOP
            Example
    *//* limit Data Matrix symbols to a minimum of 12 modules on a side *//*
    SD_Set(Handle, SD_PROP_DM_MIN_MODULE_COUNT, (void *) 12);
    See also
    SD_PROP_DM_MAX_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_SIZE
    SD_PROP_DM_MAX_MODULE_COUNT
            SD_PROP_DM_ORIENTATIONS*/
    public static final int SD_PROP_DM_MIN_MODULE_SIZE = 0x40010406;
    /*Description
    This property specifies the minimum size of an individual module (i.e. square cell) of a Data Matrix symbol in
    units of microns (thousandths of a millimeter).
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Also, the property
    is ignored if SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP. This property is a hint to the decoder.
    Symbols with smaller modules may sometimes be decoded and issued. In conjunction with using this property, it is
    vital to also accurately set the value of SD_PROP_IMAGE_TYP_DENSITY.
    Property Data Type: int
    Set By: Value
    Initial Value: 508 (i.e. 0.020" or 20 mils)
    Required Components: CORE, DM, UNOP
            Example
    *//* limit Data Matrix module size to a minimum of 25 mils *//*
    SD_Set(Handle, SD_PROP_DM_MIN_MODULE_SIZE, (void *) 635);
    See also
    SD_PROP_MISC_OP_MODE
            SD_PROP_IMAGE_TYP_DENSITY
    SD_PROP_DM_MAX_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_COUNT
    SD_PROP_DM_MAX_MODULE_COUNT
            SD_PROP_DM_ORIENTATIONS*/
    public static final int SD_PROP_DM_NON_SQUARE_MODULES = 0x40010412;
    /*Description
    This property can improve decoding Data Matrix symbols when individual modules in the symbol are sufficiently
    non-square.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. This property should only be used when
    encountering problems decoding symbols with sufficiently non-square modules with this property disabled.
    The property value should be set as follows:
            0: Disable improvements for codes with non-square modules.
            1: Enable improvements for codes with non-square modules.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* improve decoding codes with non-square modules *//*
    SD_Set(Handle, SD_PROP_DM_NON_SQUARE_MODULES, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_DM_ORIENTATIONS = 0x40010407;
    /*Description
    This property specifies the allowed orientations of a Data Matrix symbol.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Also, the property
    is ignored if SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP. This property is a hint to the decoder.
    Symbols oriented outside the limits set by this property may sometimes be decoded and issued.
    The following values are permitted:
    SD_CONST_DM_AXIS_ALIGNED: The decoder attempts to decode a Data Matrix symbol only if its finder
    pattern is aligned with the x and y axes of the image +/- 20 degrees.
            SD_CONST_DM_OMNIDIRECTIONAL: The decoder attempts to decode Data Matrix symbols regardless of
    orientation.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM, UNOP
            Example
    *//* only decode axis aligned Data Matrix symbols *//*
    SD_Set(Handle, SD_PROP_DM_ORIENTATIONS, (void *) SD_CONST_DM_AXIS_ALIGNED);
    See also
            SD_PROP_DM_MAX_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_SIZE
            SD_PROP_DM_MIN_MODULE_COUNT
            SD_PROP_DM_MAX_MODULE_COUNT*/
    public static final int SD_PROP_DM_RECT = 0x40010402;
    /*Description
    This property specifies whether Data Matrix decoding for rectangular symbols is enabled during the execution of
    the decode.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Otherwise,
    rectangular symbol decoding may be enabled or disabled as follows:
            0: Disable rectangular Data Matrix decoding.
            1: Enable rectangular Data Matrix decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* enable inverse video Data Matrix decoding including rectangular symbols *//*
    SD_Set(Handle, SD_PROP_DM_ENABLED, (void *) 0x02);
    SD_Set(Handle, SD_PROP_DM_RECT, (void *) 0x01);
    See also
    SD_PROP_DM_ENABLED*/
    public static final int SD_PROP_DM_SHIFTED_TILES = 0x40010413;
    /*Description
    This property can improve decoding multi-tile Data Matrix symbols when the upper tiles are shifted in the symbol
    relative to the bottom tiles.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. This property should only be used when
    encountering problems decoding symbols with shifted multi-tile Data Matrix symbols with this property disabled.
    The property value should be set as follows:
            0: Disable improvements for codes with shifted tiles.
            1: Enable improvements for codes with shifted tiles.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* improve decoding codes with shifted multi-tile Data Matrix symbols *//*
    SD_Set(Handle, SD_PROP_DM_SHIFTED_TILES, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_DM_SUBREGION_HEIGHT = 0x40010411;
    /*Description
    Sets the height of the subregion over which Data Matrix symbols will be detected.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. It is also ignored
    unless the value +4 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the height of the region over which Data Matrix symbols will be detected and
    reported. The value is expressed in units of millimeters. Height is measured relative to the starting line specified by
    SD_PROP_DM_SUBREGION_TOP.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's y-axis extent begins
    with the specified top starting line and continues for the remaining height of the image.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM, UNOP
            Example
    *//* set 100 millimeter height for the Data Matrix symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_DM_SUBREGION_HEIGHT, (void *) 100);
    See also*/
    public static final int SD_PROP_DM_SUBREGION_LEFT = 0x40010408;
    /*Description
    Sets the left edge of the region over which Data Matrix symbols will be detected.
    The property is ignored if the value of SD_PROP_DM_ENABLED is SD_CONST_DISABLED. It is also ignored
    unless the value +4 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the left edge of the region over which Data Matrix symbols will be detected and
    reported. The value is expressed in units of millimeters. The left edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM, UNOP
            Example
    *//* set left edge for Data Matrix symbol detection region to 2 mm *//*
    SD_Set(Handle, SD_PROP_DM_SUBREGION_LEFT, (void *) 2);
    See also*/
    public static final int SD_PROP_DM_SUBREGION_TOP = 0x40010409;
    /*Description
    Sets the top edge of the region over which Data Matrix symbols will be detected.
    The property is ignored if the value of SD_PROP_DM_ENABLED is SD_CONST_DISABLED. It is also ignored
    unless the value +4 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the top edge of the region over which Data Matrix symbols will be detected and
    reported. The value is expressed in units of millimeters. The top edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM, UNOP
            Example
    *//* set top edge for Data Matrix symbol detection region to 20 mm *//*
    SD_Set(Handle, SD_PROP_DM_SUBREGION_TOP, (void *) 20);
    See also*/
    public static final int SD_PROP_DM_SUBREGION_WIDTH = 0x40010410;
    /*Description
    Sets the width of the subregion over which Data Matrix symbols will be detected.
    The property is ignored if the value of SD_PROP_DM_ENABLED is SD_CONST_DISABLED. It is also ignored
    unless the value +4 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the width of the region over which Data Matrix symbols will be detected and
    reported. The value is expressed in units of millimeters. Width is measured relative to the starting pixel specified
    by SD_PROP_DM_SUBREGION_LEFT.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's x-axis extent begins
    with the specified left starting pixel continues for the remaining width of the line.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM, UNOP
            Example
    *//* set width to 30 millimeters for the Data Matrix symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_DM_SUBREGION_WIDTH, (void *) 30);
    See also*/
    public static final int SD_PROP_DM_SYMBOL_SIZE = 0x40010416;
    /*Description
    This property can improve decoding Data Matrix symbols when the length of a symbol side is small.
    The property is ignored when SD_PROP_DM_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. This property should only be used when
    encountering problems decoding smaller Data Matrix symbols. Note, for proper Data Matrix decode operation, the
    decoder requires at least 2 pixels per module. This property will not necessarily help symbols whose sample
    density falls below this threshold. It is useful for symbols that have short sides because its sample density is near
    the 2 pixels per module minimum, or the number of modules in the symbol is near the Data Matrix minimum of 8,
    or both.
    The decoder may require significantly more processor cycles with this property enabled. This is especially true
    when the property is set to a value of 2. Customers should carefully examine the processor cycle increase and
    determine that a genuine benefit has been achieved before deciding to use this property.
    The property value should be set as follows:
            0: Normal Data Matrix operation.
            1: Handle smaller Data Matrix symbols.
            2: Handle very small Data Matrix symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DM
            Example
    *//* improve decoding small Data Matrix codes *//*
    SD_Set(Handle, SD_PROP_DM_SYMBOL_SIZE, (void *) 1);
    See also*/
    //Hong Kong 2 of 5 properties
    public static final int SD_PROP_HK25_ENABLED = 0x40012601;
    /*Description
    This property specifies whether Hong Kong 2 of 5 decoding is enabled during the execution of SD_Decode .
    The property value should be set as follows:
            0: Disable Hong Kong 2 of 5 decoding.
            1: Enable Hong Kong 2 of 5 decoding.
    Also note, neither 1 nor 2 digit Hong Kong 2 of 5 symbols will be decoded unless enabled using
    SD_PROP_HK25_LENGTHS. Users are strongly cautioned that enabling these short lengths may increase the
    likelihood of misread or "invented" symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, HK25
            Example
    *//* enable Hong Kong 2 of 5 decoding *//*
    SD_Set(Handle, SD_PROP_HK25_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_HK25_IMPROVE_BOUNDS = 0x40012603;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Hong Kong 2 of 5 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Hong Kong 2 of 5 symbol bounds.
            1: Enable improved Hong Kong 2 of 5 symbol bounds.
    Note: To improve the bounds of a Hong Kong 2 of 5 symbol, the amount of time before the symbol is issued may
    be significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, HK25
            Example
    *//* enable Hong Kong 2 of 5 improved bounds *//*
    SD_Set(Handle, SD_PROP_HK25_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_HK25_LENGTHS = 0x40012602;
    /*Description
    This property specifies which symbol lengths are enabled for Hong Kong 2 of 5 decoding during the execution of
    SD_Decode.
    The property value is ignored if Hong Kong 2 of 5 decoding is not enabled using SD_PROP_HK25_ENABLED.
    The property value should be set according to the following procedure:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 1 digit symbols are to be decoded, 'OR' in the value 1
    If 2 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 2 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, HK25
            Example
    *//* enable all symbol lengths except 1 digit Hong Kong 2 of 5 symbols *//*
    SD_Set(Handle, SD_PROP_HK25_LENGTHS, (void *) 0xfffffffe);
    See also*/
    //14. ITF properties
    /*Description
    Interleaved 2 of 5
            14.1. SD_PROP_I25_BOUNDARY_CHECK = 0x40010508
    Description
    Enables intelligent processing of quiet zones where we allow very small quiet zones, but can ensure that a read
    cannot occur within another bar code.
            0: disabled
            1: no-minimum area checked
            2: requires a full specified quiet zone area to be within the image
            3: requires 2X the full specified quiet zone area to be within the image
                        (defaults to 2)
    See also
    SD_PROP_MISC_LINEAR_BOUNDARY_CHECK*/
    public static final int SD_PROP_I25_CHECKSUM = 0x40010502;
    /*Description
    This property specifies how Interleaved 2 of 5 checksums are to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value is ignored if Interleaved 2 of 5 decoding is not enabled using SD_PROP_I25_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable checksum checking.
            2: Enable checksum checking and strip the checksum from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, I25
            Example
    *//* enable checksum checking and strip checksum *//*
    SD_Set(Handle, SD_PROP_I25_CHECKSUM, (void *) SD_CONST_STRIPPED);
    See also*/
    public static final int SD_PROP_I25_ENABLED = 0x40010501;
    /*Description
    This property specifies whether Interleaved 2 of 5 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Interleaved 2 of 5 decoding.
            1: Enable Interleaved 2 of 5 decoding.
    Note that when Interleaved 2 of 5 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Also note that unless enabled using SD_PROP_I25_LENGTHS, neither 2 nor 4 digit Interleaved 2 of 5 symbols
    will be decoded. Users are strongly cautioned that enabling these short lengths may increase the likelihood of
    misread or "invented" symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, I25
            Example
    *//* enable Interleaved 2 of 5 decoding *//*
    SD_Set(Handle, SD_PROP_I25_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_I25_HIGH_DENSITY = 0x40010507;
    /*Description
    This property improves decoding of high density Interleaved 2 of 5 barcodes. Enabling this property increases
    decoding time.
    The property value should be set as follows:
            0: Disable Interleaved 2 of 5 high density decoding improvements.
            1: Enable Interleaved 2 of 5 high density decoding improvements.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, I25
            Example
    *//* Enable Interleaved 2 of 5 high density decoding improvements *//*
    SD_Set(Handle, SD_PROP_I25_HIGH_DENSITY, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_I25_IMPROVE_BOUNDS = 0x40010506;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    an Interleaved 2 of 5 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Interleaved 2 of 5 symbol bounds.
            1: Enable improved Interleaved 2 of 5 symbol bounds.
    Note: To improve the bounds of an Interleaved 2 of 5 symbol, the amount of time before the symbol is issued may
    be significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, I25
            Example
    *//* enable Interleaved 2 of 5 improved bounds *//*
    SD_Set(Handle, SD_PROP_I25_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_I25_LENGTHS = 0x40010503;
    /*Description
    This property specifies which symbol lengths are enabled for Interleaved 2 of 5 decoding during the execution of
    SD_Decode or SD_ProgressiveDecode.
    The property value is ignored if Interleaved 2 of 5 decoding is not enabled using SD_PROP_I25_ENABLED.
    The property value should be set according to the following procedure:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 2 digit symbols are to be decoded, 'OR' in the value 1
    If 4 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 4 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, I25
            Example
    *//* enable all symbol lengths except 2 digit Interleaved 2 of 5 symbols *//*
    SD_Set(Handle, SD_PROP_I25_LENGTHS, (void *) 0xfffffffe);
    See also*/
    public static final int SD_PROP_I25_SHORT_MARGIN = 0x40010504;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Interleaved 2
    of 5 symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is enabled, a substandard length quiet zone is allowed on one end (1), or both ends (2), of an
    Interleaved 2 of 5 symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
    This property value is ignored if Interleaved 2 of 5 decoding is not enabled using SD_PROP_I25_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols (on one end only).
            2: Allow short quiet zone symbols (on both ends).
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, I25
            Example
    *//* enable short quiet zone for Interleaved 2 of 5 *//*
    SD_Set(Handle, SD_PROP_I25_SHORT_MARGIN, (void *) 1);
    See also*/
    //15. Korea Post properties
    public static final int SD_PROP_KP_CHECKSUM = 0x40013502;
    /*Description
    This property specifies how Korea Post are to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Checksum digit is checked.
            1: Checksum digit is checked and stripped from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, KP
            Example
    *//* remove Korea Post checksum digit *//*
    SD_Set(Handle, SD_PROP_KP_CHECKSUM, (void *) 1);
    See also*/
    public static final int SD_PROP_KP_ENABLED = 0x40013501;
    /*Description
    This property specifies whether Korea Post decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Korea Post decoding.
            1: Enable Korea Post decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, KP
            Example
    *//* enable Korea Post decoding *//*
    SD_Set(Handle, SD_PROP_KP_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_KP_REVERSE = 0x40013503;
    /*Description
    This property specifies how Korea Post result strings to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: The result string is returned in the order it is encoded in the decoded symbol.
            1: The result string is returned in the reverse of the order it is encoded in the decoded symbol.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, KP
            Example
    *//* reverse Korea Post data *//*
    SD_Set(Handle, SD_PROP_KP_REVERSE, (void *) SD_CONST_ENABLED);
    See also*/
    //16. Matrix 2 of 5 properties
    public static final int SD_PROP_M25_CHECKSUM = 0x40011902;
    /*Description
    This property specifies how Matrix 2 of 5 checksums are to be handled during the execution of SD_Decode .
    The property value is ignored if Matrix 2 of 5 decoding is not enabled using SD_PROP_M25_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable checksum checking.
            2: Enable checksum checking and strip the checksum from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, M25
            Example
    *//* enable checksum checking and strip checksum *//*
    SD_Set(Handle, SD_PROP_M25_CHECKSUM, (void *) SD_CONST_STRIPPED);
    See also*/
    public static final int SD_PROP_M25_ENABLED = 0x40011901;
    /*Description
    This property specifies whether Matrix 2 of 5 decoding is enabled during the execution of SD_Decode.
    Matrix 2 of 5 is also known as: European Matrix 2 of 5.
    The property value should be set as follows:
            0: Disable Matrix 2 of 5 decoding.
            1: Enable Matrix 2 of 5 decoding.
    Also note, neither 1 nor 2 digit Matrix 2 of 5 symbols will be decoded unless using SD_PROP_M25_LENGTHS.
    Users are strongly cautioned that enabling these short lengths may increase the likelihood of misread or "invented"
    symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, M25
            Example
    *//* enable Matrix 2 of 5 decoding *//*
    SD_Set(Handle, SD_PROP_M25_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_M25_IMPROVE_BOUNDS = 0x40011904;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Matrix 2 of 5 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Matrix 2 of 5 symbol bounds.
            1: Enable improved Matrix 2 of 5 symbol bounds.
    Note: To improve the bounds of a Matrix 2 of 5 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, M25
            Example
    *//* enable Matrix 2 of 5 improved bounds *//*
    SD_Set(Handle, SD_PROP_M25_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_M25_LENGTHS = 0x40011903;
    /*Description
    This property specifies which symbol lengths are enabled for Matrix 2 of 5 decoding during the execution of
    SD_Decode .
    The property value is ignored if Matrix 2 of 5 decoding is not enabled using SD_PROP_M25_ENABLED.
    The property value should be set according to the following procedure:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 1 digit symbols are to be decoded, 'OR' in the value 1
    If 2 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 2 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, M25
            Example
    *//* enable all symbol lengths except 1 digit symbols *//*
    SD_Set(Handle, SD_PROP_M25_LENGTHS, (void *) 0xfffffffe);
    See also*/
    //17. Micro PDF properties
    public static final int SD_PROP_MICROPDF_ENABLED = 0x40010702;
    /*Description
    This property specifies whether MicroPDF417 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    Note that when MicroPDF417 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    The MicroPDF417 decoding algorithm is primarily intended for symbols oriented horizontally or vertically +/-25
    degrees. However, omnidirectional performance may be acceptable in some applications, particularly for taller
    symbols.
    In unattended operating mode, omnidirectional performance is enhanced when the value of the
    SD_PROP_MISC_LOW_ASPECT_RATIO is set to 1.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, PDF
            Example
    *//* enable MicroPDF417 decoding *//*
    SD_Set(Handle, SD_PROP_MICROPDF_ENABLED, (void *) 1);
    See also
    SD_PROP_MISC_LOW_ASPECT_RATIO*/
    public static final int SD_PROP_MICROPDF_IMPROVE_BOUNDS = 0x40010704;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a MicroPDF417 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved MicroPDF417 symbol bounds.
            1: Enable improved MicroPDF417 symbol bounds.
            Note: To improve the bounds of a MicroPDF417 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, PEF
            Example
    *//* enable MicroPDF417 improved bounds *//*
    SD_Set(Handle, SD_PROP_MICROPDF_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    //18. MSI Plessey properties
    public static final int SD_PROP_MSIP_CHECKSUM = 0x40011602;
    /*Description
    This property specifies how MSI Plessey checksums are to be handled during the execution of the decoder.
    The property value is ignored if MSI Plessey decoding is not enabled using SD_PROP_MSIP_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable a single mod 10 checksum check.
            2: Enable a mod 11 and a mod 10 checksum check.
            3: Enable two mod 10 checksum checks.
            5: Enable a single mod 10 checksum check and strip the checksum
            6: Enable a mod 11 and a mod 10 checksum check and strip the checksums
            7: Enable two mod 10 checksum checks and strip the checksums
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, MSIP
            Example
    *//* enable single mod 10 checksum checking and strip checksum *//*
    SD_Set(Handle, SD_PROP_MSIP_CHECKSUM, (void *) 5);
    See also*/
    public static final int SD_PROP_MSIP_ENABLED = 0x40011601;
    /*Description
    This property specifies whether MSI Plessey decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable MSI Plessey decoding.
            1: Enable MSI Plessey decoding.
    Note that when MSI Plessey decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, MSIP
            Example
    *//* enable MSI Plessey decoding *//*
    SD_Set(Handle, SD_PROP_MSIP_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_MSIP_IMPROVE_BOUNDS = 0x40011603;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a MSI Plessey symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved MSI Plessey symbol bounds.
            1: Enable improved MSI Plessey symbol bounds.
    Note: To improve the bounds of a MSI Plessey symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, MSIP
            Example
    *//* enable MSI Plessey improved bounds *//*
    SD_Set(Handle, SD_PROP_MSIP_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_MSIP_SHORT_MARGIN = 0x40011604;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for MSI Plessey
    symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is enabled, a substandard length quiet zone is allowed on either end (but not both ends) of a
    MSI Plessey symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
    This property value is ignored if MSI Plessey decoding is not enabled using SD_PROP_MSIP_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow short quiet zone symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, MSIP
            Example
    *//* enable short quiet zone for Code MSI Plessey *//*
    SD_Set(Handle, SD_PROP_MSIP_SHORT_MARGIN, (void *) SD_CONST_ENABLED);
    See also*/
    //19. Nec 2 of 5 properties
    public static final int SD_PROP_NEC25_CHECKSUM = 0x40012202;
    /*Description
    This property specifies how NEC 2 of 5 checksums are to be handled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    This property value is ignored if NEC 2 of 5 decoding is not enabled using SD_PROP_NEC25_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable checksum checking.
            2: Enable checksum checking and strip the checksum from the result string.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, NEC25
            Example
    *//* enable checksum checking and strip it from the result *//*
    SD_Set(Handle, SD_PROP_NEC25_CHECKSUM, (void *) SD_CONST_STRIPPED);
    See also*/
    public static final int SD_PROP_NEC25_ENABLED = 0x40012201;
    /*Description
    This property specifies whether NEC 2 of 5 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
            NEC 2 of 5 is also known as: Matrix 2 of 5 Japan.
    The property value should be set as follows:
            0: Disable NEC 2 of 5 decoding.
            1: Enable NEC 2 of 5 decoding.
    Note that when NEC 2 of 5 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Also note, neither 1 nor 2 digit NEC 2 of 5 symbols will be decoded unless using SD_PROP_NEC25_LENGTHS.
    Users are strongly cautioned that enabling these short lengths may increase
    the likelihood of misread or "invented" symbols.
    Property Data Type: int
    Set By:Value
    Initial Value: 0
    Required Components: CORE, NEC25
            Example
    *//* enable NEC 2 of 5 decoding *//*
    SD_Set(Handle, SD_PROP_NEC25_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_NEC25_IMPROVE_BOUNDS = 0x40012204;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a NEC 2 of 5 symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved NEC 2 of 5 symbol bounds.
            1: Enable improved NEC 2 of 5 symbol bounds.
    Note: To improve the bounds of a NEC 2 of 5 symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, NEC25
            Example
    *//* enable NEC 2 of 5 improved bounds *//*
    SD_Set(Handle, SD_PROP_NEC25_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_NEC25_LENGTHS = 0x40012203;
    /*Description
    This property specifies which symbol lengths are enabled for NEC 2 of 5 decoding during the execution of
    SD_Decode or SD_ProgressiveDecode.
    The property value is ignored if NEC 2 of 5 decoding is not enabled using SD_PROP_NEC25_ENABLED.
    The property value should be set according to the following procedure:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 1 digit symbols are to be decoded, 'OR' in the value 1
    If 2 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 2 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, NEC25
            Example
    *//* enable all symbol lengths except 1 digit symbols *//*
    SD_Set(Handle, SD_PROP_NEC25_LENGTHS, (void *) 0xfffffffe);
    See also*/
    //20. PDF 417 properties
    public static final int SD_PROP_PDF_CLEAR_HISTORY = 0x40010705;
    /*Description
    This property is used to clear the decoder's internal state information related to previously examined PDF417
    symbols. When 1 is written to this property, the decoder's PDF417 state information (history) is cleared. The
    property is read/write, but is normally only ever written.
    Property Data Type: int
    Initial Value: 0
    Required Components: CORE and PDF417 components
    Example
    SD_Set(Handle, SD_PROP_PDF_CLEAR_HISTORY, 1);
    See also
    SD_PROP_PDF_HISTORY
            SD_PROP_PDF_HISTORY_SIZE
    SD_PROP_PDF_HISTORY_EXPIRE_COUNT
            SD_PROP_PDF_HISTORY_MIN_MATCH_PERCENT*/
    public static final int SD_PROP_PDF_ENABLED = 0x40010701;
    /*Description
    This property specifies whether PDF417 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    Note that when PDF417 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, PDF
            Example
    *//* enable PDF417 decoding *//*
    SD_Set(Handle, SD_PROP_PDF_ENABLED, (void *) 1);
    See also*/
    public static final int SD_PROP_PDF_ENHANCED_DAMAGE_HANDLING = 0x40010710;
    /*Description
    This property specifies whether enhanced damage handling (HPF) is enabled during the execution of Decode. The
    property value should be set as follows:
            0: Disable enhanced damage handling
            1: Enable enhanced damage handling
    Initial Value: 0
    See also*/
    public static final int SD_PROP_PDF_HANDLE_INVALID_SHIFT = 0x40010703;
    /*Description
    This property is the result of the following line in the specification:
    NOTE: A sub-mode latch may be followed by another sub-mode latch or sub-mode shift; but a sub-mode shift may
    not be followed by either a sub-mode shift or sub-mode latch.
    The property value should be set as follows:
            0: No support of misplaced shift codewords (fail decoding)
            1: Ignore misplaced shift codewords
            2: Process misplaced shift codewords
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, PDF
            Example
    *//* enable the process of PDF Invalid shift *//*
    SD_Set(Handle, SD_PROP_PDF_HANDLE_INVALID_SHIFT, (void *) 2);
    See also*/
    public static final int SD_PROP_PDF_HISTORY_EXPIRE_COUNT = 0x40010708;
    /*Description
    This property controls the number of frames for which the decoder will retain and combine data from
    partially-decoded PDF417 symbols. When this setting is zero (the default), the decoder does not retain any
    information from partially-decoded PDF417s. The count is always measured from the last frame that contained
    sufficient information to identify a particular label. Multiple labels can have state information saved inside the
    decoder at any given time, and the expiration frame counts of these partially-decoded labels are maintained
    independently.
    Property Data Type: int
    Initial Value: 0 (no history maintained between frames)
    Required Components: CORE and PDF417 components
    Example
        *//* Maintain partially decoded PDF417 symbols for 3 frames *//*
    SD_Set(Handle, SD_PROP_PDF_HISTORY_EXPIRE_COUNT, 3);
    See also
    SD_PROP_PDF_CLEAR_HISTORY
            SD_PROP_PDF_HISTORY
    SD_PROP_PDF_HISTORY_SIZE
            SD_PROP_PDF_HISTORY_MIN_MATCH_PERCENT*/
    public static final int SD_PROP_PDF_HISTORY_MIN_MATCH_PERCENT = 0x40010709;
    //21. Postal properties
    /*Description
    Australian Postal Code:
            - 4-State barcode, can be 37, 52 or 67 bars (11, 16 or 21 codewords)
            - start and stop are 2 bars (tracker ascender + tracker)
            - The 4 bars after the start are the Format Control Code (FCC)
                FCC supported by the decoder:
                        37 bars: FCC01..FCC32, FCC34..FCC37, FCC45, FCC87, FCC92
            52 bars: FCC00, FCC33, FCC38..FCC43, FCC46..FCC61, FCC72
            67 bars: FCC00, FCC44, FCC62..FCC71, FCC73..FCC91, FCC93..FCC99
            - Customer information field (16 or 31 bars)
            - Reed Solomon error correction (12 bars)
                POSTNET (POSTal Numeric Encoding Technique)
            - 2-State barcode, can be
            - 32 bars: 5-digit ZIP code (A Field)
            - 37 bars: 6-digit, (B Field), now obsolete
            - 47 bars: 8-digit, (B' Field)
                        - 52 bars: 9-digit ZIP+4 code ( C Field)
            - 62 bars: 11-digit, ZIP Code, ZIP+4 Code, Delivery Point Code ( C' Field)
                        - start and stop are just one high bar (also named Frame bars)
            - each digit is encoded with 5 bars, 2 high and 3 low (same as CEPNET)
            - correction character: Modulo 10, 5 bars
                CEPNET:
                        - 2-State barcode, can be 47, 72 bars (8 or 13 digits)
            - start and stop are just one high bar (also named Delimiter)
            - each digit is encoded with 5 bars, 2 high and 3 low (same as POSTNET)
            - correction character: Modulo 10, 5 bars
                PLANET (Postal Alpha Numeric Encoding Technique)
            - fully superseded by Intelligent Mail Barcode by January 28, 2013.
                        - 2-State barcode, can be 62 or 72 bars (12- or 14-digit including checksum)
            - Service Type: 2 digits
            - Customer ID: 9 or 11 digits
            - Checksum: 1 digit
            - start and stop are just one high bar (also named Delimiter)
            - each digit is encoded with 5 bars, 3 high and 2 low (exact opposite of POSTNET)
            - correction character: Modulo 10, 5 bars
                Canada Post (CPC 4-State or PostBar)
            - derived from the RM4SCC
            - 3 types:
                        - 52 bars (Business reply): 12 reed-solomon parity check bars, 36 information bars (12 characters)
            - 56 bars: 30 reed-solomon parity check bars, 18 information bars (6 characters)
            - 73 bars (not supported by ID)
            - 82 bars: 12 reed-solomon parity check bars, 66 information bars (22 characters)
            - 4 characters set can be used
            - start and stop are 2 bars (tracker ascender + tracker)
                RM4SCC (Royal Mail 4-State Customer Code), also known as CBC (Customer Code)
            - start is tracker ascender, stop is full tracker
            - checksum based on modulo 6
            - 9 characters max encoded (38 valid characters: numeric, alphanumeric upper case and () or [])
            - each character consists of 4 bars, Two of these have ascenders and two have descenders.
            - 3 types known:
                        - 51 bars: RED TAG Barcode
            - 66 bars: Mailmark barcodes C (consolidated)
            - 78 bars: Mailmark barcodes L (long)
                KIX (Klant index or Customer index)
            - 4-State
            - Slightly modified from CBC (doesn't use the start and end symbols or the checksum, separates the house number and
                        suffixes with an X)
                Intelligent Mail Barcode (IM barcode), also known as 4-State Customer Barcode (4CB or 4-CB or USPS4CB)
            - 4-State
            - 65-bar barcode for use on mail in the United States
            - Supersede POSTNET and PLANET.
                InfoMail Barcode A
            - 4-State, 51 bars (17 cw, 12 data + 5 error correction)
            - Reed-solomon error correction
                UPU ID-tag 4-state (S18d)
            - 4-state
            - can be 57 or 75 bars
                POSTI 4-state, Finnish post barcode
            - 4-state, 42 bars (16 cw, 8 data + 8 error correction)
            - Reed-solomon error correction*/
    public static final int SD_PROP_POSTAL_AP_BAR_OUTPUT = 0x40010817;
    public static final int SD_PROP_POSTAL_AP_CUST_INTERPRET = 0x40010818;
    public static final int SD_PROP_POSTAL_AP_EC_OUTPUT = 0x40010820;
    public static final int SD_PROP_POSTAL_AP_ZERO_FCC = 0x40010813;
    /*Description
    This property allows Australia Post codes to be issued when the Format Control Code (FCC) and Delivery Point
    Identifier (DPID) are both zero.
    This property is ignored if Australia Post decoding is not enabled using SD_PROP_POSTAL_ENABLED.
    The property value should be set as follows:
            0: Do not issue zero FCC Australia Post codes.
            1: Issue zero FCC Australia Post codes.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, AP
            Example
    *//* issue zero FCC Australia Post codes *//*
    SD_Set(Handle, SD_PROP_POSTAL_AP_ZERO_FCC, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_POSTAL_CP_BAR_OUTPUT = 0x40010819;
    /*Description
    Enable this parameter to change the resulting text from a successfully decoded Canada Post barcode.
    If enabled, the resulting text will consist of three comma delimited strings. There are no spaces in the string.
    The first string contains one character for each of the (corrected) bars in the barcode. Each character will contain a
    digit from '0' through '3' inclusive. A '0' corresponds to a "full" bar, a '1' corresponds to an "up" bar, a '2'
    corresponds to a "down" bar, and a '3' corresponds to a "clock" bar.
    The second string contains one digit that indicates the number bar errors that have been successfully corrected.
    The third string contains one digit that indicates the number of bar erasures that have been successfully corrected.
    See also*/
    public static final int SD_PROP_POSTAL_ENABLED = 0x40010801;
    /*Description
    This property sets which, if any, of the postal symbologies is enabled for decoding.
    The postal symbologies are grouped by application: only postal symbologies of one group can be enabled together.
    For instance symbologies from United States Postal group cannot be combined with those from Royal Mail group.
    Postal symbology decoding is enabled by ORing (OR bitwise) one or more constants from the group list below.
    All postal symbology decoding can be disabled with SD_CONST_DISABLED.
    United States Postal group:
            - SD_CONST_PL: PLANET decoding enabled.
            - SD_CONST_PN: POSTNET decoding enabled.
            - SD_CONST_PN + SD_CONST_BF: POSTNET (with B and B' Fields) decoding enabled.
            - SD_CONST_UPU: UPU decoding enabled (aka Universal Postal Union).
            - SD_CONST_USPS4CB: USPS 4CB decoding enabled (aka Intelligent Mail or Customer Barcode).
    Autralia Post group:
            - SD_CONST_AP: Australia 4-state decoding enabled.
    Brazil Post group:
            - SD_CONST_PN + SD_CONST_CEPNET: POSTNET and CEPNET decoding enabled.
    - SD_CONST_BZ4: Brazil 4-state decoding enabled.
    Japan Post group:
            - SD_CONST_JP: Japan Post decoding enabled.
    Royal Mail group:
            - SD_CONST_RM: Royal Mail 4-state Customer Code decoding enabled (aka British Post Office).
            - SD_CONST_INFOMAIL: Royal Mail InfoMail decoding enabled.
    - SD_CONST_EIB: Mailmark code enabled (aka EIB).
    Dutch Post group:
            - SD_CONST_KIX: KIX Code decoding enabled.
    Sweden Post group:
            - SD_CONST_SP: Sweden 4-state decoding enabled.
    Canada Post group:
            - SD_CONST_CP: Canada 4-state decoding enabled.
    Finland Post group:
            - SD_CONST_POSTI4 : Posti 4-state decoding enabled.
    Portugal Post group:
            - SD_CONST_PORTUGAL: Portugal 4-state decoding enabled.
    - SD_CONST_UPU: UPU decoding enabled (aka Universal Postal Union).
    New Zealand Post group:
            - SD_CONST_NZ4: New Zealand 4-state decoding enabled.
    Note that when any postal symbology decoding is enabled in unattended operating mode, the values of the
    properties SD_PROP_POSTAL_MIN_BAR_COUNT, SD_PROP_POSTAL_MAX_BAR_COUNT,
    SD_PROP_POSTAL_ORIENTATIONS, and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Postal symbology decoding performance depends on accurate information regarding sampling density. Assuming
    accurate information is provided to the engine, the following range of sampling densities is well supported:
    POSTNET, PLANET, UPU and USPS 4CB: 80 .. 310 per inch (31,5 .. 122 per cm)
    Japan Post: 84 .. 324 per inch (33,07 .. 127,5 per cm)
    Australia Post: 86 .. 332 per inch (33,86 .. 130,7 per cm)
    Royal Mail and KIX Code: 82 .. 317 per inch (32.28 .. 124,9 per cm)
    Sampling densities outside this range are allowed, and the engine makes its best effort to decode. However,
    customers are cautioned that performance rapidly degrades outside the specified range.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and the appropriate Postal Symbologies Group component
    Example
        *//* enable POSTNET (w/ B and B' fields) decoding *//*
    SD_Set(Handle, SD_PROP_POSTAL_ENABLED, (void *) SD_CONST_PN + SD_CONST_BF);
    See also*/
    public static final int SD_PROP_POSTAL_KIX_BOTH_DIR = 0x40010821;
    /*Description
    When this property is active, the KIX barcode is read in both directions without any message validation. The two
    obtained messages are concatenated using the minus sign as a separator.
    Initial Value: 0
    Example
        *//* enable KIX reading in both directions *//*
    SD_Set(Handle, SD_PROP_POSTAL_KIX_BOTH_DIR, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_POSTAL_MAX_BAR_COUNT = 0x40010804;
    /*Description
    Sets the maximum number of bars in a postal symbol.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. When the
    value of SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP, the value of
    SD_PROP_POSTAL_MAX_BAR_COUNT is ignored. When the value of SD_PROP_MISC_OP_MODE is set to
    SD_CONST_UNOP, and the SD_ProgressiveDecode function is used, the value of
    SD_PROP_MAX_BAR_COUNT must be set appropriately. Only values from 24 through 72 are legal.
    This property is a hint to the decoder. Symbols with greater than the maximum specified bar count may sometimes
    be decoded and issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 72
    Required Components: CORE, UNOP, and any component in the Postal Symbologies Group
    Example
        *//* optimize for 52 bar and down decoding *//*
    SD_Set(Handle, SD_PROP_POSTAL_MAX_BAR_COUNT, (void *) 52);
    See also*/
    public static final int SD_PROP_POSTAL_MIN_BAR_COUNT = 0x40010802;
    /*Description
    Sets the minimum number of bars in a postal symbol.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. When the
    value of SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP, the value of
    SD_PROP_POSTAL_MIN_BAR_COUNT is ignored. When the value of SD_PROP_MISC_OP_MODE is set to
    SD_CONST_UNOP, the value of SD_PROP_MIN_BAR_COUNT must be set appropriately. Only values from 24
    through 72 are legal.
    This property is a hint to the decoder. Symbols with less than the minimum specified bar count may sometimes be
    decoded and issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 32
    Required Components: CORE, UNOP, and any component in the Postal Symbologies Group
    Example
        *//* optimize for 52 bar and down decoding *//*
    SD_Set(Handle, SD_PROP_POSTAL_MIN_BAR_COUNT, (void *) 52);
    See also*/
    public static final int SD_PROP_POSTAL_ORIENTATIONS = 0x40010803;
    /*Description
    Sets the orientations at which postal symbols can be decoded by the decoder.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. When the
    value of SD_PROP_MISC_OP_MODE is set to SD_CONST_MANOP, the value of
    SD_PROP_POSTAL_ORIENTATIONS is ignored. When the value of SD_PROP_MISC_OP_MODE is set to
    SD_CONST_UNOP, the value of SD_PROP_POSTAL_ORIENTATIONS must be set appropriately.
    Orientations are set in accordance with the following table:
            0: Omnidirectional
            1: Horizontal (+/- 20Â°)
            2: Vertical (+/- 20Â°)
            3: Horizontal (+/- 20Â°) and Vertical (+/- 20Â°)
    This property is a hint to the decoder. Symbols outside of the specified orientations may sometimes be decoded
    and issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP, and any component in the Postal Symbologies Group
    Example
        *//* enable Vertical postal decoding *//*
    SD_Set(Handle, SD_PROP_POSTAL_ORIENTATIONS, (void *) 2);
    See also*/
    public static final int SD_PROP_POSTAL_RM_MISC = 0x40010815;
    public static final int SD_PROP_POSTAL_SUBREGION_HEIGHT = 0x40010812;
    /*Description
    Sets the height of the subregion over which postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value +1 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the height of the region over which postal symbols will be detected and reported.
    The value is expressed in units of millimeters. Height is measured relative to the starting line specified by
    SD_PROP_POSTAL_SUBREGION_TOP.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's y-axis extent begins
    with the specified top starting line and continues for the remaining height of the image.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any component in the Postal Symbologies Group
            Example
    *//* set 100 millimeter height for the postal symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_POSTAL_SUBREGION_HEIGHT, (void *) 100);
    See also*/
    public static final int SD_PROP_POSTAL_SUBREGION_LEFT = 0x40010809;
    /*Description
    Sets the left edge of the region over which postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value +1 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the left edge of the region over which postal symbols will be detected and reported.
    The value is expressed in units of millimeters. The left edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any component in the Postal Symbologies Group
            Example
    *//* set left edge for postal symbol detection region to 2 mm *//*
    SD_Set(Handle, SD_PROP_POSTAL_SUBREGION_LEFT, (void *) 2);
    See also*/
    public static final int SD_PROP_POSTAL_SUBREGION_TOP = 0x40010810;
    /*Description
    Sets the top edge of the region over which postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value +1 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the top edge of the region over which postal symbols will be detected and reported.
    The value is expressed in units of millimeters. The top edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Note that if a particular symbology is not enabled, then no processing for that symbology will take place,
    regardless
    of this propertyâ€TMs value. Also, this property is ignored in manual operating mode.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any component in the Postal Symbologies Group
            Example
    *//* set top edge for postal symbol detection region to 20 mm *//*
    SD_Set(Handle, SD_PROP_POSTAL_SUBREGION_TOP, (void *) 20);
    See also*/
    public static final int SD_PROP_POSTAL_SUBREGION_WIDTH = 0x40010811;
    /*Description
    Sets the width of the subregion over which postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value +1 has been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the width of the region over which postal symbols will be detected and reported.
    The value is expressed in units of millimeters. Width is measured relative to the starting pixel specified by
    SD_PROP_POSTAL_SUBREGION_LEFT.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's x-axis extent begins
    with the specified left starting pixel continues for the remaining width of the line.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any component in the Postal Symbologies Group
            Example
    *//* set width to 30 millimeters for the postal symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_POSTAL_SUBREGION_WIDTH, (void *) 30);
    See also*/
    public static final int SD_PROP_POSTAL_UNDECODABLE_HEIGHT = 0x40010808;
    /*Description
    Sets the height of the region over which undecodable postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value SD_CONST_UNDECODABLE_POSTAL has been summed into
    SD_PROP_MISC_UNDECODABLE_SYMBOLS.
    The value of this property sets the height of the region over which undecodable postal symbols will be detected
    and reported. The value is expressed in units of millimeters. Height is measured relative to the starting line
    specified by SD_PROP_POSTAL_UNDECODABLE_TOP.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's y-axis extent begins
    with the specified top starting line and continues for the remaining height of the image.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and any component in the Postal Symbologies Group
            Example
    *//* set 100 millimeter height for the undecodable postal symbol detection region *//*
    SD_Set(Handle, SD_PROP_POSTAL_UNDECODABLE_HEIGHT, (void *) 100);
    See also*/
    public static final int SD_PROP_POSTAL_UNDECODABLE_LEFT = 0x40010805;
    /*Description
    Sets the left edge of the region over which undecodable postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value SD_CONST_UNDECODABLE_POSTAL has been summed into
    SD_PROP_MISC_UNDECODABLE_SYMBOLS.
    The value of this property sets the left edge of the region over which undecodable postal symbols will be detected
    and reported. The value is expressed in units of millimeters. The left edge is measured relative to the pixel
    specified by SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and any component in the Postal Symbologies Group
            Example
    *//* set left edge for undecodable postal symbol detection region to 2 mm *//*
    SD_Set(Handle, SD_PROP_POSTAL_UNDECODABLE_LEFT, (void *) 2);
    See also*/
    public static final int SD_PROP_POSTAL_UNDECODABLE_TOP = 0x40010806;
    /*Description
    Sets the top edge of the region over which undecodable postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value SD_CONST_UNDECODABLE_POSTAL has been summed into
    SD_PROP_MISC_UNDECODABLE_SYMBOLS.
    The value of this property sets the top edge of the region over which undecodable postal symbols will be detected
    and reported. The value is expressed in units of millimeters. The top edge is measured relative to the pixel
    specified by SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and any component in the Postal Symbologies Group
            Example
    *//* set top edge for undecodable postal symbol detection region to 5 mm *//*
    SD_Set(Handle, SD_PROP_POSTAL_UNDECODABLE_TOP, (void *) 5);
    See also*/
    public static final int SD_PROP_POSTAL_UNDECODABLE_WIDTH = 0x40010807;
    /*Description
    Sets the width of the region over which undecodable postal symbols will be detected.
    This property is ignored if the value of SD_PROP_POSTAL_ENABLED is SD_CONST_DISABLED. It is also
    ignored unless the value SD_CONST_UNDECODABLE_POSTAL has been summed into
    SD_PROP_MISC_UNDECODABLE_SYMBOLS.
    The value of this property sets the width of the region over which undecodable postal symbols will be detected and
    reported. The value is expressed in units of millimeters. Width is measured relative to the starting pixel specified
    by SD_PROP_POSTAL_UNDECODABLE_LEFT.
    A property value of 0 is equivalent to a property value of infinity, meaning that the region's x-axis extent be-gins
    with the specified left starting pixel and continues for the remaining width of the line.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE and any component in the Postal Symbologies Group
            Example
    *//* set 25 millimeter width for the undecodable postal symbol detection region *//*
    SD_Set(Handle, SD_PROP_POSTAL_UNDECODABLE_WIDTH, (void *) 25);
    See also*/
    //22. QR code properties
    public static final int SD_PROP_QR_ENABLED = 0x40010901;
    public static final int SD_PROP_QR_OUT_OF_SPEC_SYMBOL = 0x40010903;
    public static final int SD_PROP_QR_WITHOUT_QZ = 0x40010905;
    /*Description
    This property specifies whether QR Code and/or Micro QR Code decoding is enabled during the execution of
    SD_Decode or SD_ProgressiveDecode.
    Decoding may be separately enabled or disabled for normal and inverse video symbols and for QR Code and
    Micro QR Code. A normal video symbol is printed in black on a white substrate. An inverse video symbol is
    printed in white on a black substrate.
    The property value is a bit field defined as follows:
    b0: Enable normal video QR Code decoding.
    b1: Enable inverse video QR Code decoding.
    b2: Enable normal video Micro QR Code decoding.
            b3: Enable inverse video Micro QR Code decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, QR
            Example
    *//* enable inverse video QR Code decoding *//*
    SD_Set(Handle, SD_PROP_QR_ENABLED, (void *) (0x02));
    See also*/
    public static final int SD_PROP_QR_NON_SQUARE_MODULES = 0x40010902;
    /*Description
    This property can improve decoding QR Code symbols when the finder patterns are sufficiently non-square.
    The property is ignored when SD_PROP_QR_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. This property should only be used when
    encountering problems decoding symbols with sufficiently non-square modules with this property disabled.
    The property value should be set as follows:
            0: Disable improvements for codes with non-square modules.
            1: Enable improvements for codes with non-square modules.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, QR
            Example
    *//* improve decoding codes with non-square modules *//*
    SD_Set(Handle, SD_PROP_QR_NON_SQUARE_MODULES, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_QR_SYMBOL_SIZE = 0x40010904;
    /*Description
    This property can improve decoding QR Code or Micro QR Code symbols when the length of a symbol side is
    small.
    The property is ignored when SD_PROP_QR_ENABLED is set to SD_CONST_DISABLED. Enabling this
    property is discouraged by Honeywell, unless absolutely necessary. This property should only be used when
    encountering problems decoding smaller QR Code symbols. Note, for proper QR Code decode operation, the
    decoder requires at least 2 pixels per module. This property will not necessarily help symbols whose sample
    density falls below this threshold. It is useful for symbols that have short sides because its sample density is near
    the 2 pixel per module minimum.
    The decoder may require significantly more processor cycles with this property enabled. Customers should
    carefully examine the processor cycle increase and determine that a genuine benefit has been achieved before
    deciding to use this property.
    The property value should be set as follows:
            0: Normal QR Code and Micro QR Code operation.
            1: Handle small QR Code and Micro QR Code symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, QR
            Example
    *//* improve decoding small QR Code symbols *//*
    SD_Set(Handle, SD_PROP_QR_SYMBOL_SIZE, (void *) 1);
    See also*/
    //23. Standard 2 of 5 properties
    public static final int SD_PROP_S25_2SS_ENABLED = 0x40011501;
    /*Description
    This property specifies whether Straight 2 of 5 (with 2 bar start/stop codes) decoding is enabled during the
    execution of SD_Decode.
            Straight 2 of 5 (with 2 bar start/stop codes) is also known as: Standard 2 of 5, IATA 2 of 5, and Airline 2 of 5
    The property value should be set as follows:
            0: Disable Straight 2 of 5 (with 2 bar start/stop codes) decoding.
            1: Enable Straight 2 of 5 (with 2 bar start/stop codes) decoding.
    Also note, neither 1 nor 2 digit Straight 2 of 5 (with 2 bar start/stop codes) symbols will be decoded unless using
    SD_PROP_S25_2SS_LENGTHS. Users are strongly cautioned that enabling these short lengths may increase the
    likelihood of misreads.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, S25
            Example
    *//* enable Straight 2 of 5 (with 2 bar start/stop codes) decoding *//*
    SD_Set(Handle, SD_PROP_S25_2SS_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_S25_2SS_IMPROVE_BOUNDS = 0x40011505;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Straight 2 of 5 (with 2 bar start/stop codes) symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Straight 2 of 5 symbol bounds.
            1: Enable improved Straight 2 of 5 symbol bounds.
    Note: To improve the bounds of a Straight 2 of 5 (with 2 bar start/stop codes) symbol, the amount of time before
    the symbol is issued may be significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, S25
            Example
    *//* enable Straight 2 of 5 (with 2 bar start/stop codes) improved bounds *//*
    SD_Set(Handle, SD_PROP_S25_2SS_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_S25_2SS_LENGTHS = 0x40011502;
    /*Description
    This property specifies which symbol lengths are enabled for Straight 2 of 5 (with 2 bar start/stop codes) decoding
    during the execution of SD_Decode.
    The property value is ignored if Straight 2 of 5 (with 2 bar start/stop codes) decoding is not enabled using
    SD_PROP_S25_2SS_ENABLED. The property value should be set according to the following procedure:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 1 digit symbols are to be decoded, 'OR' in the value 1
    If 2 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 2 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, S25
            Example
    *//* enable all symbol lengths except 1 digit symbols *//*
    SD_Set(Handle, SD_PROP_S25_2SS_LENGTHS, (void *) 0xfffffffe);
    See also*/
    public static final int SD_PROP_S25_3SS_ENABLED = 0x40011503;
    /*Description
    This property specifies whether Straight 2 of 5 (with 3 bar start/stop codes) decoding is enabled during the
    execution of SD_Decode.
    Straight 2 of 5 (with 3 bar start/stop codes) is also known as: Industrial 2 of 5, Code 2 of 5, and Discrete 2 of 5
    The property value should be set as follows:
            0: Disable Straight 2 of 5 (with 3 bar start/stop codes) decoding.
            1: Enable Straight 2 of 5 (with 3 bar start/stop codes) decoding.
    Also note, neither 1 nor 2 digit Straight 2 of 5 (with 3 bar start/stop codes) symbols will be decoded unless enabled
    using SD_PROP_S25_3SS_LENGTHS. Users are strongly cautioned that enabling these short lengths may
    increase the likelihood of misread or "invented" symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, S25
            Example
    *//* enable Straight 2 of 5 (with 3 bar start/stop codes) decoding *//*
    SD_Set(Handle, SD_PROP_S25_3SS_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_S25_3SS_IMPROVE_BOUNDS = 0x40011506;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Straight 2 of 5 (with 3 bar start/stop codes) symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Straight 2 of 5 symbol bounds.
            1: Enable improved Straight 2 of 5 symbol bounds.
    Note: To improve the bounds of a Straight 2 of 5 symbol (with 3 bar start/stop codes), the amount of time before
    the symbol is issued may be significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, S25
            Example
    *//* enable Straight 2 of 5 (with 3 bar start/stop codes) improved bounds *//*
    SD_Set(Handle, SD_PROP_S25_3SS_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_S25_3SS_LENGTHS = 0x40011504;
    /*Description
    This property specifies which symbol lengths are enabled for Straight 2 of 5 (with 3 bar start/stop codes) decoding
    during the execution of SD_Decode.
    The property value is ignored if Straight 2 of 5 (with 3 bar start/stop codes) decoding is not enabled using
    SD_PROP_S25_3SS_ENABLED. One or more of the following values may be summed to enable the
    corresponding length:
    Begin with 32 bit hexadecimal value FFFFFFFC
    If 1 digit symbols are to be decoded, 'OR' in the value 1
    If 2 digit symbols are to be decoded, 'OR' in the value 2
    When writing the property, the 30 most significant bits are ignored. When reading the property, the 30 most
    significant bits are always 1. All lengths greater than 2 are always enabled.
    Property Data Type: int
    Set By: Value
    Initial Value: FFFFFFFC hexadecimal
    Required Components: CORE, S25
            Example
    *//* enable all symbol lengths except 1 digit symbols *//*
    SD_Set(Handle, SD_PROP_S25_3SS_LENGTHS, (void *) 0xfffffffe);
    See also*/

    //24. Telepen properties
    public static final int SD_PROP_TP_ENABLED = 0x40012101;
    /*Description
    This property specifies whether Telepen decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Telepen decoding.
            1: Enable Telepen decoding.
    Note that when Telepen decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, TP
            Example
    *//* enable Telepen decoding *//*
    SD_Set(Handle, SD_PROP_TP_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_TP_IMPROVE_BOUNDS = 0x40012103;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a Telepen symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved Telepen symbol bounds.
            1: Enable improved Telepen symbol bounds.
            Note: To improve the bounds of a Telepen symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, TP
            Example
    *//* enable Telepen improved bounds *//*
    SD_Set(Handle, SD_PROP_TP_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_TP_NUM_START = 0x40012102;
    /*Description
    This property specifies how a Telepen symbol will be converted to text during the execution of SD_Decode.
    The property value should be set as follows:
            0: Convert a Telepen symbol to text according to the AIM specification.
            1: Ignore the starting text encodation and begin in numeric text compaction when converting a Telepen symbol to
    text.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, TP
            Example
    *//* enable numeric Telepen only *//*
    SD_Set(Handle, SD_PROP_TP_NUMERIC, (void *) SD_CONST_ENABLED);
    See also
    SD_PROP_TP_ENABLED*/
    //25. Trioptic properties
    public static final int SD_PROP_TRIOPTIC_ENABLED = 0x40010307;
    /*Description
    This property specifies whether Trioptic Code 39 decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Trioptic Code 39 decoding.
            1: Enable Trioptic Code 39 decoding.
    Note that when Trioptic Code 39 decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Trioptic Code 39 is a variation of standard Code 39 that uses a different start/stop character and is a fixed length of
        6 data codewords (8 codewords overall counting the start and stop characters). Some decoders output the bar code
    data in a reversed format (ex: if the data content was 123456, the output would be 456123). The decoder does NOT
    reverse the data output. Data is output in the same order as it appears in the actual bar code. Calling software may
    perform any desired reordering before transmitting the results to the final user.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, TRIOPTIC
            Example
    *//* enable Trioptic Code 39 decoding *//*
    SD_Set(Handle, SD_PROP_TRIOPTIC_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_TRIOPTIC_SHORT_MARGIN = 0x40010308;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Trioptic Code
    39 symbols during the execution of SD_Decode or SD_ProgressiveDecode.
    When this property is set to 1, a substandard length quiet zone is allowed on either end (but not both ends) of a
    Trioptic Code 39 symbol.
            When this property is set to 2, no quiet zone check is performed on either end of a prospective Trioptic Code 39
    symbol. There must be enough of a quiet zone to at least terminate the last bar of the Trioptic start/stop character
            ($).
    Enabling this property is discouraged by Honeywell, unless absolutely necessary.
            August 28, 2019 - Page 123Honeywell - ID decoder Properties
    The property value is ignored if Trioptic Code 39 decoding is not enabled using
    SD_PROP_TRIOPTIC_ENABLED.
    The property value should be set as follows:
            0: Normal quiet zones are assumed.
            1: Allow short quiet zone symbols.
            2: No quiet zones are required.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, TRIOPTIC
            Example
    *//* enable short quiet zone for Trioptic Code 39 *//*
    SD_Set(Handle, SD_PROP_TRIOPTIC_SHORT_MARGIN, (void *) SD_CONST_ENABLED);
    See also*/
    //26. UPC/EAN properties
    public static final int SD_PROP_UPC_ENABLED = 0x40011001;
    /*Description
    This property specifies whether UPC/EAN/JAN decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable UPC/EAN/JAN decoding.
            1: Enable UPC/EAN/JAN decoding.
    Note that when UPC/EAN/JAN decoding is enabled in unattended operating mode, the values of properties
    SD_PROP_MISC_MIN_1D_HEIGHT and SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    The properties SD_PROP_UPC_EXPANSION and SD_PROP_UPC_SUPPLEMENTALS affect the expansion of
    UPC-E symbols and the decoding of supplemental symbols respectively.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UPC
            Example
    *//* enable UPC/EAN/JAN decoding *//*
    SD_Set(Handle, SD_PROP_UPC_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_UPC_EXPANSION = 0x40011002;
    /*Description
    This property specifies whether UPC E0 and E1 symbols should be expanded per the UPC specification or left
    unexpanded in the result string (see SD_PROP_RESULT_STRING).
    The property value is ignored if UPC decoding is not enabled using SD_PROP_UPC_ENABLED.
    The property value should be set as follows:
            0: Disable expansion.
            1: Enable expansion.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UPC
            Example
    *//* enable UPC E0 and E1 expansion *//*
    SD_Set(Handle, SD_PROP_UPC_EXPANSION, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_UPC_IMPROVE_BOUNDS = 0x40011006;
    /*Description
    This property specifies whether additional processing should be performed in an attempt to improve the bounds of
    a UPC/EAN/JAN symbol before it is issued.
    The property value should be set as follows:
            0: Disable improved UPC/EAN/JAN symbol bounds.
            1: Enable improved UPC/EAN/JAN symbol bounds.
            Note: To improve the bounds of a UPC/EAN/JAN symbol, the amount of time before the symbol is issued may be
    significantly increased.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UPC
            Example
    *//* enable UPC/EAN/JAN improved bounds *//*
    SD_Set(Handle, SD_PROP_UPC_IMPROVE_BOUNDS, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_UPC_SECURITY = 0x40011008;
    public static final int SD_PROP_UPC_SHORT_MARGIN = 0x40011004;
    /*Description
    This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for UPC symbols
    during the execution of SD_Decode or SD_ProgressiveDecode.
            When this property is set to SD_CONST_SHORT_MARGIN_ONE (1), a substandard length quiet zone is allowed
    on either end (but not both ends) of a UPC symbol. When this property is set to
    SD_CONST_SHORT_MARGIN_BOTH (2), a substandard length quiet zone is allowed on both ends of a UPC
    symbol.
    Substandard length quiet zones are not permitted for UPC-E symbols. Setting this property to any value besides
    SD_CONST_DISABLED (0) is discouraged by Honeywell, unless absolutely necessary. If this property is set to a
    value besides SD_CONST_DISABLED (0), it is highly recommended to set the
    SD_PROP_MISC_LINEAR_BOUNDARY_CHECK property to a value of 2 or greater.
    This property value is ignored if UPC decoding is not enabled using SD_PROP_UPC_ENABLED.
    The property value should be set as follows:
            0: Disallow short quiet zone symbols.
            1: Allow a short quiet zone to exist on at most one side of the symbol.
            2: Allow short quiet zones to exist on both sides of the symbol.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UPC
            Example
    *//* enable one-sided short quiet zone *//*
    SD_Set(Handle, SD_PROP_UPC_SHORT_MARGIN, (void *)SD_CONST_SHORT_MARGIN_ONE);
    See also
    SD_PROP_MISC_LINEAR_BOUNDARY_CHECK*/
    public static final int SD_PROP_UPC_SUPPLEMENTALS = 0x40011003;
    /*Description
    This property specifies whether 2 and 5 digit supplemental symbols to the right of a UPC/EAN/JAN symbol
    should be decoded.
    The property is ignored whenever SD_PROP_UPC_ENABLED is set to SD_CONST_DISABLED.
    The property value should be set as follows:
            0: Disable supplemental decoding.
            1: Enable supplemental decoding.
            3: Enable supplemental decoding and allow a substandard trailing quiet zone on a 2 digit supplemental symbol.
            5: Enable supplemental decoding, and allow supplemental decoding even with an abnormally large gap between
    the rightmost bar of the main symbol and the leftmost bar of the supplemental symbol.
    The substandard trailing quiet zone option allows 2 digit supplemental symbols with a trailing quiet zone smaller
    than 5 modules to be successfully decoded. Users are strongly cautioned that this option should be enabled only
    when it is guaranteed that a 2 digit supplemental symbol will be present, for example, as with periodicals sold at
    retail. When this option is enabled and no 2 digit supplemental symbol is present, there is an increased probability
    that the decoder will "invent" a 2 digit supplemental symbol causing a (partial) misread.
    The large gap option allows decoding of a 2 or 5 digit supplemental symbol with a gap of up to about 35 modules.
    When the option is disabled a gap of up to about 17 modules is allowed. This option cannot be used together with
    the substandard trailing quiet zone option. Users are strongly cautioned that this option should be enabled only
    when absolutely necessary. When this option is enabled and no supplemental symbol is present, there is an
    increased probability that the decoder will "invent" a supplemental symbol causing a (partial) misread.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UPC
            Example
    *//* enable supplemental decoding *//*
    SD_Set(Handle, SD_PROP_UPC_SUPPLEMENTALS, (void *) SD_CONST_ENABLED);
    See also*/
    //27. Progress properties
    public static final int SD_PROP_PROGRESS_CANCEL = 0x40006001;
    /*Description
    This property specifies whether the SD_Decode function should continue processing after a call to
    SD_CB_Progress or SD_CB_Status returns.
    The following values may be set during the execution of the SD_CB_Progress or SD_CB_Status callback function
    to alter the execution of an in-progress SD_Decode function:
    SD_CONST_PROGRESS_CONTINUE: The execution of the decoding function should continue normally. It is
    rarely necessary to explicitly set this property during the SD_CB_Progress callback because the
    SD_PROP_PROGRESS_CANCEL property is automatically set to this value each time SD_Decode returns.
            SD_CONST_PROGRESS_CANCEL: The execution of the decoding function will immediately terminate and the
    decoding function returns giving control back to the caller.
            SD_CONST_PROGRESS_PAUSE: This value is ignored by the SD_Decode function. For the
    SD_ProgressiveDecode function, this value indicates that processing should be suspended and
    SD_ProgressiveDecode returns giving control back to the caller. If SD_ProgressiveDecode was called with the
    SD_CONST_ACQ_INCOMPLETE Phase parameter, processing may be resumed on the same image upon the
    next call to SD_ProgressiveDecode, which is normally made only after the value of the
    SD_PROP_IMAGE_HEIGHT property has been updated. See Section 7.3, "Progressive Decoding Example (in
            'C')", which describes a progressive decoding example.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    static void SD_CB_Progress(int Handle)
    {
        *//* cancel this decode if too much time has elapsed since start *//*
        if (TimeDiff(TimeNow(), StartTime) > MAX_DECODE_TIME)
            SD_Set(Handle, SD_PROP_PROGRESS_CANCEL, (void *) SD_CONST_PROGRESS_CANCEL);
    }
    See also*/
    //28. Version properties
    public static final int SD_PROP_VERSION_BUILD = 0x40008001;
    /*Description
    This property indicates the build number of the software provided.
    The build number should be treated as the third element in a complete version number string of the form #.#.#. For
    example, for software version 4.2.7, the property value would be 7. Normally, Honeywell increments build
    numbers after making very minor changes to the software.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    int Build;
    SD_Get(Handle, SD_PROP_VERSION_BUILD, &Build);
    See also
    SD_PROP_VERSION_PRODUCT_NAME
            SD_PROP_VERSION_CONFIGURATION
    SD_PROP_VERSION_MAJOR
            SD_PROP_VERSION_MINOR*/
    public static final int SD_PROP_VERSION_COMMENTS = 0x60008009;
    /*Description
    This property returns any comment strings associated with the decoder release.
    Each comment string is terminated by a \x00 character and is guaranteed not to contain any embedded \x00
    characters. The final string is terminated by an additional \x00 character. The complete set of comment strings is
    guaranteed to be less than 1000 characters long, including all terminators.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    char Comments[1000], *p;
    SD_Get(Handle, SD_PROP_VERSION_COMMENTS, Comments);
    for (p = Comments; *p; p += strlen(p) + 1)
    printf("%s\n", p);
    See also*/
    public static final int SD_PROP_VERSION_COMPANY_NAME= 0x50008010;
    /*Description
    This property returns the name of the company authoring the decoder release.
    Remarks
    The company name string is terminated by a \x00 character and is guaranteed not to contain any embedded \x00
    characters. The string is guaranteed to be less than 1000 characters long, including its terminator.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    char CompanyName[1000];
    SD_Get(Handle, SD_PROP_VERSION_COMPANY_NAME, CompanyName);
    printf("Company Name = [%s]\n", CompanyName);
    See also*/
    public static final int SD_PROP_VERSION_COMPONENTS_AUTHORIZED = 0x40008002;
    /*Description
    This property indicates which the decoder components are authorized for use.
    See Chapter 2, "The decoder Components" for a table of the decoder components and their associated symbolic
    constants. A decoder component may be present but unauthorized, depending on a customer's business
    arrangements with Honeywell.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value is the 'OR' function of the authorized components' symbolic constant values. For
    example, if components CORE, AGC, and MC are authorized, then the value is SD_CONST_CORE |
    SD_CONST_AGC | SD_CONST_MC. If a component is not authorized, then calling a decoder Function requiring
    the component will result in error SD_ERR_COMPONENT_UNAUTHORIZED. Note that the property value may
    also have undocumented constants 'ORed' in, and these should be ignored.
    Required Components: CORE
            Example
    int Authorized;
    SD_Get(Handle, SD_PROP_VERSION_AUTHORIZED, &Authorized);
    See also*/
    public static final int SD_PROP_VERSION_COMPONENTS_AUTHORIZED_EX = 0x40008013;
    /*Description
    This property extends the SD_PROP_VERSION_COMPONENTS_AUTHORIZED property by 32 bits to provide
    expansion room for new components.
    See Chapter 2, "The decoder Components" for a table of the decoder components and their associated symbolic
    constants. A decoder component may be present but unauthorized, depending on a customer's business
    arrangements with Honeywell.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value is the 'OR' function of the authorized components' symbolic constant values. For
    example, if components M25 and TP are authorized, then the value is SD_CONST_M25 | SD_CONST_TP . If a
    component is not authorized, then calling a decoder Function requiring the component will result in error
    SD_ERR_COMPONENT_UNAUTHORIZED. Note that the property value may also have undocumented
    constants 'ORed' in, and these should be ignored.
    Required Components: CORE
            Example
    int AuthorizedEx;
    SD_Get(Handle, SD_PROP_VERSION_AUTHORIZED_EX, &AuthorizedEx);
    See also*/
    public static final int SD_PROP_VERSION_COMPONENTS_PRESENT = 0x40008003;
    /*Description
    This property indicates which the decoder components are present.
    See Chapter 2, "The decoder Components" for a table of the decoder components and their associated symbolic
    constants. A decoder component may be present but unauthorized, depending on a customer's business
    arrangements with Honeywell. Also, various decoder components are frequently not present in a particular
    customer's release, depending on the customer's stated requirements.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value is the 'OR' function of the authorized components' symbolic constant values. For
    example, if components CORE, AGC, and MC are authorized, then the value is SD_CONST_CORE |
    SD_CONST_AGC | SD_CONST_MC. If a component is not present, then calling a decoder Function requiring the
    component will result in error SD_ERR_COMPONENT_ABSENT. Note that the property value may also have
    undocumented constants 'ORed' in, and these should be ignored.
    Required Components:
    CORE
            Example
    int Present;
    SD_Get(Handle, SD_PROP_VERSION_PRESENT, &Present);
    See also*/
    public static final int SD_PROP_VERSION_COMPONENTS_PRESENT_EX = 0x40008014;
    /*Description
    This property extends the SD_PROP_VERSION_COMPONENTS_PRESENT property by 32 bits to provide
    expansion room for new components.
    See Chapter 2, The decoder Components for a table of the decoder components and their associated symbolic
    constants. A decoder component may be present but unauthorized, depending on a customer's business
    arrangements with Honeywell. Also, various decoder components are frequently not present in a particular
    customer's release, depending on the customer's stated requirements.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value is the 'OR' function of the present components' symbolic constant values. For
    example, if components M25 and TP are present, then the value is SD_CONST_M25 | SD_CONST_TP . If a
    component is not present, then calling a decoder Function requiring the component will result in error
    SD_ERR_COMPONENT_ABSENT. Note that the property value may also have undocumented constants 'ORed'
    in, and these should be ignored.
    Required Components: CORE
            Example
    int PresentEx;
    SD_Get(Handle, SD_PROP_VERSION_PRESENT_EX, &PresentEx);
    See also*/
    public static final int SD_PROP_VERSION_CONFIGURATION=0x50008004;
    /*Description
    This property returns the configuration string associated with this version of the decoder.
    The configuration string is terminated by a x00 character and is guaranteed not to contain any embedded x00
    characters. The string is guaranteed to be less than 1000 characters long, including its terminator.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    char Configuration[1000];
    SD_Get(Handle, SD_PROP_VERSION_CONFIGURATION, Configuration);
    printf("Configuration = [%s]", Configuration);
    See also
    SD_PROP_VERSION_PRODUCT_NAME
            SD_PROP_VERSION_MAJOR
    SD_PROP_VERSION_MINOR
            SD_PROP_VERSION_BUILD*/
    public static final int SD_PROP_VERSION_COPYRIGHTS=0x60008005;
    /*Description
    This property returns the legal copyright strings for the decoder release.
    Each copyright string is terminated by a \x00 character and is guaranteed not to contain any embedded \x00
    characters. The final string is terminated by an additional \x00 character. The complete set of copyright strings is
    guaranteed to be less than 1000 characters long, including all terminators.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    Example
    char Copyrights[1000], *p;
    SD_Get(Handle, SD_PROP_VERSION_COPYRIGHTS, Copyrights);
    for (p = Copyrights; *p; p += strlen(p) + 1)
    printf("%s\n", p);
    See also*/
    public static final int SD_PROP_VERSION_MAJOR = 0x40008006;
    /*Description
    This property indicates the major version number of the software provided.
    The major version number should be treated as the first element in a complete version number string of the form
#.#.#. For example, for software version 4.2.7, the property value would be 4. As of version 7.5.0, the major
    version number will contain the year the software was released.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    int Major;
    SD_Get(Handle, SD_PROP_VERSION_MAJOR, &Major);
    See also
    SD_PROP_VERSION_PRODUCT_NAME
            SD_PROP_VERSION_CONFIGURATION
    SD_PROP_VERSION_MINOR
            SD_PROP_VERSION_BUILD*/
    public static final int SD_PROP_VERSION_MINOR = 0x40008007;
    /*Description
    This property indicates the minor version number of the software provided.
    The minor version number should be treated as the second element in a complete version number string of the form
#.#.#. For example, for software version 4.2.7, the property value would be 2. As of version 7.5.0, the minor
    version number will contain the month the software was released.
    Property Data Type: int
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    int Minor;
    SD_Get(Handle, SD_PROP_VERSION_MINOR, &Minor);
    See also
    SD_PROP_VERSION_PRODUCT_NAME
            SD_PROP_VERSION_CONFIGURATION
    SD_PROP_VERSION_MAJOR
            SD_PROP_VERSION_BUILD*/
    public static final int SD_PROP_VERSION_PRODUCT_NAME = 0x50008011;
   /* Description
    This property returns the name of the decoder product.
    The product name string is terminated by a x00 character and is guaranteed not to contain any embedded x00
    characters. The string is guaranteed to be less than 1000 characters long, including its terminator.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    char ProductName[1000];
    SD_Get(Handle, SD_PROP_VERSION_PRODUCT_NAME, ProductName);
    printf("Product Name = [%s]
                   ", ProductName);
           See also
                SD_PROP_VERSION_CONFIGURATION
                   SD_PROP_VERSION_MAJOR
                   SD_PROP_VERSION_MINOR
                   SD_PROP_VERSION_BUILD
                   28.13. SD_PROP_VERSION_TIMESTAMP, 0x50008012
           Description
                   This property returns the timestamp string for the decoder release.
            The timestamp string is terminated by a \x00 character and is guaranteed not to contain any embedded \x00
                   characters. The string is guaranteed to be less than 1000 characters long, including its terminator. The timestamp
                   string is produced by the 'C' language statement below:
                   char API2Timestamp[] = __DATE__ " " __TIME__;
                   The predefined preprocessor macro __DATE__ is a string literal of the form Mmm dd yyyy. The month name
                   Mmm is the same as for dates generated by the library function asctime declared in TIME.H. The __TIME__
                   macro is a string literal of the form hh:mm:ss. Thus, the full timestamp string is of the form Mmm dd yyyy
                   hh:mm:ss.
            Property Data Type: char *
                   Set By: Not applicable. The property is read-only.
            Initial Value: The property value varies according to the version.
            Required Components: CORE
                   Example
                   char Timestamp[1000];
                   SD_Get(Handle, SD_PROP_VERSION_TIMESTAMP, Timestamp);
    printf("Timestamp = [%s]\n", Timestamp);
    See also*/
    public static final int SD_PROP_VERSION_TRADEMARKS =0x60008008;
    /*Description
    This property returns the legal trademark strings for the decoder release.
    Each trademark string is terminated by a \x00 character and is guaranteed not to contain any embedded \x00
    characters. The final string is terminated by an additional \x00 character. The complete set of trademark strings is
    guaranteed to be less than 1000 characters long, including all terminators.
    Property Data Type: char *
    Set By: Not applicable. The property is read-only.
    Initial Value: The property value varies according to the version.
    Required Components: CORE
            Example
    char Trademarks[1000], *p;
    SD_Get(Handle, SD_PROP_VERSION_TRADEMARKS, Trademarks);
    for (p = Trademarks; *p; p += strlen(p) + 1)
    printf("%s\n", p);
    See also
    public static final int GS1 Databar properties
            Description
    Formerly known as RSS*/
    public static final int SD_PROP_RSS14_IMPROVE_BOUNDS = 0x40011302;
    public static final int SD_PROP_RSSEXP_IMPROVE_BOUNDS = 0x40011304;
    public static final int SD_PROP_RSSLIM_IMPROVE_BOUNDS = 0x40011303;
    public static final int SD_PROP_RSS_ENABLED = 0x40011301;
    /*Description
    This property specifies whether GS1 Databar (aka Reduced Space Symbology or RSS) decoding is enabled during
    the execution of SD_Decode.
    The property value is a bit field defined as follows:
            b0: Enable GS1 Databar Expanded decoding.
            b1: Enable GS1 Databar Expanded Stacked decoding.
            b2: Enable GS1 Databar Limited decoding.
            b3: Enable GS1 Databar Omnidirectional and GS1 Databar truncated decoding.
            b4: Enable GS1 Databar Stacked Omnidirectional and GS1 Databar Stacked decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, RSS
            Example
    *//* enable decoding of all GS1 Databar variants *//*
    SD_Set(Handle, SD_PROP_RSS_ENABLED, (void *) 0x1F);
    See also*/
    //30. Maxicode properties
    public static final int SD_PROP_MC_ENABLED = 0x40010601;
    /*Description
    This property specifies whether MaxiCode decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value is a bit field defined as follows:
    b0: Enable Mode 0 decoding.
            b1: Enable Mode 1 decoding.
            b2: Enable Mode 2 decoding.
            b3: Enable Mode 3 decoding.
            b4: Enable Mode 4 decoding.
            b5: Enable Mode 5 decoding.
            b6: Enable Mode 6 decoding.
    With the publication of the International Symbology Specification - MaxiCode, modes 0 and 1 have been declared
    obsolete. However, mode 0 still appears in use frequently.
    Note that when MaxiCode decoding is enabled in unattended operating mode, the values of property
    SD_PROP_IMAGE_TYP_DENSITY must be set appropriately.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, MC
            Example
    *//* enable MaxiCode modes 2 through 5 *//*
    SD_Set(Handle, SD_PROP_MC_ENABLED, (void *) 0x3c);
    See also*/
    public static final int SD_PROP_MC_MESSAGE_FORMAT = 0x40010603;
    /*Description
    This property specifies whether the secondary message of a Maxicode is decoded:
            0: Primary Message Only
            1: Primary + Secondary (if avail)
            2: Primary + Secondary Required
    Initial value: 1
    See also*/
    public static final int SD_PROP_MC_SYMBOL_SIZE = 0x40010602;
    //31. Label Code properties
    public static final int SD_PROP_LC_ENABLED = 0x40014001;
    //32. Hanxin properties
    public static final int SD_PROP_HX_ENABLED = 0x40013601;
    /*Description
    This property specifies whether Han Xin Code decoding is enabled during the execution of SD_Decode or
    SD_ProgressiveDecode.
    The property value should be set as follows:
            0: Disable Han Xin Code decoding.
            1: Enable Han Xin Code decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, HX
            Example
    *//* Enable Han Xin Code decoding *//*
    SD_Set(Handle, SD_PROP_HX_ENABLED, (void *) SD_CONST_ENABLED);
    See also*/
    //33. Aztec properties
    public static final int SD_PROP_AZ_ENABLED = 0x40011201;
    /*Description
    This property specifies whether Aztec Code decoding is enabled during the execution of SD_Decodeor
    SD_ProgressiveDecode.
    Decoding may be separately enabled or disabled for normal and inverse video symbols. A normal video symbol is
    printed in black on a white substrate. An inverse video symbol is printed in white on a black substrate.
    The property value is a bit field defined as follows:
    b0: Enable normal video Aztec decoding
    b1: Enable inverse video Aztec decoding
    b2: Enable Compact Aztec Code decoding
    b3: Enable Full-Size Aztec Code decoding
    For example, to decode only Full-Size inverse video Aztec Codes, set the property value to 10. The addends for
    Compact and/ or Full-Size decoding are hints to the decoder, and it is not guaranteed that these will be the only
    Aztec Code symbols issued.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, AZ
            Example
    *//* enable inverse video Aztec Code decoding only, and give Full-Size hint *//*
    SD_Set(Handle, SD_PROP_AZ_ENABLED, (void *) 0x0a);
    See also*/
    public static final int SD_PROP_AZ_SYMBOL_SIZE = 0x40011202;
    /*Description
    This property enables the detection of smaller Aztec symbols. The decoder processing time may be increased
    when this property is enabled.
    The property value should be set as follows:
            0: Normal Aztec operation.
                1: Handle smaller Aztec symbols.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, AZ
    See also*/
    //34. Grid Matrix properties
    public static final int SD_PROP_GM_ENABLED = 0x40015001;
    /*Description
    This property specifies whether Grid Matrix decoding is enabled during the execution of SD_Decode.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, GM
            Example
    *//* enable Grid Matrix decoding *//*
    SD_Set(Handle, SD_PROP_GM_ENABLED, (void *) 1);
    See also*/
    //35. GS1 Composite properties
    public static final int SD_PROP_CC_ENABLED = 0x40011401;
    /*Description
    This property specifies whether Composite Code and TCL39 decoding are enabled during the execution of the
    Decode.
    The property value is a bit field defined as follows:
    b0: Enable Composite Code decoding.
    b1: Enable TLC39 decoding.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, PDF
            Example
    *//* enable Composite Code decoding *//*
    SD_Set(Handle, SD_PROP_CC_ENABLED, (void *) 0x01);
    See also*/
    //36. Image properties
    public static final int SD_PROP_IMAGE_HEIGHT = 0x40004001;
    /*Description
    This property specifies the height (in lines) of an image to be processed.
    This property must be set prior to calling functions that need to know the height of the image to be processed, such
    as SD_Decode. Although it is not usually necessary, the property may be read back.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    unsigned image[480][640 + 384]; *//* 384 unused bytes per line *//*
    SD_Set(Handle, SD_PROP_IMAGE_HEIGHT, (void *) 480);
    See also
    SD_PROP_IMAGE_WIDTH
            SD_PROP_IMAGE_LINE_DELTA*/
    public static final int SD_PROP_IMAGE_LINE_DELTA = 0x40004002;
   /* Description
    This property specifies the separation (in bytes) between a pixel on one line, and the vertically adjacent pixel on
    the next line.
    This property must be set prior to calling functions that need to know the line-to-line separation of the image to be
    processed, such as SD_Decode. This value is often, but not always, the same as the value of
    SD_PROP_IMAGE_WIDTH. It may be more when there are "dummy" pixels stored along with the "active" pixels
    on each line. Although it is not usually necessary, the property may be read back.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    unsigned image[480][640 + 384]; *//* 384 unused bytes per line *//*
    SD_Set(Handle, SD_PROP_IMAGE_LINE_DELTA, (void *) 1024);
    See also
    SD_PROP_IMAGE_WIDTH
            SD_PROP_IMAGE_HEIGHT*/
    public static final int SD_PROP_IMAGE_MIRRORED = 0x40004003;
    /*Description
    This property specifies whether the image to be processed is stored as a mirror image in memory.
    If the first pixel of a stored image is displayed as the upper left corner of the image, and the last pixel of a stored
    image is displayed as the lower right corner of the image, then the image is considered mirrored if, for example,
    text in the image appears in mirrored form on the display. Otherwise it is considered a normal (non-mirrored)
    image. Note the .BMP format images are usually stored from bottom line to top line in a file, and are therefore
    stored as mirror images.
    Also note that while image mirroring is irrelevant for 1D (i.e. linear) symbols, it is significant for 2D symbols.
    Some postal symbols can misdecode when mirroring is incorrectly set.
    The property value should be set as follows:
            0: Disable mirrored processing; the image is a normal image
            1: Enable mirrored processing
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    unsigned image[480][640 + 384]; *//* 384 unused bytes per line *//*
    fread(image, 1024, 480, BmpFile); *//* .BMP files are in mirrored order *//*
    SD_Set(Handle, SD_PROP_IMAGE_MIRRORED, (void *) SD_CONST_ENABLED);
    See also*/
    public static final int SD_PROP_IMAGE_POINTER = 0x40004004;
    /*Description
    This property is used to set a pointer to the first (i.e. topmost, leftmost) pixel of an image.
    This property must be set prior to calling functions that need to access the image to be processed, such as
    SD_Decode. Although it is not usually necessary, the property may be read back. The image format must conform
    to the following rules:
            - The image is stored as a sequence (i.e. array) of bytes
            - There is one byte per pixel
            - Consecutive pixels on a line are stored in consecutive pixels in memory
            - Consecutive lines are stored consecutively in memory (with an optional fixed length gap).
            - Lower numeric values (i.e. values near 0) represent darker, (i.e. lower reflectance) pixels, and higher numeric
    values (i.e. values near 255) represent lighter (i.e. higher reflectance) pixels2
    Property Data Type: unsigned char *
    Set By: Reference
    Initial Value: 0
    Required Components: CORE
            Example
    unsigned char image[480][640 + 384]; *//* 384 unused bytes per line *//*
    SD_Set(Handle, SD_PROP_IMAGE_POINTER, (void *) image);
    See also
    SD_PROP_IMAGE_WIDTH
            SD_PROP_IMAGE_HEIGHT
    SD_PROP_IMAGE_LINE_DELTA*/
    public static final int SD_PROP_IMAGE_RATIO_X = 0x40004007;
    /*Description
    This property is used to set a sampling density along the x-axis that differs by a known factor from the typical (or
                                                                                                                              nominal) sampling density of an image.
    This property is expressed in percent, and defaults to 100, meaning that the sampling density along the x-axis is
1.00 times the typical sampling density set by the SD_PROP_IMAGE_TYP_DENSITY property. It is primarily
    used when images are obtained using a linescan camera and the x-axis sampling density changes with package
    height. It should also be used with any camera that has non-square pixel pitch. If, for example, the sampling
    density along the x-axis for a particular package is 1.5 times the typical sampling density, then the property value
    should be set to 150.
    The x-axis runs in the direction in which the image pixels are stored in memory.
    The performance of the decoder degrades when the x-axis and y-axis sampling densities are very different.
    Normally, it is not advisable to allow these sampling densities to vary by more than a factor of 2. Some
    symbologies are not especially tolerant of x-axis and y-axis ratios that deviate significantly from 1.0. Consult
    Honeywell if the property value will be less than 0.8 or greater than 1.2.
    Property Data Type: int
    Set By: Value
    Initial Value:100
    Required Components: CORE, UNOP
            Example
    *//* Set up for 150% of typical density on x-axis *//*
    SD_Set(Handle, SD_PROP_IMAGE_RATIO_X, (void *) 150);
    See also
    SD_PROP_IMAGE_TYP_DENSITY
            SD_PROP_IMAGE_RATIO_Y*/
    public static final int SD_PROP_IMAGE_RATIO_Y = 0x40004008;
    /*Description
    This property is used to set a sampling density along the y-axis that differs by a known factor from the typical (or
                                                                                                                              nominal) sampling density of an image.
    This property is expressed in percent, and defaults to 100, meaning that the sampling density along the y-axis is
1.00 times the typical sampling density set by the SD_PROP_IMAGE_TYP_DENSITY property. The property
    might be used, for example, with a line scan camera running at a fixed rate and a conveyor belt running at a
    varying, but measurable, rate. It should also be used with any camera that has non-square pixel pitch. If, for
    example, the sampling density along the y-axis for a particular package is 1.5 times the typical sampling density,
    then the property value should be set to 150.
    The y-axis runs perpendicular to the direction in which the image pixels are stored in memory.
    The performance of the decoder degrades when the x-axis and y-axis sampling densities are very different.
    Normally, it is not advisable to allow these sampling densities to vary by more than a factor of 2. Some
    symbologies are not especially tolerant of x-axis and y-axis ratios that deviate significantly from 1.0. Consult
    Honeywell if the property value will be less than 0.8 or greater than 1.2.
    Property Data Type: int
    Set By: Value
    Initial Value: 100
    Required Components: CORE, UNOP
            Example
    *//* Set up for 150% of typical density on y-axis *//*
    SD_Set(Handle, SD_PROP_IMAGE_RATIO_Y, (void *) 150);
    See also
    SD_PROP_IMAGE_TYP_DENSITY
            SD_PROP_IMAGE_RATIO_Y*/
    public static final int SD_PROP_IMAGE_SEARCH_CENTER_X = 0x40004010;
    /*Description
    See also
    SD_PROP_IMAGE_SEARCH_CENTER_Y*/
    public static final int SD_PROP_IMAGE_SEARCH_CENTER_Y = 0x40004011;
    /*Description
    See also
    SD_PROP_IMAGE_SEARCH_CENTER_X*/
    public static final int SD_PROP_IMAGE_TYP_DENSITY = 0x40004006;
    /*Description
    This property is used to set the typical (or nominal) sampling density of an image in units of samples per meter.
    This property must be set prior to calling SD_Decode or SD_ProgressiveDecode when the operating mode
    specified by SD_PROP_MISC_OP_MODE is unattended mode, and certain symbologies are enabled (e.g. a postal
symbology). Situations requiring this value to be set are noted throughout this document.
    Property Data Type: int
    Set By: Value
    Initial Value: 5039 (i.e. 128 samples per inch)
    Required Components: CORE, UNOP
            Example
    *//* Set for 200 dots per inch *//*
    SD_Set(Handle, SD_PROP_IMAGE_TYP_DENSITY, (void *) (int) (39.37 * 200 *//* dpi *//*));
    See also
    SD_PROP_MISC_OP_MODE
            SD_PROP_IMAGE_RATIO_X
    SD_PROP_IMAGE_RATIO_Y*/
    public static final int SD_PROP_IMAGE_WIDTH = 0x40004005;
    /*Description
    This property is used to set the width (in pixels) of an image.
    This property must be set prior to calling functions that need to know the width of the image to be processed, such
    as SD_Decode. This value is often, but not always, the same as the value of SD_PROP_IMAGE_LINE_DELTA.
    It may be less when there are "dummy" pixels stored along with the "active" pixels on each line. Although it is not
    usually necessary, the property may be read back.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    unsigned image[480][640 + 384]; *//* 384 unused bytes per line *//*
    SD_Set(Handle, SD_PROP_IMAGE_WIDTH, (void *) 640);
    See also
    SD_PROP_IMAGE_LINE_DELTA
            SD_PROP_IMAGE_HEIGHT*/
    //37. DotCode properties
    public static final int SD_PROP_DOTCODE_ENABLED = 0x40016001;
    /*Description
    This property specifies whether DotCode decoding is enabled during the execution of SD_Decode.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DOTCODE
            Example
    *//* enable DotCode decoding *//*
    SD_Set(Handle, SD_PROP_DOTCODE_ENABLED, (void *) 1);
    See also*/
    public static final int SD_PROP_DOTCODE_EXTENSIVE_SEARCH = 0x40016004;
    /*Description
    This property allows a more extensive search of a DotCode barcode. It helps to decode bad printed barcode but
    increases DotCode CPU usage.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DOTCODE
            Example
    *//* enable DotCode extensive search *//*
    SD_Set(Handle, SD_PROP_DOTCODE_EXTENSIVE_SEARCH, (void *) 1);
    See also*/
    public static final int SD_PROP_DOTCODE_SEARCH_AREA = 0x40016008;
    /*        Description
    This property allows to select the size of the search area for Dotcode:
            0: (small) a centered window of 400x300 pixels (max)
            1: (medium) a centered window of 640x400 pixels (max)
            2: (full) the full image
    If a ROI mode is used, the search area is applied on the ROI.
    The decode duration increases with the search area. It is recommended to keep the search area as small as possible
            (ROI mode, especially mode 2, can also be used for this).
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, DOTCODE
    See also
    SD_PROP_ROI_MODE*/
    //38. Monocolor properties
    public static final int SD_PROP_MONOCOLOR_CORRECTION_ENABLE = 0x40100007;
    /*Description
    Monocolor is a technology developed by Honeywell where inside of a monochrome (black and white) image,
    every 4th pixel of every 4th row is color pixel. The color pixels are ordered in a bayer pattern throughout the
    image, similar to how a color imager would have them ordered, except these pixels are spaced out as described.
    Since the decoder is expecting a monochrome image, it is necessary to convert these color pixels into monochrome
    so that the information in the color pixel locations is pertinent to decoding. This setting controls that.
            0 = Monocolor correction disabled
            1 = Monocolor correction enabled
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE
            Example
    SD_Set(Handle, SD_PROP_MONOCOLOR_CORRECTION_ENABLE, (void *) 1); // Enable Monocolor
    conversion to Monochrome
    See also
    SD_PROP_MONOCOLOR_CORRECTION_OFFSETX
            SD_PROP_MONOCOLOR_CORRECTION_OFFSETY
    SD_PROP_MONOCOLOR_CORRECTION_SPACINGX
            SD_PROP_MONOCOLOR_CORRECTION_SPACINGY*/
    //39. Linear properties
    public static final int SD_PROP_LINEAR_SUBREGION_HEIGHT = 0x40012004;
    /*Description
    Sets the height of the subregion over which linear symbols (including PDF417 and MicroPDF417) will be
    detected.
    This property is ignored if all linear symbology decoding is disabled. It is also ignored unless the value +2 has
    been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the height of the region over which linear symbols will be detected and reported.
    The value is expressed in units of millimeters. Height is measured relative to the starting line specified by
    SD_PROP_LINEAR_SUBREGION_TOP.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's y-axis extent begins
    with the specified top starting line and continues for the remaining height of the image.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any linear symbologies group component
            Example
    *//* set 100 millimeter height for the linear symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_LINEAR_SUBREGION_HEIGHT, (void *) 100);
    See also*/
    public static final int SD_PROP_LINEAR_SUBREGION_LEFT = 0x40012001;
   /* Description
    Sets the left edge of the region over which linear symbols will be detected.
    This property is ignored if all linear symbology decoding is disabled. It is also ignored unless the value +2 has
    been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the left edge of the region over which linear symbols will be detected and reported.
    The value is expressed in units of millimeters. The left edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any linear symbologies group component
            Example
    *//* set left edge for linear symbol detection region to 2 mm *//*
    SD_Set(Handle, SD_PROP_LINEAR_SUBREGION_LEFT, (void *) 2);
    See also*/
    public static final int SD_PROP_LINEAR_SUBREGION_TOP = 0x40012002;
    /*Description
    Sets the top edge of the region over which linear symbols will be detected.
    This property is ignored if all linear symbology decoding is disabled. It is also ignored unless the value +2 has
    been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the top edge of the region over which linear symbols will be detected and reported.
    The value is expressed in units of millimeters. The top edge is measured relative to the pixel specified by
    SD_PROP_IMAGE_POINTER.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any linear symbologies group component
            Example
    *//* set top edge for linear symbol detection region to 20 mm *//*
    SD_Set(Handle, SD_PROP_LINEAR_SUBREGION_TOP, (void *) 20);
    See also*/
    public static final int SD_PROP_LINEAR_SUBREGION_WIDTH = 0x40012003;
    /*Description
    Sets the width of the subregion over which linear symbols will be detected.
    This property is ignored if all linear symbology decoding is disabled. It is also ignored unless the value +2 has
    been summed into SD_PROP_MISC_SUBREGION_PROCESSING.
    The value of this property sets the width of the region over which linear symbols will be detected and reported.
    The value is expressed in units of millimeters. Width is measured relative to the starting pixel specified by
    SD_PROP_LINEAR_SUBREGION_LEFT.
    A property value of 0 is equivalent to a property value of infinity, meaning that the subregion's x-axis extent begins
    with the specified left starting pixel and continues for the remaining width of the line.
    Property Data Type: int
    Set By: Value
    Initial Value: 0
    Required Components: CORE, UNOP and any linear symbologies group component
            Example
    *//* set width to 30 millimeters for the linear symbol detection subregion *//*
    SD_Set(Handle, SD_PROP_LINEAR_SUBREGION_WIDTH, (void *) 30);
    See also*/
}
