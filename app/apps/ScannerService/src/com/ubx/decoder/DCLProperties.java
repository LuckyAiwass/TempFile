package com.ubx.decoder;

public class DCLProperties {

    /* enable combination of Aztec append codes
    Description
    This property specifies whether Aztec barcodes that contain structured append information are combined into a
    single result.
    When enabled, in order to obtain a result, all pieces of the structured append must be decodable in the same image.
    The property value should be set as follows:
            0: Disable combination of structured append codes.
            1: Enable combination of structured append codes.
    */
    public static final int DEC_AZTEC_APPEND_ENABLED= 0x1a027004;


    /**
     * Description
     *     This property specifies whether Aztec barcodes that contain structured append information have the information
     *     removed before issuing a result.
     *     By enabling this property, the position ID information will be kept where they are encoded in the barcode data. If
     *     DEC_AZTEC_APPEND_ENABLED is enabled, this information will be shown at the beginning of each barcode.
     *     The property value should be set as follows:
     *             0: Does not remove append information.
     *             1: Removed append information
     *     Initial Value: 1
     */
    public static final int DEC_AZTEC_APPEND_STRIP_INFO=  0x1a027005;
    /**
     * Description
     *     This property specifies whether Aztec decoding is enabled during the execution of Decode.
     *     Decoding may be separately enabled or disabled for normal and inverse video symbols. A normal video symbol is
     *     printed in black on a white substrate. An inverse video symbol is printed in white on a black substrate.
     *     The property value is a bit field defined as follows:
     *     b0: Enable normal video Aztec decoding
     *     b1: Enable inverse video Aztec decoding
     *     b2: Enable Compact Aztec Code decoding
     *     b3: Enable Full-Size Aztec Code decoding
     *     For example, to decode only Full-Size inverse video Aztec Codes, set the property to 10. The addends for Compact
     *     and/or Full-Size decoding are hints to the Decoder, and it is not guaranteed that these will be the only Aztec Code
     *     symbols issued.
     *     Initial value: 0
     */
    public static final int DEC_AZTEC_ENABLED = 0x1a027001;
    /**
     * Description
     *     This property controls the maximum length an Aztec Code result must be to be issued.
     *     This property must to be greater than or equal to DEC_AZTEC_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     This property does not apply to reader configuration barcodes.
     *     Initial value: 3832
     */
    public static final int DEC_AZTEC_MAX_LENGTH= 0x1a027003;
    /**
     * Description
     *     This property controls the minimum length an Aztec Code result must be to be issued.
     *             September 3, 2018 - Page 2Honeywell - DCL Properties
     *     This property must be less than or equal to DEC_AZTEC_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     This property does not apply to reader configuration barcodes.
     *     Initial value: 1
     */
    public static final int DEC_AZTEC_MIN_LENGTH= 0x1a027002;
    /**
     *  Description
     *     Controls algorithms that aid in decoding of symbols of different sizes.
     *     Initial value: 1
     */
    public static final int DEC_AZTEC_SYMBOL_SIZE= 0x40011202;
    /**
     * Description
     *     This property specifies whether Codablock A decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Codablock A decoding
     * 1: Enable Codablock A decoding
     *     When Codablock A and Code 39 decoding are enabled, there is some danger of mistakenly decoging a damaged
     *     Codeblock A symbol as a Code 39 symbol. Therefore whenever possible, Code 39 decoding should be disabled
     *     when Codablock A decoding is enabled.
     *     Initial Value: 0
     */
    public static final int DEC_CODABLOCK_A_ENABLED= 0x1a030001;
    /**
     * Description
     *     This property specifies the maximum length a Codablock A must be to be issued.
     *     The property must be greater than or equal to DEC_CODABLOCK_A_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 2048
     */
    public static final int  DEC_CODABLOCK_A_MAX_LENGTH= 0x1a030003;
    /**
     * Description
     *     This property specifies the minimum length a Codablock A must be to be issued.
     *     The property must be less than or equal to DEC_CODABLOCK_A_MAX_LENGTH so a result can be issued.
     *     September 3, 2018 - Page 4Honeywell - DCL Properties
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 1
     */
    public static final int DEC_CODABLOCK_A_MIN_LENGTH= 0x1a030002;
    /**
     * Description
     *     This property specifies whether Codablock F decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Codablock F decoding
     * 1: Enable Codablock F decoding
     *     When Codablock F and Code 128 decoding are enabled, there is some danger of mistakenly decoding a damaged
     *     Codablock F symbol as a Code 128 symbol. Therefore whenever possible, Code 128 decoding should be disabled
     *     when Codablock F decoding is enabled.
     *     Initial Value: 0
     */
    public static final int DEC_CODABLOCK_F_ENABLED= 0x1a023001;
    /**
     * Description
     *     This property specifies the maximum length a Codablock F must be to be issued.
     *     The property must be greater than or equal to DEC_CODABLOCK_F_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 60
     */
    public static final int DEC_CODABLOCK_F_MAX_LENGTH= 0x1a023003;
    /**
     * Description
     *     This property specifies the minimum length a Codablock F must be to be issued.
     *     The property must be less than or equal to DEC_CODABLOCK_F_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 1
     */
    public static final int DEC_CODABLOCK_F_MIN_LENGTH= 0x1a023002;
    /**
     * Description
     *     This property specifies how the Code 11 check digit is to be handled during the execution of Decode().
     *     The property value should be set as follows:
     *             0: Two check digits verified.
     *             1: One check digit verified.
     *             2: Two check digits verified and stripped from result data.
     * 3: One check digit verified and stripped from result data.
     *     Initial Value: 0
     */
    public static final int DEC_CODE11_CHECK_DIGIT_MODE= 0x40011802;
    /**
     * Description
     *     This property specifies whether Code 11 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Code 11 decoding
     * 1: Enable Code 11 decoding
     *     Initial Value: 0
     */
    public static final int DEC_CODE11_ENABLED= 0x1a01e001;
    /**
     * Description
     *     This property specifies whether additional processing should be performed in an attempt to improve the bounds of
     *     a Code 11 symbol before a result is issued.
     *     In order to improve the bounds of a Code 11 symbol, the amount of time before the symbol may be significantly
     *     September 3, 2018 - Page 7Honeywell - DCL Properties
     *     increased.
     *     The property value should be set as follows:
     *             0: Disable improved bounds processing.
     *             1: Enable improved bounds processing.
     *     Initial Value: 0
     */
    public static final int DEC_CODE11_IMPROVE_BOUNDS= 0x40011803;
    /**
     * Description
     *     This property specifies the maximum length a Code 11 must be to be issued.
     *     The property must be greater than or equal to DEC_CODE11_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 80
     */
    public static final int DEC_CODE11_MAX_LENGTH= 0x1a01e003;
    /**
     * Description
     *     This property specifies the minimum length a Code 11 must be to be issued.
     *     The property must be less than or equal to DEC_CODE11_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 4
     */
    public static final int DEC_CODE11_MIN_LENGTH= 0x1a01e002;
    /**
     * Description
     *     This property specifies the character to be used as a replacement of the FNC1 character when not in first or second
     *     position.
     *     This applies to code 128 and GS1-128 labels.
     *             Default: 0x1D
     *             Example
     *         set &#039;#&#039; character as FNC1 substitute
     *
            DecodeSet(DEC_C128_FNC1_SUBSTITUTE, (void *) 0x23);
     */
    public static final int DEC_C128_FNC1_SUBSTITUTE= 0x1a014006;
    /**
     * Description
     *     This property specifies whether a "function" codeword, if present, is transmitted in the data for a Code 128 symbol.
     * "Function" codewords are replaced with an equivalent 0x8n value. For example, a FNC1 is replaced with 0x81.
     *     Note that not all Code 128 symbols contain function characters.
     *     This property also applies to GS1-128 symbols, as they are defined by having a FNC1 codeword at the beginning
     *     of the symbol.
     *     The property value should be set as follows:
     *             0: Do not transmit "function" characters.
     * 1: Transmit "function" characters.
     *     Initial Value: 0
     */
    public static final int DEC_C128_FNC_TRANSMIT= 0x1a014007;
    /**
     * Description
     *     This property specifies whether additional processing should be performed in an attempt to improve the
     *     bounds of a Code 128 symbol before a result is issued.
     *     In order to improve the bounds of a Code 128 symbol, the amount of time before the symbol may be
     *     significantly increased.
     *     September 3, 2018 - Page 10Honeywell - DCL Properties
     *     The property value should be set as follows:
     *             0: Disable improved bounds processing.
     *             1: Enable improved bounds processing.
     *     Initial Value: 0
     *     Example
     *         enable Code 128 improve bounds
     *     DecodeSet( DEC_CODE128_IMPROVE_BOUNDS, (void *)DEC_CONST_ENABLED );
     */
    public static final int  DEC_C128_IMPROVE_BOUNDS= 0x40010208;
    /**
     * Description
     *     This property specifies whether ISBT concatenation is enabled.
     *     For full explanation of the requirements for ISBT concatenation, see the United States Industry Consensus
     *     Standard for the Uniform Labeling of Blood and Blood Components using ISBT128 document, or visit
     *     http://iccbba.com.
     *     The property value should be set as follows:
     *             0: Disable ISBT concatenation.
     * 1: Enable ISBT concatenation.
     *     Initial Value: 0
     */
    public static final int DEC_C128_ISBT_ENABLED= 0x1a014005;
    /**
     * Description
     *     This property enables enhancements for reading difficult Code 128 bar codes during the execution of the Decode.
     *     The property value is a bit field defined as follows:
     *     b0: Enable Code 128 Enhancement for reading codes whose bars have inconsistent width from top to bottom.
     *             NOTE: This setting only works when DEC_USE_MLD for Code 128 is disabled. Also, this setting can affect read
     *     performance when DEC_GENERAL_IMPROVEMENTS bit +2 is set.
     *     b1: Enable Code 128 Enhancement for reading codes with extreme bar growth (i.e. over inking)
     *     b2: Enable reading of Code 128 barcodes with Out of Spec Start patterns (1st bar is 1x). NOTE: b0 and b2 cannot
     *     be activated at the same time.
     *     September 3, 2018 - Page 11Honeywell - DCL Properties
     *     Initial Value: 0
     */
    public static final int  DEC_C128_OUT_OF_SPEC_SYMBOL= 0x40010203;
    /**
     * Description
     *     This property specifies whether a Code 128 symbol should be issued when only part of the symbol is present.
     *     This property should only be used if it is absolutely necessary to receive decode results when the full symbol is not
     *     present. This property specifies the minimum number of characters that must be present to issue a partial result.
     *     The minimum characters needed before a partial result is issued is 4 characters. The ModifierEx value will specify
     *     whether the issued result is a partial result.
     *     Note: When issuing partial symbols it is possible that the entire symbol may be issued after a partial result of the
     *     same barcode. Enabling this property may adversely affect read rates on marginal symbols.
     *     Initial Value:0
     */
    public static final int DEC_C128_PARTIAL= 0x40010207;
    /**
     * Description
     *     This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 128
     *     symbols during the execution of Decode().
     *     When enabled, a substandard length margin is allowed on either end of a Code 128 symbol, but not both. This
     *     property must be explicitly set to allow short margins on both ends. Enabling this property is not recommended
     *     unless absolutely necessary.
     *     The property value should be set as follows:
     *             0: Disable short margin decoding.
     * 1: Enable short margin decoding.
     * 2: Enable short margin decoding on both ends.
     *     Initial Value: 1
     */
    public static final int  DEC_C128_SHORT_MARGIN= 0x40010202;
    /**
     * Description
     *     This property specifies whether a Code 128 symbol should be issued if Codablock F is enabled and the symbol
     *     appears to be part of a Codablock F symbol.
     *     This propery should only be used when both DEC_CODE128_ENABLED and
     *     DEC_CODABLOCK_A_ENABLED are both enabled. A Code 128 symbol in which the first codeword is a Start
     *     A character, the second codeword is a SHIFT, CODE B, or CODE C character, and the third codeword is a valid
     *     Codablock F row indicator.
     *     The property value should be set as follows:
     *             0: Issue symbols that may be part of a Codablock F.
     *             1: Do not issue symbols that may be part of a Codablock F.
     *     Initial Value: 1
     */
    public static final int DEC_C128_SUPPRESS_CODABLOCK_CONFLICT= 0x40010206;
    /**
     * Description
     *     This property specifies whether Code 128 codes with append information are combined into a single result.
     *     When enabled, each symbol with Code 128 append information must be decoded 1 at a time and will be
     *     concatenated to any previous data and the CB_ResultNotify() callback will be called.
     *     Once a Code 128 without append information is decoded, all data scanned will be combined into a single result,
     *     and that result will be issued through the CB_Result() callback.
     *     If a different symbology is decoded, all data previous data will be cleared.
     *     Initial Value: 0
     */
    public static final int DEC_CODE128_APPEND_ENABLED= 0x1a014004;
    /**
     * Description
     *     This property specifies whether Code 128 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Code 128 decoding.
     *         1: Enable Code 128 decoding.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE128_ENABLED= 0x1a014001;

    /**
     * Description
     *     This property specifies the maximum length a Code 128 must be to be issued.
     *     The property must be greater than or equal to DEC_CODE128_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 80
     */
    public static final int DEC_CODE128_MAX_LENGTH= 0x1a014003;
    /**
     * Description
     *     This property specifies the minimum length a Code 128 must be to be issued.
     *     The property must be less than or equal to DEC_CODE128_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *             September 3, 2018 - Page 14Honeywell - DCL Properties
     *     Initial Value: 1
     */
    public static final int DEC_CODE128_MIN_LENGTH= 0x1a014002;
    /**
     * Description
     *     This property specifies whether GS1 128 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable GS1 128 decoding
     * 1: Enable GS1 128 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_GS1_128_ENABLED= 0x1a015001;
    /**
     * Description
     *     Maximum data length to issue a result for GS1-128.
     *     Default: 0
     */
    public static final int  DEC_GS1_128_MAX_LENGTH= 0x1a015003;
    /**
     * Description
     *     Minimum data length to issue a result for GS1-128.
     *     Default: 0
     */
    public static final int  DEC_GS1_128_MIN_LENGTH= 0x1a015002;
    /**
     * Description
     *     This property specifies how the Codabar check digit is to be handled during the execution of Decode().
     *     The property value should be set as follows:
     *             0: Disable checksum verification.
     * 1: Enable checksum verification.
     * 2: Enable checksum verification and strip digit from result.
     *     Initial Value: 0
     */
    public static final int DEC_CODABAR_CHECK_DIGIT_MODE= 0x1a01f005;
    /**
     * Description
     *     This property specifies whether Codabar codes with concatenation information are combined into a single result.
     *     When enabled, in order to obtain a result, all pieces of the structured append must be decodable in the same image.
     * 0: Codabar Concatenation Off
     * 1: Codabar Concatenation On
     * 2: Codabar Concatenation Required
     *     Initial Value: 0
     *     Example
     *     enable the combination of Codabar code with concatenation information
     *     DecodeSet( DEC_CODABAR_CONCAT_ENABLED, (void *)0x01);
     */
    public static final int DEC_CODABAR_CONCAT_ENABLED= 0x1a01f007;
    /**
     *Description
     *     This property specifies whether Codabar decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Codabar decoding
     *     September 3, 2018 - Page 17Honeywell - DCL Properties
     * 1: Enable Codabar decoding
     *     Initial Value: 0
     *
     */
    public static final int DEC_CODABAR_ENABLED= 0x1a01f001;
    /**
     * Description
     *     This property specifies whether additional processing should be performed in an attempt to improve the bounds of
     *     a Codabar symbol before a result is issued.
     *     In order to improve the bounds of a Codabar symbol, the amount of time before the symbol may be significantly
     *     increased.
     *     The property value should be set as follows:
     *             0: Disable improved bounds processing.
     *             1: Enable improved bounds processing.
     *     Initial Value: 0
     *     Example
     *     enable Codabar improve bounds
     *     DecodeSet( DEC_CODABAR_IMPROVE_BOUNDS, (void *)DEC_CONST_ENABLED );
     */
    public static final int DEC_CODABAR_IMPROVE_BOUNDS= 0x40010104;
    /**
     * Description
     *     This property specifies the maximum length a Codabar must be to be issued.
     *     The property must be greater than or equal to DEC_CODABAR_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 60
     */
    public static final int DEC_CODABAR_MAX_LENGTH= 0x1a01f003;
    /**
     * Description
     *     This property specifies the minimum length a Codabar must be to be issued.
     *     The property must be less than or equal to DEC_CODABAR_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 4
     */
    public static final int DEC_CODABAR_MIN_LENGTH= 0x1a01f002;
    /**
     * Description
     *     This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Codabar
     *     symbols during the execution of Decode().
     *     When enabled, a substandard length margin is allowed on either end of a Codabar symbol, but not both.
     *     Enabling this property is not recommended unless absolutely necessary.
     *     The property value should be set as follows:
     *             0: Disable short margin decoding.
     * 1: Enable short margin decoding.
     *     Initial Value: 1
     */
    public static final int  DEC_CODABAR_SHORT_MARGIN= 0x40010103;
    /**
     * Description
     *     This property specifies whether the start and stop characters are included with the result data.
     *     With DEC_CODABAR_CONCAT_ENABLED enabled, only the start character from the first code and the stop
     *     character from the last code will be included in the data.
     *     The property value should be set as follows:
     *             0: Start and stop characters are not included.
     *             1: Start and stop characters are included.
     *     Initial Value: 0
     */
    public static final int  DEC_CODABAR_START_STOP_TRANSMIT= 0x1a01f004;
    /**
     * Description
     *     This property specifies whether Vesta decoding for Codabar is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Vesta decoding for Codabar.
     * 1: Enable Vesta decoding for Codabar.
     *     Initial Value: 0
     */
    public static final int  DEC_CODABAR_VESTA= 0xfd000007;
    /**
     * Description
     *     This property specifies whether Code 39 codes with append information are combined into a single result.
     *     When enabled, each symbol with Code 39 append information must be decoded 1 at a time and will be
     *     concatenated to any previous data and the CB_ResultNotify() callback will be called.
     *     Once a Code 39 without append information is decoded, all data scanned will be combined into a single result, and
     *     that result will be issued through the CB_Result() callback.
     *     If a different symbology is decoded, all data previous data will be cleared.
     *     Initial Value: 0
     *     Example
     *     enable the combination of Code 39 code with append information
     *     DecodeSet( DEC_CODE39_APPEND_ENABLED, (void *)DEC_CONST_ENABLED);
     */
    public static final int  DEC_CODE39_APPEND_ENABLED= 0x1a016005;
    /**
     * Description
     *     This property specifies whether base 32 interpretation should be done before the Code 39 result is issued.
     *     A result will be issued regardless of this setting. When enabled, this property will allow 6 characters results to be
     *     translated to their base 32 equivalents.
     *     The property value should be set as follows:
     *             0: Disable.
     *          1: Enable
     *     Initial Value: 0
     *     Example
     *         enable base 32 interpretation for Code 39 symbols
     *     DecodeSet( DEC_CODE39_BASE32_ENABLED, (void *)1);
     */
    public static final int  DEC_CODE39_BASE32_ENABLED= 0x1a016008;
    /**
     * Description
     *     This property specifies how the Code 39 check digit is to be handled during the execution of Decode().
     *     September 3, 2018 - Page 21Honeywell - DCL Properties
     *     The property value should be set as follows:
     *             0: Disable checksum verification.
     * 1: Enable checksum verification.
     * 2: Enable checksum verification and strip digit from result.
     *     Initial Value: 0
     */
    public static final int DEC_CODE39_CHECK_DIGIT_MODE= 0x1a016004;
    /**
     * Description
     *     This property specifies whether Code 39 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Code 39 decoding
     * 1: Enable Code 39 decoding
     *     Initial Value: 0
     */
    public static final int DEC_CODE39_ENABLED= 0x1a016001;
    /**
     * Description
     *     This property specifies whether Code 39 full ASCII decoding is enabled during the execution of Decode().
     *     The property value should be set as follows:
     *             0: Disable full ASCII decoding.
     *             1: Enable full ASCII decoding.
     *     Initial Value: 0
     */
    public static final int DEC_CODE39_FULL_ASCII_ENABLED= 0x1a016006;
    /**
     *  Description
     *     This property specifies whether additional processing should be performed in an attempt to improve the bounds of
     *     a Code 39 symbol before a result is issued.
     *     In order to improve the bounds of a Code 39 symbol, the amount of time before the symbol may be significantly
     *     increased.
     *     The property value should be set as follows:
     *             0: Disable improved bounds processing.
     *             1: Enable improved bounds processing.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE39_IMPROVE_BOUNDS= 0x40010310;
    /**
     * Description
     *     This property specifies the maximum length a Code 39 must be to be issued.
     *     The property must be greater than or equal to DEC_CODE39_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 48
     */
    public static final int DEC_CODE39_MAX_LENGTH= 0x1a016003;
    /**
     * Description
     *     This property specifies the minimum length a Code 39 must be to be issued.
     *     The property must be less than or equal to DEC_CODE39_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 1
     */
    public static final int  DEC_CODE39_MIN_LENGTH= 0x1a016002;
    /**
     * Description
     *     This property specifies whether a Code 39 symbol should be issued when only part of the symbol is present.
     *     This property should only be used if it is absolutely necessary to receive decode results when the full symbol is not
     *     present.
     *     This property specifies the minimum number of characters that must be present to issue a partial result. The
     *     minimum characters needed before a partial result is issued is 4 characters.
     *     The ModifierEx value will specify whether the issued result is a partial result.
     *             Note: When issuing partial symbols it is possible that the entire symbol may be issued after a partial result of the
     *     same barcode.
     *     Enabling this property may adversely affect read rates on marginal symbols.
     *     When DEC_CODE39_FULL_ASCII_ENABLED is disabled, partial results may be issued after either a start or
     *     stop character.
     *     When DEC_CODE39_FULL_ASCII_ENABLED is enabled, partial results will only be issued from the start
     *     character.
     *     Initial Value: 0
     *     Example
     *         issue partial Code 39 symbols with at least 10 characters
     *     DecodeSet( DEC_CODE39_PARTIAL, (void *)10);
     */
    public static final int DEC_CODE39_PARTIAL= 0x40010309;
    /**
     * Description
     *     This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 39
     *     September 3, 2018 - Page 24Honeywell - DCL Properties
     *     symbols during the execution of Decode().
     *     When enabled, a substandard length margin is allowed on either end of a Code 39 symbol, but not both.
     *     This property must be explicitly set to allow short margins on both ends. Enabling this property is not
     *     recommended unless absolutely necessary.
     *     The property value should be set as follows:
     *             0: Disable short margin decoding.
     * 1: Enable short margin decoding.
     * 2: Enable short margin decoding on both ends.
     *     Initial Value: 1
     */
    public static final int DEC_CODE39_SHORT_MARGIN= 0x40010304;
    /**
     * Description
     *     This property specifies whether Code 39 start and stop characters are included with the result data.
     *     Initial Value: 0
     *     Example
     *         enabled transmission of Code 39 start and stop characters
     *     DecodeSet( DEC_CODE39_START_STOP_TRANSMIT, (void *)DEC_CONST_ENABLED);
     */
    public static final int DEC_CODE39_START_STOP_TRANSMIT= 0x1a016007;
    /**
     * Description
     *     This property specifies whether a Code 39 symbol should be issued if Codablock A is enabled and the
     *     symbol appears to be part of a Codablock A symbol.
     *     This property should only be used when both DEC_CODE39_ENABLED and
     *     DEC_CODABLOCK_A_ENABLED are both enabled.
     *     A Code 39 symbol in which the first and last codewords are the same may be part of a Codablock A, and will be
     *     suppressed with this property enabled.
     *     This is not a terribly unique situation, so users are cautioned that perfectly valid Code 39 symbols may be
     *     suppressed.
     *             September 3, 2018 - Page 25Honeywell - DCL Properties
     *     The property value should be set as follows:
     *             0: Issue symbols that may be part of a Codablock A.
     *             1: Do not issue symbols that may be part of a Codablock A.
     *     Initial Value: 1
     */
    public static final int DEC_CODE39_SUPPRESS_CODABLOCK_CONFLICT= 0x40010306;
    /**
     * Description
     *     This property specifies whether Code 39 barcodes with unconventional intercharacter gaps can be read.
     *     The property value should be set as follows:
     *             0: Disable reading of Code 39 barcodes with unconventional intercharacter gaps.
     * 1: Enable reading of Code 39 barcodes with unconventional intercharacter gaps.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE39_UNCONV_INTER_CHAR= 0x40010313;
    /**
     * Description
     *     This property specifies whether Code 39 barcodes with unconventional intercharacter gaps can be read.
     *     The property value should be set as follows:
     *             0: Disable reading of Code 39 barcodes with unconventional intercharacter gaps.
     * 1: Enable reading of Code 39 barcodes with unconventional intercharacter gaps.
     *     Initial Value: 0
     */
    public static final int DEC_FLD_CODE39_UNCONV_INTER_CHAR= 0xfd004009;
    /**
     * Description
     *     This property specifies whether Code 93 codes with append information are combined into a single result.
     *     When enabled, each symbol with Code 93 append information must be decoded 1 at a time and will be
     *     concatenated to any previous data and the CB_ResultNotify() callback will be called.
     *     Once a Code 93 without append information is decoded, all data scanned will be combined into a single result, and
     *     that result will be issued through the CB_Result() callback.
     *     If a different symbology is decoded, all data previous data will be cleared.
     *     Initial Value: 0
     */
    public static final int DEC_CODE93_APPEND_ENABLED= 0x1a01d004;
    /**
     * Description
     *     This property specifies whether Code 93 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Code 93 decoding.
     * 1: Enable Code 93 decoding.
     *     Initial Value: 0
     */
    public static final int DEC_CODE93_ENABLED= 0x1a01d001;
    /**
     * Description
     *     This property improves decoding of high density Code 93 barcodes. Enabling this property increases decoding
     *     time.
     *     The property value should be set as follows:
     *             0: Disable Code 93 high density decoding improvements.
     *     September 3, 2018 - Page 28Honeywell - DCL Properties
     * 1: Enable Code 93 high density decoding improvements.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE93_HIGH_DENSITY= 0x40011104;
    /**
     * Description
     *     This property specifies whether additional processing should be performed in an attempt to improve the bounds of
     *     a Code 93 symbol before a result is issued.
     *     In order to improve the bounds of a Code 93 symbol, the amount of time before the symbol may be significantly
     *     increased.
     *     The property value should be set as follows:
     *             0: Disable improved bounds processing.
     *             1: Enable improved bounds processing.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE93_IMPROVE_BOUNDS= 0x40011103;
    /**
     * Description
     *     This property specifies the maximum length a Code 93 must be to be issued.
     *     The property must be greater than or equal to DEC_CODE93_MIN_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 80
     */
    public static final int DEC_CODE93_MAX_LENGTH= 0x1a01d003;
    /**
     * Description
     *     This property specifies the minimum length a Code 93 must be to be issued.
     *     The property must be less than or equal to DEC_CODE93_MAX_LENGTH so a result can be issued.
     *     There is no internal check to ensure this requirement to allow asynchronous configuration of this property.
     *     Initial Value: 0
     */
    public static final int  DEC_CODE93_MIN_LENGTH= 0x1a01d002;
    /**
     * Description
     *     This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for Code 93
     *     symbols during the execution of Decode().
     *     When enabled, a substandard length margin is allowed on either end of a Code 93 symbol, but not both.
     *             Enabling this property is not recommended unless absolutely necessary.
     *     The property value should be set as follows:
     *             0: Disable short margin decoding.
     * 1: Enable short margin decoding.
     *     Initial Value: 1
     */
    public static final int DEC_CODE93_SHORT_MARGIN= 0x40011102;
    /**
     * Description
     *     This property specifies whether TLC39 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable TLC39 decoding
     * 1: Enable TLC39 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_TLC39_ENABLED= 0x1a017001;
    /**
     * Description
     *     This property specifies whether Hong Kong 2 of 5 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Hong Kong 2 of 5 decoding
     * 1: Enable Hong Kong 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int DEC_HK25_ENABLED= 0x1a02c001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int DEC_HK25_IMPROVE_BOUNDS= 0x40012603;
    /**
     * Description
     *     Maximum data length to issue a result for Hong Kong 2 of 5.
     */
    public static final int  DEC_HK25_MAX_LENGTH= 0x1a02c003;
    /**
     * Description
     *     Minimum data length to issue a result for Hong Kong 2 of 5.
     */
    public static final int  DEC_HK25_MIN_LENGTH= 0x1a02c002;
    /**
     * Description
     *     This property specifies whether HanXin decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable HanXin decoding
     *             1: Enable HanXin decoding
     *     Initial Value: 0
     */
    public static final int  DEC_HANXIN_ENABLED= 0x1a02b001;
    /**
     * Description
     *     Maximum data length to issue a result for Han Xin Code.
     */
    public static final int  DEC_HANXIN_MAX_LENGTH= 0x1a02b003;
    /**
     * Description
     *     Minimum data length to issue a result for Han Xin Code.
     */
    public static final int DEC_HANXIN_MIN_LENGTH= 0x1a02b002;
    /**
     * Description
     *     This property specifies whether Interleaved 2 of 5 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Interleaved 2 of 5 decoding
     * 1: Enable Interleaved 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_I25_ENABLED= 0x1a019001;
    /**
     * Description
     *     This property improves decoding of high density Interleaved 2 of 5 barcodes. Enabling this property increases
     *     decoding time.
     *     The property value should be set as follows:
     *             0: Disable Interleaved 2 of 5 high density decoding improvements.
     *             1: Enable Interleaved 2 of 5 high density decoding improvements.
     *     Initial Value: 0
     */
    public static final int DEC_I25_HIGH_DENSITY= 0x40010507;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_I25_IMPROVE_BOUNDS= 0x40010506;
    /**
     * Description
     *     Maximum data length to issue a result for Interleaved 2 of 5.
     */
    public static final int DEC_I25_MAX_LENGTH= 0x1a019003;
    /**
     * Description
     *     Minimum data length to issue a result for Interleaved 2 of 5.
     */
    public static final int  DEC_I25_MIN_LENGTH= 0x1a019002;
    /**
     * Description
     *     Specifies whether substandard length margins should be allowed.
     *     Initial value: 1
     */
    public static final int  DEC_I25_SHORT_MARGIN= 0x40010504;
    /**
     * Description
     *     This property specifies whether Straight 2 of 5 (with 2 bar start/stop codes) decoding is enabled during the
     *     execution of Decode. This symbology is also called: Standard 2 of 5, IATA 2 of 5, and Airline 2 of 5.
     *             "IATA" was chosen to be the short-hand for this symbology in this API.
     *     The property value should be set as follows:
     *             0: Disable IATA 2 of 5 decoding
     *             1: Enable IATA 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int DEC_IATA25_ENABLED= 0x1a01b001;
    /**
     * Description
     *     Maximum data length to issue a result for IATA 2 of 5.
     */
    public static final int DEC_IATA25_MAX_LENGTH= 0x1a01b003;
    /**
     * Description
     *     Minimum data length to issue a result for IATA 2 of 5.
     */
    public static final int DEC_IATA25_MIN_LENGTH= 0x1a01b002;
    /**
     * Korea Post properties
     * Description
     *     Korea Post check digit transmit enable.
     */
    public static final int  DEC_KOREA_POST_CHECK_DIGIT_TRANSMIT= 0x1a100004;
    /**
     * Description
     *     This property specifies whether Korea Post decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Korea Post decoding
     *             1: Enable Korea Post decoding
     *     Initial Value: 0
     */
    public static final int  DEC_KOREA_POST_ENABLED= 0x1a100001;
    /**
     * Description
     *     Maximum data length to issue a result for Korea Post.
     */
    public static final int  DEC_KOREA_POST_MAX_LENGTH= 0x1a100003;
    /**
     * Description
     *     Minimum data length to issue a result for Korea Post.
     */
    public static final int  DEC_KOREA_POST_MIN_LENGTH= 0x1a100002;
    /**
     * Description
     *     Reverses the data prior to issuing a result for Korea Post.
     *             0: disable
     *             1: enable
     *     Initial Value: 1
     */
    public static final int  DEC_KOREA_POST_REVERSE= 0x40013503;
    /**
     * Description
     *     This property specifies whether Matrix 2 of 5 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Matrix 2 of 5 decoding
     *             1: Enable Matrix 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_M25_ENABLED= 0x1a01c001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_M25_IMPROVE_BOUNDS= 0x40011904;
    /**
     * Description
     *     Maximum data length to issue a result for Matrix 2 of 5.
     */
    public static final int  DEC_M25_MAX_LENGTH= 0x1a01c003;
    /**
     * Description
     *     Minimum data length to issue a result for Matrix 2 of 5.
     */
    public static final int  DEC_M25_MIN_LENGTH= 0x1a01c002;
    /*Description
    This property specifies how Matrix 2 of 5 checksums are to be handled during the execution of SD_Decode .
    The property value is ignored if Matrix 2 of 5 decoding is not enabled using SD_PROP_M25_ENABLED.
    The property value should be set as follows:
            0: Disable checksum checking.
            1: Enable checksum checking.
            2: Enable checksum checking and strip the checksum from the result string.*/
    public static final int SD_PROP_M25_CHECKSUM = 0x40011902;
    /**
     * Description
     *     This property specifies whether MicroPDF417 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable MicroPDF417 decoding
     *             1: Enable MicroPDF417 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_MICROPDF_ENABLED= 0x1a025001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_MICROPDF_IMPROVE_BOUNDS= 0x40010704;
    /**
     * Description
     *     Maximum data length to issue a result for MicroPDF417.
     */
    public static final int DEC_MICROPDF_MAX_LENGTH= 0x1a025003;
    /**
     * Description
     *     Minimum data length to issue a result for MicroPDF417.
     */
    public static final int  DEC_MICROPDF_MIN_LENGTH= 0x1a025002;
    /**
     * Description
     *     This property specifies whether substandard length margins (i.e. quiet zones) should be allowed for MSI Plessey
     *     symbols during the execution of the Decode.
     *             When this property is enabled, a substandard length quiet zone is allowed on either end (but not both ends) of a
     *     MSI Plessey symbol. Enabling this property is discouraged by Honeywell, unless absolutely necessary.
     *     This property value is ignored if MSI Plessey decoding is not enabled using SD_PROP_MSIP_ENABLED.
     *     The property value should be set as follows:
     *             0: Disallow short quiet zone symbols.
     *             1: Allow short quiet zone symbols.
     *     Property Data Type: int
     *     Set By: Value
     *     Initial Value: 0
     */
    public static final int  DEC_MSIP_SHORT_MARGIN= 0x40011604;
    /*Description
    This property specifies how MSI Plessey checksums are to be handled during the execution of the decoder.
    The property value is ignored if MSI Plessey decoding is not enabled.
    The property value should be set as follows:
    0: Disable checksum checking.
    1: Enable a single mod 10 checksum check.
    2: Enable a mod 11 and a mod 10 checksum check.
    3: Enable two mod 10 checksum checks.
    5: Enable a single mod 10 checksum check and strip the checksum
    6: Enable a mod 11 and a mod 10 checksum check and strip the checksums
    7: Enable two mod 10 checksum checks and strip the checksums*/
    public static final int DEC_MSI_CHECK_DIGIT_MODE = 0x40011602;
    /**
     * Description
     *     This property specifies whether MSI decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable MSI decoding
     *             1: Enable MSI decoding
     *     Initial Value: 0
     */

    public static final int  DEC_MSI_ENABLED= 0x1a021001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_MSI_IMPROVE_BOUNDS= 0x40011603;
    /**
     * Description
     *     Maximum data length to issue a result for MSI.
     */
    public static final int  DEC_MSI_MAX_LENGTH= 0x1a021003;
    /**
     * Description
     *     Minimum data length to issue a result for MSI.
     */
    public static final int DEC_MSI_MIN_LENGTH= 0x1a021002;

    /**
     * Description
     *     This property enables enhancements to be able to read Out of Spec MSI barcodes. As the risk of misreads is
     *     increased in case of decoding Out of Spec symbols, it is recommanded to only do this in case of checksums.
     *     The property value should be set as follows:
     *             0: Disable MSI Out of Spec Symbol Enhancements.
     *             1: Enable MSI Enhancement to read symbols with inter character gaps.
     *     Property Data Type: int
     *     Set By: Value
     *     Initial Value: 0
     */
    public static final int  DEC_PROP_MSIP_OUT_OF_SPEC_SYMBOL= 0x40011605;
    /**
     * Description
     *     This property specifies whether NEC 2 of 5 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable NEC 2 of 5 decoding
     *             1: Enable NEC 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_NEC25_ENABLED= 0x1a02f001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int DEC_NEC25_IMPROVE_BOUNDS= 0x40012204;
    /**
     * Description
     *     Maximum data length to issue a result for NEC 2 of 5.
     */
    public static final int DEC_NEC25_MAX_LENGTH= 0x1a02f003;
    /**
     * Description
     *     Minimum data length to issue a result for NEC 2 of 5.
     */
    public static final int DEC_NEC25_MIN_LENGTH= 0x1a02f002;
    /**
     * GS1 Databar properties
     * Formerly known as RSS
     * Description
     *     This property specifies whether GS1 Databar 14 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable GS1 Databar 14 decoding
     *             1: Enable GS1 Databar 14 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_RSS_14_ENABLED= 0x1a022001;
    /**
     * Description
     *     This property specifies whether GS1 Databar Expanded decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable GS1 Databar Expanded decoding
     *             1: Enable GS1 Databar Expanded decoding
     *     Initial Value: 0
     */
    public static final int  DEC_RSS_EXPANDED_ENABLED= 0x1a022003;
    /**
     * Description
     *     Maximum data length to issue a result for GS1-Databar Expanded.
     */
    public static final int  DEC_RSS_EXPANDED_MAX_LENGTH= 0x1a022005;
    /**
     * Description
     *     Minimum data length to issue a result for GS1-Databar Expanded.
     */
    public static final int DEC_RSS_EXPANDED_MIN_LENGTH= 0x1a022004;
    /**
     * Description
     *     This property specifies whether GS1 Databar Limited decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable GS1 Databar Limited decoding
     *             1: Enable GS1 Databar Limited decoding
     *     Initial Value: 0
     */
    public static final int  DEC_RSS_LIMITED_ENABLED= 0x1a022002;

    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_IATA25_IMPROVE_BOUNDS= 0x40011505;
    /**
     * Description
     *     This property specifies whether Straight 2 of 5 (with 3 bar start/stop codes) decoding is enabled during the
     *     execution of Decode.
     *     This symbology is also called: Industrial 2 of 5, Code 2 of 5, and Discrete 2 of 5.
     *             "S" for "Straight" was chosen to be the short-hand for this symbology in this API.
     *     The property value should be set as follows:
     *             0: Disable S 2 of 5 decoding
     *             1: Enable S 2 of 5 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_S25_ENABLED= 0x1a01a001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_S25_IMPROVE_BOUNDS= 0x40011506;
    /**
     * Description
     *     Maximum data length to issue a result for Straight 2 of 5.
     */
    public static final int  DEC_S25_MAX_LENGTH= 0x1a01a003;
    /**
     * Description
     *     Minimum data length to issue a result for Straight 2 of 5.
     */
    public static final int  DEC_S25_MIN_LENGTH= 0x1a01a002;
    /**
     * Description
     *     This property specifies whether Telepen decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Telepen decoding
     *             1: Enable Telepen decoding
     *     Initial Value: 0
     */
    public static final int  DEC_TELEPEN_ENABLED= 0x1a020001;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_TELEPEN_IMPROVE_BOUNDS= 0x40012103;
    /**
     * Description
     *     Maximum data length to issue a result for Telepen.
     */
    public static final int  DEC_TELEPEN_MAX_LENGTH= 0x1a020003;
    /**
     * Description
     *     Minimum data length to issue a result for Telepen.
     */
    public static final int  DEC_TELEPEN_MIN_LENGTH= 0x1a020002;
    /**
     * Description
     *     Converts data using old algorithm prior to issuing a result for Telepen.
     */
    public static final int  DEC_TELEPEN_OLD_STYLE= 0x40012102;
    /**
     * Description
     *     This property specifies whether Trioptic decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable Trioptic decoding
     *             1: Enable Trioptic decoding
     *     Trioptic is a variation of standard Code 39 that uses a different start/stop character and is a fixed length of 6 data
     *     codewords (8 codewords overall counting the start and stop characters).
     *     The Decoder Control Logic will perform the reordering of codewords before calling the result callback.
     *     For example, the data content was 123456, it is output as 456123.
     *     Initial Value: 0
     */
    public static final int DEC_TRIOPTIC_ENABLED= 0x1a018001;
    /**
     * Description
     *     Specifies whether substandard length margins should be allowed.
     *     Initial value: 1
     */
    public static final int  DEC_TRIOPTIC_SHORT_MARGIN= 0x40010308;
    /**
     * Description
     *     Minimum additional time to decode a UPC addenda.
     */
    public static final int  DEC_ADD_SEARCH_TIME_ADDENDA= 0x1a003004;
    /**
     * Description
     *     This property specifies whether a 5 chars addenda is required for EAN 13 starting with &quot;290&quot;.
     *     When enabled, in order to obtain a result, a 5 chars addenda must be decodable in the same image if the EAN13
     *     starts with &quot;290&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Enable
     *     Initial Value: 0
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 290
     *DecodeSet(DEC_EAN13_290_ADDENDA_REQUIRED, (void *)1);
     *See also
     *DEC_EAN13_378_ADDENDA_REQUIRED
     *DEC_EAN13_414_ADDENDA_REQUIRED
     *DEC_EAN13_434_ADDENDA_REQUIRED
     *DEC_EAN13_491_ADDENDA_REQUIRED
     *DEC_EAN13_977_ADDENDA_REQUIRED
     *DEC_EAN13_978_ADDENDA_REQUIRED
     *DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_290_ADDENDA_REQUIRED= 0x1a013009;
    /**
     * Description
     *     EAN-13 2-character addenda enable.
     */
    public static final int DEC_EAN13_2CHAR_ADDENDA_ENABLED= 0x1a013003;

    /**
     * Description
     *     This property specifies whether an addenda is required for EAN 13 starting with &quot;378&quot; or
     * &quot;379&quot;.
     *     When enabled, in order to obtain a result, the addenda specified must be decodable in the same image if the
     *     EAN13 starts with &quot;378&quot; or &quot;379&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Requires 2 chars addenda
     *             2: Requires 5 chars addenda
     *             3: Requires 2 or 5 chars addenda
     *     Initial Value: 0
     *     Example
     *          enable 5 chars addenda for EAN13 codes starting with 378 or 379
     *     DecodeSet( DEC_EAN13_378_ADDENDA_REQUIRED, (void *)2 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_414_ADDENDA_REQUIRED
     *     DEC_EAN13_434_ADDENDA_REQUIRED
     *             DEC_EAN13_491_ADDENDA_REQUIRED
     *     DEC_EAN13_977_ADDENDA_REQUIRED
     *             DEC_EAN13_978_ADDENDA_REQUIRED
     *     DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_378_ADDENDA_REQUIRED= 0x1a01300a;

    /**
     * Description
     *     This property specifies whether an addenda is required for EAN 13 starting with &quot;414&quot; or
     * &quot;419&quot;.
     *     When enabled, in order to obtain a result, the addenda specified must be decodable in the same image if the
     *     EAN13 starts with &quot;414&quot; or &quot;419&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Requires 2 chars addenda
     *             2: Requires 5 chars addenda
     *             3: Requires 2 or 5 chars addenda
     *     Initial Value: 0
     *     September 3, 2018 - Page 52Honeywell - DCL Properties
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 414 or 419
     *     DecodeSet( DEC_EAN13_414_ADDENDA_REQUIRED, (void *)2 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_378_ADDENDA_REQUIRED
     *     DEC_EAN13_434_ADDENDA_REQUIRED
     *             DEC_EAN13_491_ADDENDA_REQUIRED
     *     DEC_EAN13_977_ADDENDA_REQUIRED
     *             DEC_EAN13_978_ADDENDA_REQUIRED
     *     DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_414_ADDENDA_REQUIRED= 0x1a01300b;
    /**
     * Description
     *     This property specifies whether an addenda is required for EAN 13 starting with &quot;434&quot; or
     * &quot;439&quot;.
     *     When enabled, in order to obtain a result, the addenda specified must be decodable in the same image if the
     *     EAN13 starts with &quot;434&quot; or &quot;439&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Requires 2 chars addenda
     *             2: Requires 5 chars addenda
     *             3: Requires 2 or 5 chars addenda
     *     Initial Value: 0
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 434 or 439
     *     DecodeSet( DEC_EAN13_434_ADDENDA_REQUIRED, (void *)2 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_378_ADDENDA_REQUIRED
     *     DEC_EAN13_414_ADDENDA_REQUIRED
     *             DEC_EAN13_491_ADDENDA_REQUIRED
     *     DEC_EAN13_977_ADDENDA_REQUIRED
     *             DEC_EAN13_978_ADDENDA_REQUIRED
     *     DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_434_ADDENDA_REQUIRED= 0x1a01300c;
    /**
     * Description
     *     This property specifies whether an addenda is required for EAN 13 starting with &quot;491&quot;.
     *     When enabled, in order to obtain a result, the addenda specified must be decodable in the same image if the
     *     EAN13 starts with &quot;491&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Requires 2 chars addenda
     *             2: Requires 5 chars addenda
     *             3: Requires 2 or 5 chars addenda
     *     Initial Value: 0
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 491
     *     DecodeSet( DEC_EAN13_491_ADDENDA_REQUIRED, (void *)2 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_378_ADDENDA_REQUIRED
     *     DEC_EAN13_414_ADDENDA_REQUIRED
     *             DEC_EAN13_434_ADDENDA_REQUIRED
     *     DEC_EAN13_977_ADDENDA_REQUIRED
     *             DEC_EAN13_978_ADDENDA_REQUIRED
     *     DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int DEC_EAN13_491_ADDENDA_REQUIRED= 0x1a013010;
    /**
     * Description
     *     EAN-13 5-character addenda enable.
     */
    public static final int  DEC_EAN13_5CHAR_ADDENDA_ENABLED= 0x1a013004;
    /**
     * Description
     *     This property specifies whether a 2 chars addenda is required for EAN 13 starting with &quot;977&quot;.
     *     When enabled, in order to obtain a result, a 2 chars addenda must be decodable in the same image if the EAN13
     *     starts with &quot;977&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Enable
     *     Initial Value: 0
     *     Example
     *         enable 2 chars addenda for EAN13 codes starting with 977
     *DecodeSet(DEC_EAN13_977_ADDENDA_REQUIRED, (void *)1);
     *See also
     *DEC_EAN13_290_ADDENDA_REQUIRED
     *DEC_EAN13_378_ADDENDA_REQUIRED
     *DEC_EAN13_414_ADDENDA_REQUIRED
     *DEC_EAN13_434_ADDENDA_REQUIRED
     *DEC_EAN13_491_ADDENDA_REQUIRED
     *DEC_EAN13_978_ADDENDA_REQUIRED
     *DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_977_ADDENDA_REQUIRED= 0x1a01300d;
    /**
     * Description
     *     This property specifies whether a 5 chars addenda is required for EAN 13 starting with &quot;978&quot;.
     *     When enabled, in order to obtain a result, a 5 chars addenda must be decodable in the same image if the EAN13
     *     starts with &quot;978&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Enable
     *     Initial Value: 0
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 978
     *     DecodeSet( DEC_EAN13_978_ADDENDA_REQUIRED, (void *)1 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_378_ADDENDA_REQUIRED
     *     DEC_EAN13_414_ADDENDA_REQUIRED
     *             DEC_EAN13_434_ADDENDA_REQUIRED
     *     DEC_EAN13_491_ADDENDA_REQUIRED
     *             DEC_EAN13_977_ADDENDA_REQUIRED
     *     DEC_EAN13_979_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_978_ADDENDA_REQUIRED= 0x1a01300e;
    /**
     * Description
     *     This property specifies whether a 5 chars addenda is required for EAN 13 starting with &quot;979&quot;.
     *     When enabled, in order to obtain a result, a 5 chars addenda must be decodable in the same image if the EAN13
     *     starts with &quot;979&quot;.
     *     This property takes precedence on other addenda requirement properties.
     *     The property value should be set as follows:
     *             0: Disable
     *             1: Enable
     *     Initial Value: 0
     *     Example
     *         enable 5 chars addenda for EAN13 codes starting with 979
     *     DecodeSet( DEC_EAN13_979_ADDENDA_REQUIRED, (void *)1 );
     *     See also
     *     DEC_EAN13_290_ADDENDA_REQUIRED
     *             DEC_EAN13_378_ADDENDA_REQUIRED
     *     DEC_EAN13_414_ADDENDA_REQUIRED
     *             DEC_EAN13_434_ADDENDA_REQUIRED
     *     DEC_EAN13_491_ADDENDA_REQUIRED
     *             DEC_EAN13_977_ADDENDA_REQUIRED
     *     DEC_EAN13_978_ADDENDA_REQUIRED
     */
    public static final int  DEC_EAN13_979_ADDENDA_REQUIRED= 0x1a01300f;
    /**
     * Description
     *     Requires reading an addenda for EAN-13 to issue a result.
     */
    public static final int  DEC_EAN13_ADDENDA_REQUIRED= 0x1a013005;
    /**
     * Description
     *     Adds a space between main code and addenda for EAN-13.
     */
    public static final int  DEC_EAN13_ADDENDA_SEPARATOR= 0x1a013006;
    /**
     * Description
     *     This property specifies whether EAN 13 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable EAN 13 decoding
     * 1: Enable EAN 13 decoding
     *     Initial Value: 1
     */
    public static final int  DEC_EAN13_ENABLED= 0x1a013001;
    /**
     * Description
     *     EAN-13 ISBN format handling enable.
     */
    public static final int DEC_EAN13_ISBN_ENABLED= 0x1a013007;
    /**
     * Description
     *     EAN-8 2-character addenda enable.
     */
    public static final int  DEC_EAN8_2CHAR_ADDENDA_ENABLED= 0x1a012003;
    /**
     * Description
     *     EAN-8 5-character addenda enable.
     */
    public static final int  DEC_EAN8_5CHAR_ADDENDA_ENABLED= 0x1a012004;
    /**
     * Description
     *     Requires reading an addenda for EAN-8 to issue a result.
     */
    public static final int  DEC_EAN8_ADDENDA_REQUIRED= 0x1a012005;
    /**
     * Description
     *     Adds a space between main code and addenda for EAN-8.
     */
    public static final int  DEC_EAN8_ADDENDA_SEPARATOR= 0x1a012006;
    /**
     * Description
     *     EAN-8 check digit transmit enable.
     */
    public static final int  DEC_EAN8_CHECK_DIGIT_TRANSMIT= 0x1a012002;
    /**
     * Description
     *     This property specifies whether EAN 8 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable EAN 8 decoding
     * 1: Enable EAN 8 decoding
     *     Initial Value: 1
     */
    public static final int  DEC_EAN8_ENABLED= 0x1a012001;
    /**
     * Description
     *     UPC-A 2-character addenda enable.
     */
    public static final int  DEC_UPCA_2CHAR_ADDENDA_ENABLED= 0x1a010004;
    /**
     * Description
     *     UPC-A 5-character addenda enable.
     */
    public static final int  DEC_UPCA_5CHAR_ADDENDA_ENABLED= 0x1a010005;
    /**
     *  Description
     *     Requires reading an addenda for UPC-A to issue a result.
     */
    public static final int  DEC_UPCA_ADDENDA_REQUIRED= 0x1a010006;
    /**
     * Description
     *     Added a space between main code and addenda for UPC-A.
     */
    public static final int  DEC_UPCA_ADDENDA_SEPARATOR= 0x1a010007;
    /**
     * Description
     *     UPC-A check digit transmit enable.
     */
    public static final int  DEC_UPCA_CHECK_DIGIT_TRANSMIT= 0x1a010002;
    /**
     * Description
     *     This property specifies whether UPCA decoding is enabled during the execution of Decode.
     *             September 3, 2018 - Page 59Honeywell - DCL Properties
     *     The property value should be set as follows:
     *             0: Disable UPCA decoding
     * 1: Enable UPCA decoding
     *     Initial Value: 1
     *     See also
     *     DEC_UPCA_TRANSLATE_TO_EAN13
     */
    public static final int  DEC_UPCA_ENABLE= 0x1a010011;
    /**
     * Description
     *     UPC-A number system transmit enable.
     */
    public static final int  DEC_UPCA_NUMBER_SYSTEM_TRANSMIT= 0x1a010003;
    /**
     * Description
     *     This property specifies whether UPC-A are translated to EAN13.
     *     The property value should be set as follows:
     *             0: Disable UPC-A to EAN13 translation
     * 1: Enable UPC-A to EAN13 translation
     *     Initial Value: 0
     */
    public static final int  DEC_UPCA_TRANSLATE_TO_EAN13= 0x1a010001;
    /**
     * Description
     *     This property specifies whether UPC-E0 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable UPC-E0 decoding
     * 1: Enable UPC-E0 decoding
     *     September 3, 2018 - Page 60Honeywell - DCL Properties
     *     Initial Value: 1
     */
    public static final int  DEC_UPCE0_ENABLED= 0x1a011001;
    /**
     * Description
     *     This property specifies whether UPC-E1 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable UPC-E1 decoding
     * 1: Enable UPC-E1 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_UPCE1_ENABLED= 0x1a011002;
    /**
     * Description
     *     UPC-E 2-character addenda enable.
     */
    public static final int DEC_UPCE_2CHAR_ADDENDA_ENABLED= 0x1a011006;
    /**
     * Description
     *     UPC-E 5-character addenda enable.
     */
    public static final int  DEC_UPCE_5CHAR_ADDENDA_ENABLED= 0x1a011007;
    /**
     * Description
     *     Requires reading an addenda for UPC-E to issue a result.
     */
    public static final int  DEC_UPCE_ADDENDA_REQUIRED= 0x1a011008;
    /**
     * Description
     *     Adds a space between main code and addenda for UPC-E.
     */
    public static final int  DEC_UPCE_ADDENDA_SEPARATOR= 0x1a011009;
    /**
     * Description
     *     UPC-E check digit transmit enable.
     */
    public static final int DEC_UPCE_CHECK_DIGIT_TRANSMIT= 0x1a011004;
    /**
     * Description
     *     UPC-E expand enable.
     */
    public static final int  DEC_UPCE_EXPAND= 0x1a011003;
    /**
     * Description
     *     UPC-E number system transmit enable.
     */
    public static final int  DEC_UPCE_NUMBER_SYSTEM_TRANSMIT= 0x1a011005;
    /**
     * Description
     *     Controls additional processing to improve bounds.
     */
    public static final int  DEC_UPC_IMPROVE_BOUNDS= 0x40011006;
    /**
     * Description
     *     Specifies whether substandard length margins should be allowed.
     *     Initial value: 1
     */
    public static final int  DEC_UPC_SHORT_MARGIN= 0x40011004;
    /**
     * Description
     *     Specifies which templates are active for decoding an OCR string.
     */
    public static final int  DEC_OCR_ACTIVE_TEMPLATES= 0x1b02d003;
    /**
     * Description
     *     Controls algorithms that improve decoding when the background field on which OCR tests is printed is not
     *     uniform.
     * 0: Disable
     * 1: Enable
     *     Default: 0
     */
    public static final int  DEC_OCR_BUSY_BACKGROUND= 0x40012308;
    /**
     * Description
     *     Optical Character Recognition mode.
     *             0: Disable
     * 1: Normal video
     * 2: Reverse Video
     * 3: Both Video
     *     Default: 0
     */
    public static final int  DEC_OCR_MODE= 0x1a02d001;
    /**
     * Description
     *     Specifies whether ICAO checksum calculations are preformed.
     * 0: Perform checksum calculations and suppress OCR output for rows with bad checksums.
     *             1: Checksum calculations are not performed. OCR output with bad checksums may be issued.
     *     September 3, 2018 - Page 64Honeywell - DCL Properties
     *     Default: 0
     */
    public static final int  DEC_OCR_PASSPORT_IGNORE_CHECKSUM= 0x1b02d006;
    /**
     * Description
     *     Template for reading Optical Characters.
     */
    public static final int  DEC_OCR_TEMPLATE= 0x9a02d002;
    /**
     * Description
     *     Sets if the image is mirrored.
     */
    public static final int DEC_IMAGE_MIRRORED= 0x40004003;
    /**
     * Description
     *     Sets the orientation of the image.
     */
    public static final int  DEC_IMAGE_ROTATION= 0x1a001015;
    /**
     * Description
     *     Bottom border of window in percentage of the image height.
     *     See also
     *     DEC_WINDOW_MODE
     *             DEC_WINDOW_TOP
     *     DEC_WINDOW_LEFT
     *             DEC_WINDOW_RIGHT
     */
    public static final int  DEC_WINDOW_BOTTOM= 0x1a00100d;
    /**
     * Description
     *     Left border of window in percentage of the image width.
     *     See also
     *     DEC_WINDOW_MODE
     *             DEC_WINDOW_TOP
     *     DEC_WINDOW_BOTTOM
     *             DEC_WINDOW_RIGHT
     */
    public static final int  DEC_WINDOW_LEFT= 0x1a00100e;

    /**
     * Description
     *     Windowing or centering mode.
     *     This property restricts the region of the image in which the decoder will decode bar codes. In order to make use of
     * this property, the user must define a windowing region by setting the following related properties:
     *             - DEC_WINDOW_TOP
     * - DEC_WINDOW_BOTTOM
     * - DEC_WINDOW_LEFT
     * - DEC_WINDOW_RIGHT
     *     These are referred to as the window bounds properties.
     *     The DEC_WINDOW_MODE property value should be set to one of the following primary modes:
     *             0: Windowing mode disabled
     * 1: Search-center-relative windowing mode
     * 2: Image-relative windowing mode
     * 3: Clipped windowing mode
     * # Windows Mode Details
     * ## Mode 0: Windowing mode disabled
     *     In mode 0, the decoder processes the entire supplied image frame, and any bar codes found are reported via the
     *     result callback.
     *             ## Mode 1: Search-center-relative windowing mode
     *     In mode 1, the decoder defines a region within the image based on the window bounds properties. Any bar code
     *     the decoder detects that intersects with this region is reported via the result callback. Bar codes that do not
     *     intersect are ignored. The region is sized relative to the image dimensions and centered about the search center,
     *     which is defined by the user-specified DEC_IMAGE_CENTER_X and DEC_IMAGE_CENTER_Y properties.
     *     Note that to use this mode, the window bounds properties must be set such that they span 50% in both dimensions.
     *             Otherwise, the decoder will operate as if it were set to mode 2. For example, the decoder can operate in mode 1
     *     when DEC_WINDOW_TOP = 40 and DEC_WINDOW_BOTTOM = 60 because 40 &lt; 50 &lt; 60. However, it
     *     fallback to the mode 2 behavior if DEC_WINDOW_TOP = 30 and DEC_WINDOW_BOTTOM = 40. The same
     *     constraint applies to DEC_WINDOW_LEFT and DEC_WINDOW_RIGHT.
     *             ## Mode 2: Image-relative windowing mode
     *     Mode 2 operates identically to mode 1, except the region is not centered about the search center.
     *             ## Mode 3: Clipped windowing mode
     *     In mode 3, the decoder defines the region in the same manner as in mode 2 (i.e. image-relative), but it does not
     *     process any portions of the image that lie outside of the region. Mode 3 is the fastest windowing mode, but the
     *     decoder will only read bar codes that lie entirely within the region.
     *     See also
     *     DEC_WINDOW_TOP
     *             DEC_WINDOW_BOTTOM
     *     DEC_WINDOW_LEFT
     *             DEC_WINDOW_RIGHT
     */
    public static final int  DEC_WINDOW_MODE= 0x1a00100b;
    /**
     *  Description
     *     Right border of window in percentage of the image height.
     *     See also
     *     DEC_WINDOW_MODE
     *             DEC_WINDOW_TOP
     *     DEC_WINDOW_BOTTOM
     *             DEC_WINDOW_LEFT
     */
    public static final int  DEC_WINDOW_RIGHT= 0x1a00100f;

    /**
     * Description
     *     Top border of window in percentage of the image height.
     *     See also
     *     DEC_WINDOW_MODE
     *             DEC_WINDOW_BOTTOM
     *     DEC_WINDOW_LEFT
     *             DEC_WINDOW_RIGHT
     */
    public static final int  DEC_WINDOW_TOP= 0x1a00100c;

    /**
     * Description
     *     Average black level of the background on which scanned objects appear.
     */
    public static final int DEC_BLACK_LEVEL= 0x40005013;
    /**
     * Description
     *     It configures how the finder scans will be done on decoder.
     *     For linear code, when cycling finder is enabled the decoder focuses its search for bar codes on the center of the
     *     image. This is appropriate for handheld readers and applications where the bar code to be read is expected to
     *     always appear near the center of the image. When this property is disabled, the decoder performs a uniform search
     *     over the entire image.
     *     This property has no effect if DEC_PASS_THROUGH is activated.
     *             When this property is enabled, bar codes located far from the image center may not be decoded.
     *     The property values are:
     *             0: disable
     * 1: enable
     *     default value: 1
     *     Example
     *         enable uniform search over the entire image
     *         DecodeSet( DEC_CYCLING_FINDER, (void *) 0x00);
     *         See also
     *     DEC_PASS_THROUGH
     */
    public static final int  DEC_CYCLING_FINDER= 0x40100006;

    /**
     * Description
     *     Algorithms for more vigorous searching decoding.
     */
    public static final int  DEC_DECODE_VIGOR= 0x1a001001;
    /**
     * Description
     *     This property specifies whether DPM decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable DPM decoding
     * 1: Dotpeen DPM decoding
     * 2: Reflective DPM decoding
     *     Initial Value: 0
     */
    public static final int  DEC_DPM_ENABLED= 0x40012903;
    /**
     * Description
     *     ECI codeword handling in result data.
     */
    public static final int DEC_ECI_HANDLING= 0x1a002003;
    /**
     * Description
     *     This property specifies whether all the decode filters in the FLD decoder are applied on the same image or on
     *     several images.
     *     The property value should be set as follows:
     *             0: on the same image.
     *             1: on two consecutive images.
     *     Initial Value: 1
     *     Note: If all the filters are applied on a same image, the time spent in FLD is higher.
     */
    public static final int  DEC_FLD_ROLLING_FILTERS= 0xfd012004;
    /**
     * Description
     *     This property specifies the area analysed by the FLD decoder to find a barcode.
     *     The property value should be set as follows:
     *             0: center of the image.
     *             1: full image.
     *     Initial Value: 0
     */
    public static final int  DEC_FLD_SEARCH_AREA= 0xfd012002;
    /**
     * Description
     *     This property specifies the amount of summing used by the FLD decoder to decode poor printed barcodes.
     *     The property value should be set as follows:
     *             0: no summing.
     *             1: summing of 3 pixels.
     * 2: summing of 5 pixels.
     *     Initial value is 0.
     *     Note: The higher is the property value, the higher is the time spent in the FLD decoder.
     */
    public static final int  DEC_FLD_SUMMING= 0xfd012003;
    /**
     * Description
     *     This property specifies whether certain improvements should be enabled.
     *     The property value is a bit field defined as follow:
     *     b0: Enable improved positioning of the bounds for POSTNET and PLANET code symbols.
     *     The values returned by SD_PROP_RESULT_BOUNDS will indicate the starting and ending positions of the
     *     symbol with greatly improved accuracy.
     *             b1: Enable improved symbol locating algorithms, especially at lower sampling density.
     *             b2: For Code 128, enable improved compliance with sections 2.3.4 (&quot;Special Characters&quot;) and 2.7
     *             (&quot;Transmitted Data&quot;) of the Uniform Symbology Specification â€“ Code 128 (June 1993 edition).
     *     Initial value: 0x00
     */
    public static final int  DEC_GENERAL_IMPROVEMENTS= 0x40005011;
    /**
     * Description
     *     Returns energy of image.
     */
    public static final int  DEC_GET_ENERGY= 0x1a001013;
    /**
     * Description
     *     When set, only the region(s) of interest is(are) processed by the decoder.
     *             0: Disable(default value). The entire original image is sent to the Decoder.
     * 1: Standard
     * - Use the aimer position to weight activity.
     * - Activity calculated on the row and the column in the middle of each cell.
     *             - The ROI window may not include the aimer.
     *             - This is the default mode recommended for hand held scanner when used in Manual Trigger mode.
     * 2: Standard, aimer centered.
     *             - Activity calculated on the row and the column in the middle of each cell.
     *             - The ROI window will always include the aimer.
     *             - This is the default mode recommended for Andaman.
     * 3: DPM, aimer centered.
     *             - Activity calculated on 4 rows and 2 columns in each cell.
     *             - The ROI window will always include the aimer.
     *             4: Kiosk/presentation application.
     *             - Ignore aimer position, no weight activity.
     * - Activity calculated on the row and the column in the middle of each cell.
     *             - The ROI window may not include the aimer.
     *             - This is the mode recommended for hand held scanner when used in Presentation mode.
     *     NB : only DPM ROI mode could be used when DPM feature is active.
     */
    public static final int  DEC_ID_PROP_USE_ROI= 0x40008015;
    /**
     * Description
     *     This property specifies whether normal or inverse decoding for linear symbologies is enabled during the execution
     *     of Decode. This property only works if DEC_VIDEO_REVERSE_ENABLED is set to 0. It is recommended to
     *     use this property instead of DEC_VIDEO_REVERSE_ENABLED.
     *             0: Decode only normal video for 1D codes
     * 1: Decode only inverse video for 1D codes
     * 2: Decode both, normal and inverse video for 1D codes
     * See also
     *     DEC_VIDEO_REVERSE_ENABLED
     */
    public static final int  DEC_LINEAR_DECODE_POLARITY= 0x1a00102b;
    /**
     * Description
     *     Controls low aspect ratio searching for 1D symbols (including PDF417).
     *     Initial value: 1
     */
    public static final int  DEC_LOW_ASPECT_RATIO= 0x40005005;
    /**
     * Description
     *     Controls low contrast searching.
     *     Initial value: 1
     */
    public static final int  DEC_LOW_CONTRAST_IMPROVEMENTS= 0x40005006;
    /**
     * Description
     *     If the value of the property is not 0, a 1D barcode can be decoded only if the resolution in the image of its narrow
     *     bars is higher than the property value. The measurement unit is 1/10 of pixel.
     *     This property is only available for: Code 39, Codabar, Code 128, EAN/UPC, Interleaved 2 of 5, Code 11, Code 93,
     *     MSI, PDF/microPDF and GS1 Databar.
     *     Initial Value: 0 (No resolution check)
     */
    public static final int  DEC_MIN_PPM_1D= 0x1a003008;
    /**
     * Description
     *     If the value of the property is not 0, a 2D barcode can be decoded only if the resolution in the image of its modules
     *     is higher than the property value. The measurement unit is 1/10 of pixel.
     *     This property is only available for: Aztec, Datamatrix and QR code.
     *     Initial Value: 0 (No resolution check)
     */
    public static final int  DEC_MIN_PPM_2D= 0x1a003009;
    /**
     * Description
     *     Controls time spent searching and decoding.
     */
    public static final int  DEC_PASS_THROUGH= 0x40005016;
    /**
     * Description
     *     This property controls the reading tolerance of the decoder.
     *     The property value should be set as follows:
     *             0: Very high reading tolerance: this is the most permissive mode. When enabled, the scanner reads codes of
     *     variable quality.
     *             1: High reading tolerance
     * 2: Medium reading tolerance: this mode allows medium permissiveness (recommended)
     * 3: Low reading tolerance: this is the least permissive mode
     */
    public static final int  DEC_SECURITY_LEVEL= 0x1a002002;
    /**
     * Description
     *     Debug verbosity.
     */
    public static final int  DEC_SHOW_DECODE_DEBUG= 0x1a001006;
    /**
     * Description
     *     This property specifies whether normal or inverse decoding for linear symbologies is enabled during the execution
     *     of Decode. It is recommended to use DEC_LINEAR_DECODE_POLARITY rather than this property.
     * 0: Decode only normal video for 1D codes
     * 1: Decode only inverse video for 1D codes
     * 2: Decode both, normal and inverse video for 1D codes
     */
    public static final int  DEC_VIDEO_REVERSE_ENABLED= 0x1a001004;
    /**
     * Description
     *     Gets the Link flag of the result.
     */
    public static final int  DEC_RESULT_LINK_FLAG= 0x1a001028;
    /**
     * Description
     *     Coupon Code decoding mode.
     */
    public static final int  DEC_COUPON_CODE_MODE= 0x1a006001;
    /**
     * Description
     *     This property specifies whether PDF417 decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable PDF417 decoding
     * 1: Enable PDF417 decoding
     *     Initial Value: 0
     */
    public static final int  DEC_PDF417_ENABLED= 0x1a024001;
    /**
     * Description
     *     Maximum data length to issue a result for PDF417.
     */
    public static final int  DEC_PDF417_MAX_LENGTH= 0x1a024003;
    /**
     * Description
     *     Minimum data length to issue a result for PDF417.
     */
    public static final int  DEC_PDF417_MIN_LENGTH= 0x1a024002;
    /**
     * Description
     *     Maximum additional time in milliseconds to decode the composite piece after reading a UPC.
     *     If null, any UPC label will be decoded without the composite part, whatever the 2D is present or not.
     *     Default: 300ms.
     */
    public static final int  DEC_ADD_SEARCH_TIME_UPC_COMPOSITE= 0x1a003006;
    /**
     * Composite code enable.
     */
    public static final int DEC_COMPOSITE_ENABLED= 0x1a026001;
    /**
     * Description
     *     Allows UPC codes to be read with PDF417 or MicroPDF417 composite.
     * 0: UPC/EAN codes read as UPC/EAN
     * 1: UPC/EAN codes read as GS1 composites
     */
    public static final int  DEC_COMPOSITE_WITH_UPC_ENABLED= 0x1a026004;
    /**
     * Maxicode enable.
     */
    public static final int  DEC_MAXICODE_ENABLED= 0x1a028001;
    /**
     * Description
     *     Maximum data length to issue a result for Maxicode.
     */
    public static final int  DEC_MAXICODE_MAX_LENGTH= 0x1a028003;
    /**
     * Description
     *     Minimum data length to issue a result for Maxicode.
     */
    public static final int  DEC_MAXICODE_MIN_LENGTH= 0x1a028002;
    /**
     * Description
     *     Controls algorithms that aid in decoding of symbols of different sizes.
     *     Initial value: 1
     */
    public static final int  DEC_MAXICODE_SYMBOL_SIZE= 0x40010602;
    /**
     * Description
     *     This property specifies whether the secondary message of a Maxicode is decoded:
     *             0: Primary Message Only
     * 1: Primary + Secondary (if avail)
     * 2: Primary + Secondary Required
     *     September 3, 2018 - Page 80Honeywell - DCL Properties
     *     Initial value: 1
     */
    public static final int DEC_SD_PROP_MC_MESSAGE_FORMAT= 0x40010603;
    /**
     * Description
     *     This property specifies whether Data Matrix decoding is enabled during the execution of the Decoder.
     *     Decoding may be separately enabled or disabled for normal and inverse video symbols. A normal video symbol
     *     has a black &quot;L&quot; finder pattern. An inverse video symbol has a white &quot;L&quot; finder pattern.
     *     Note that rectangular Data Matrix symbol decoding is separately controlled by the
     *     DEC_DATAMATRIX_RECTANGLE property.
     *     The property value is a bit field defined as follows:
     *     b0: Enable normal video Data Matrix decoding
     *     b1: Enable inverse video Data Matrix decoding
     *     Note that only the ECC 200 style Data Matrix symbols are decoded by the decoder. The other styles are obsolete.
     */
    public static final int DEC_DATAMATRIX_ENABLED= 0x1a029001;
    /**
     * Description
     *     Controls algorithms that improve decoding of low contrast symbols.
     *     Initial value: 1
     */
    public static final int  DEC_DATAMATRIX_LOW_CONTRAST= 0x40010414;
    /**
     * Description
     *     Maximum data length to issue a result for Data Matrix.
     */
    public static final int DEC_DATAMATRIX_MAX_LENGTH= 0x1a029003;
    /**
     *  Minimum data length to issue a result for Data Matrix.
     */
    public static final int DEC_DATAMATRIX_MIN_LENGTH= 0x1a029002;
    /**
     * Description
     *     Controls algorithms that improve decoding when individual modules are sufficiently non-square.
     */
    public static final int DEC_DATAMATRIX_NON_SQUARE_MODULES= 0x40010412;
    /**
     * Description
     *     Controls algorithms that improve decoding when multi-tile symbols are not aligned.
     */
    public static final int DEC_DATAMATRIX_SHIFTED_TILES= 0x40010413;
    /**
     * Description
     *     Controls algorithms that improve in decoding of symbols of different sizes.
     *     The property value should be set as follows:
     *             0: Normal Data Matrix operation.
     *             1: Handle smaller Data Matrix symbols.
     *             2: Handle very small Data Matrix symbols.
     *     Initial Value: 1
     */
    public static final int DEC_DATAMATRIX_SYMBOL_SIZE= 0x40010416;
    /**
     * Description
     *     This property specifies whether QR Code decoding is enabled during the execution of Decode.
     *     Decoding may be separately enabled or disabled for normal and inverse video symbols, and for QR Code and
     *     Micro QR Code. A normal video symbol is printed in black on a white substrate.
     *     An inverse video symbol is printed in white on a black substrate.
     *     The property value is a bit field defined as follows:
     *     b0: Enable normal video QR Code decoding
     *     b1: Enable inverse video QR Code decoding
     *     b2: Enable normal video Micro QR Code decoding
     *     b3: Enable inverse video Micro QR Code decoding
     *     Initial Value: 0
     *     Example
     *    enable inverse video QR Code and normal video Micro QR Code decoding
     *
            DecodeSet(DEC_QR_ENABLED, (void *) 0x06);
     */
    public static final int DEC_QR_ENABLED= 0x1a02a001;
    /**
     * Description
     *     Maximum data length to issue a result for QR Code.
     */
    public static final int  DEC_QR_MAX_LENGTH= 0x1a02a003;
    /**
     * Description
     *     Minimum data length to issue a result for QR Code.
     */
    public static final int  DEC_QR_MIN_LENGTH= 0x1a02a002;
    /**
     * Description
     *     Controls algorithms that improve decoding of QR symbols with non-square modules.
     */
    public static final int  DEC_QR_NON_SQUARE_MODULES= 0x40010902;

    /**
     * Description
     *     Controls algorithms that aid in decoding of symbols of different sizes.
     *     Initial value: 1
     */
    public static final int  DEC_QR_SYMBOL_SIZE= 0x40010904;
    /**
     * Description
     *     Specifies whether a Micro-QR symbol will have its own HHP Code ID.
     */
    public static final int  DEC_QR_USE_ALT_MICROQR_ID= 0x1b02a003;
    /**
     * Description
     *     This property allows to decode QR barcodes without quiet zones.
     *     Enabling this property is discouraged by Honeywell, unless absolutely necessary.
     *     The property value should be set as follows:
     *             0: Disable
     * 1: Enable
     *     Initial Value: 0
     */
    public static final int  DEC_QR_WITHOUT_QZ= 0x40010905;
    /**
     * Label Code enable.
     */
    public static final int DEC_LABELCODE_ENABLED= 0x1a101001;
    /**
     * Postal properties
     * Description
     * Australian Postal Code:
     * - 4-State barcode, can be 37, 52 or 67 bars (11, 16 or 21 codewords)
     * - start and stop are 2 bars (tracker ascender + tracker)
     * - The 4 bars after the start are the Format Control Code (FCC)
     * FCC supported by the decoder:
     * 37 bars: FCC01..FCC32, FCC34..FCC37, FCC45, FCC87, FCC92
     * 52 bars: FCC00, FCC33, FCC38..FCC43, FCC46..FCC61, FCC72
     * 67 bars: FCC00, FCC44, FCC62..FCC71, FCC73..FCC91, FCC93..FCC99
     * - Customer information field (16 or 31 bars)
     * - Reed Solomon error correction (12 bars)
     * POSTNET (POSTal Numeric Encoding Technique)
     * - 2-State barcode, can be
     * - 32 bars: 5-digit ZIP code (A Field)
     * - 37 bars: 6-digit, (B Field), now obsolete
     * - 47 bars: 8-digit, (B&#039; Field)
     * - 52 bars: 9-digit ZIP+4 code ( C Field)
     * - 62 bars: 11-digit, ZIP Code, ZIP+4 Code, Delivery Point Code ( C&#039; Field)
     * - start and stop are just one high bar (also named Frame bars)
     * - each digit is encoded with 5 bars, 2 high and 3 low (same as CEPNET)
     * - correction character: Modulo 10, 5 bars
     * CEPNET:
     * - 2-State barcode, can be 47, 72 bars (8 or 13 digits)
     * - start and stop are just one high bar (also named Delimiter)
     * - each digit is encoded with 5 bars, 2 high and 3 low (same as POSTNET)
     * - correction character: Modulo 10, 5 bars
     * PLANET (Postal Alpha Numeric Encoding Technique)
     * - fully superseded by Intelligent Mail Barcode by January 28, 2013.
     * - 2-State barcode, can be 62 or 72 bars (12- or 14-digit including checksum)
     * - Service Type: 2 digits
     * - Customer ID: 9 or 11 digits
     * - Checksum: 1 digit
     * - start and stop are just one high bar (also named Delimiter)
     * - each digit is encoded with 5 bars, 3 high and 2 low (exact opposite of POSTNET)
     * - correction character: Modulo 10, 5 bars
     * Canada Post (CPC 4-State or PostBar)
     * - derived from the RM4SCC
     * - 3 types:
     * - 52 bars (Business reply): 12 reed-solomon parity check bars, 36 information bars (12 characters)
     * - 56 bars: 30 reed-solomon parity check bars, 18 information bars (6 characters)
     * - 73 bars (not supported by ID)
     * - 82 bars: 12 reed-solomon parity check bars, 66 information bars (22 characters)
     * - 4 characters set can be used
     * - start and stop are 2 bars (tracker ascender + tracker)
     * RM4SCC (Royal Mail 4-State Customer Code), also known as CBC (Customer Bar Code)
     * - start is tracker ascender, stop is full tracker
     * - checksum based on modulo 6
     * - 9 characters max encoded (38 valid characters: numeric, alphanumeric upper case and () or [])
     * - each character consists of 4 bars, Two of these have ascenders and two have descenders.
     * - 3 types known:
     * - 51 bars: RED TAG Barcode
     * - 66 bars: Mailmark barcodes C (consolidated)
     * - 78 bars: Mailmark barcodes L (long)
     * KIX (Klant index or Customer index)
     * - 4-State
     * - Slightly modified from CBC (doesn&#039;t use the start and end symbols or the checksum, separates the house number
     * and suffixes with an X)
     * Intelligent Mail Barcode (IM barcode), also known as 4-State Customer Barcode (4CB or 4-CB or USPS4CB)
     * - 4-State
     * - 65-bar barcode for use on mail in the United States
     * - Supersede POSTNET and PLANET.
     * InfoMail Barcode A
     * - 4-State, 51 bars (17 cw, 12 data + 5 error correction)
     * - Reed-solomon error correction
     * UPU ID-tag 4-state (S18d)
     * - 4-state
     * - can be 57 or 75 bars
     * POSTI 4-state, Finnish post barcode
     * - 4-state, 42 bars (16 cw, 8 data + 8 error correction)
     * - Reed-solomon error correction
     */
    /**
     * Description
     *     Issues the bar number instead of decoded data for Australia Post.
     */
    public static final int  DEC_AUS_POST_BAR_OUTPUT_ENABLED= 0x40010817;
    /**
     *  Description
     *     Australia Post Customer data interpretation mode.
     */
    public static final int  DEC_AUS_POST_INTERPRET_MODE = 0x40010818;
    /**
     * Description
     *     Allows decoding of Australia Post code with no FCC information.
     */
    public static final int DEC_AUS_POST_ZERO_FCC= 0x40010813;
    /**
     * Description
     *     Issues the bar number instead of the decoded data for Canadian Post.
     */
    public static final int DEC_CAN_POST_BAR_OUTPUT= 0x40010819;
    /**
     * Description
     *     Planetcode check digit transmit enable.
     */
    public static final int DEC_PLANETCODE_CHECK_DIGIT_TRANSMIT= 0x1a130001;
    /**
     * Description
     *     This property sets which, if any, postal decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable all postal decoding
     * 1: Enable Australia Post decoding
     * 2: Enable Royal Mail InfoMail decoding
     * 3: Enable Japan Post decoding
     * 4: Enable KIX Code decoding
     * 5: Enable Planet Code decoding
     * 6: Enable Postnet decoding
     * 7: Enable Royal Mail 4 State Code (British Post Office) decoding
     * 8: Enable Royal Mail InfoMail and Royal Mail 4 State decoding
     * 9: Enable UPU decoding
     * 10: Enable USPS 4 State (4CB) decoding
     * 11: Enable Postnet with B and B&#039; fields (B&amp;B) decoding
     * 12: Enable Planet Code and Postnet decoding
     * 13: Enable PlanetCode and UPU decoding
     * 14: Enable Postnet and UPU decoding
     * 15: Enable Planet Code and USPS 4CB decoding
     *     September 3, 2018 - Page 89Honeywell - DCL Properties
     * 16: Enable Postnet and USPS 4CB decoding
     * 17: Enable UPU and USPS 4CB decoding
     * 18: Enable Planet Code, Postnet, and Postnet B&amp;B decoding
     * 19: Enable Postnet, Postnet B&amp;B, and UPU decoding
     * 20: Enable Postnet, Postnet B&amp;B, USPS 4CB decoding
     * 21: Enable Planet Code, Postnet, and UPU decoding
     * 22: Enable Planet Code, Postnet, and USPS 4CB decoding
     * 23: Enable Planet Code, UPU, and USPS 4CB decoding
     * 24: Enable Postnet, UPU, and USPS 4CB decoding
     * 25: Enable Planet Code, Postnet, Postnet B&amp;B, and UPU decoding
     * 26: Enable Planet Code, Postnet, Postnet B&amp;B, and USPS 4CB decoding
     * 27: Enable Postnet, Postnet B&amp;B, UPU, and USPS 4CB decoding
     * 28: Enable Planet Code, Postnet, UPU, and USPS 4CB decoding
     * 29: Enable Planet Code, Postnet, Postnet B&amp;B, UPU, and USPS 4CB decoding
     * 30: Enable Canadian Post decoding
     * 31: Enable Postnet, Cepnet decoding
     * 32: Enable Swedish post decoding
     * 33: Enable Brasil post decoding
     * 34: Enable Brasil post, Postnet and Cepnet decoding
     * 35: Enable Royal Mail mailmark (EIB) decoding
     * 36: Enable Royal Mail mailmark (EIB) and Royal Mail Infomail decoding
     * 37: Enable Royal Mail mailmark (EIB) and Royal Mail 4 State Code (British Post Office) decoding
     * 38: Enable Royal Mail mailmark (EIB), Royal Mail Infomail and Royal Mail 4 State Code (British Post Office)
     *     decoding
     * 39: Enable Finnish post (Posti) decoding
     *     Initial Value: 0
     */
    public static final int  DEC_POSTAL_ENABLED= 0x1a110001;
    /**
     * Description
     *     Postnet check digit transmit enable.
     */
    public static final int  DEC_POSTNET_CHECK_DIGIT_TRANSMIT= 0x1a120001;
    /**
     * Description
     *     Verifies the minimum length of Royal Mail.
     */
    public static final int DEC_ROYAL_MAIL_FORMAT_CHECK_MIN_LENGTH= 0x40010815;
    /**
     *  Description
     *     This property specifies whether Grid Matrix decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *      0: Disable Grid Matrix decoding
     *      1: Enable Grid Matrix decoding
     *     Initial Value: 0
     */
    public static final int  DEC_GRIDMATRIX_ENABLED= 0x1a160001;
    /**
     * Description
     *     Sets the maximum data length of a Grid Matrix that is allowed to issue.
     */
    public static final int  DEC_GRIDMATRIX_MAX_LENGTH= 0x1a160003;
    /**
     * Description
     *     Sets the minimum data length of a Grid Matrix that is allowed to issue.
     */
    public static final int  DEC_GRIDMATRIX_MIN_LENGTH= 0x1a160002;
    /**
     * Description
     *     enable/disable of enhanced methods for dealing with heavily damaged linear bar codes (defaults to 0)
     */
    public static final int DEC_LINEAR_DAMAGE_IMPROVEMENTS= 0x40005025;
    /**
     * Description
     *     This property specifies whether DotCode decoding is enabled during the execution of Decode.
     *     The property value should be set as follows:
     *             0: Disable DotCode decoding
     * 1: Enable DotCode decoding
     *     Initial Value: 0
     */
    public static final int DEC_DOTCODE_ENABLED= 0x1a161001;
    /**
     * Description
     *     This property allows a more extensive search of a DotCode barcode. It helps to decode bad printed barcode but
     *     increases DotCode CPU usage.
     *     The property value should be set as follows:
     *             0: Disable DotCode extensive search
     * 1: Enable DotCode extensive search
     *     Initial Value: 0
     */
    public static final int DEC_DOTCODE_EXTENSIVE_SEARCH= 0x40016004;
    /*
    Description
    Sets the maximum data length of a DotCode that is allowed to issue.
            Example
    September 3, 2018 - Page 94Honeywell - DCL Properties
    set DotCode max length to 500
     */
    public static final int  DEC_DOTCODE_MAX_LENGTH= 0x1a161003;
    /**
     * Description
     *     Sets the minimum data length of a DotCode that is allowed to issue.
     *             Example
     *  set DotCode min length to 7
     */
    public static final int  DEC_DOTCODE_MIN_LENGTH= 0x1a161002;
    /**
     *  Monocolor interpolation enable.
     */
    public static final int  DEC_MONOCOLOR_ENABLED= 0x1a007001;

    //public static final int DEC_MSI_MIN_LENGTH = 436342786;
    //public static final int DEC_OCR_TEMPLATE = -1711091710;
    //public static final int DEC_OCR_ACTIVE_TEMPLATES = 453169155;
    public static final int POSTAL_ENABLED = 437321729;
    public static final int POSTAL_INDEX_MIN = 1;
    public static final int POSTAL_INDEX_MAX = 30;
    //public static final int DEC_WINDOW_MODE = 436211723;
    //public static final int DEC_WINDOW_TOP = 436211724;
    //public static final int DEC_WINDOW_BOTTOM = 436211725;
    //public static final int DEC_WINDOW_LEFT = 436211726;
    //public static final int DEC_WINDOW_RIGHT = 436211727;
    public static final int WINDOWING_OFF = 0;
    public static final int WINDOWING_ON = 2;
    public static final int WINDOWING_ON_INSIDE_ONLY = 3;
    //public static final int DEC_DECODE_VIGOR = 436211713;
    public static final int DEC_IQ_FILTER_MODE = 436211714;
    //public static final int DEC_VIDEO_REVERSE_ENABLED = 436211716;
    public static final int DEC_SHOW_NO_READ_ENABLED = 436211717;
    //public static final int DEC_SHOW_DECODE_DEBUG = 436211718;
    public static final int DEC_COMBINE_COMPOSITES = 436211719;
    public static final int DEC_IMAGE_HEIGHT = 436211720;
    public static final int DEC_IMAGE_WIDTH = 436211721;
    public static final int DEC_IMAGE_LINE_DELTA = 436211722;
    public static final int DEC_IMAGE_CENTER_X = 436211729;
    public static final int DEC_IMAGE_CENTER_Y = 436211730;
    //public static final int DEC_GET_ENERGY = 436211731;
    //public static final int DEC_IMAGE_MIRRORED = 1073758211;
    public static final int DEC_USE_MLD = 436215809;
    //public static final int DEC_SECURITY_LEVEL = 436215810;
    //public static final int DEC_ECI_HANDLING = 452988934;
    public static final int DEC_DECODE_TIMING_CONTROL = 436219905;
    public static final int DEC_DECODE_TIME = 436219906;
    public static final int DEC_SEARCH_TIME = 436219907;
    //public static final int DEC_ADD_SEARCH_TIME_ADDENDA = 436219908;
    public static final int DEC_ADD_SEARCH_TIME_CONCAT = 436219909;
    //public static final int DEC_ADD_SEARCH_TIME_UPC_COMPOSITE = 436219910;
    public static final int DEC_PRINT_RESULTS = 436228097;
    public static final int DEC_DUMP_SETTINGS = 436228098;
    public static final int DEC_DISPLAY_DATA = 436228099;
    public static final int DEC_RESET_DECODER = 436228100;
    public static final int DEC_FAST_DECODER_ENABLED = 436228101;
    public static final int DEC_FULL_DECODER_ENABLED = 436228102;
    //public static final int DEC_COUPON_CODE_MODE = 436232193;
    public static final int DEC_EANUCC_EMULATION_MODE = 436232194;
    public static final int DEC_COMBINE_COUPON_CODES = 436232196;
    //public static final int DEC_MONOCOLOR_ENABLED = 436236289;
}
