package android.device.scanner.configuration;

/**
 * <code>Symbology</code> is an enumeration class defining constants for
 * different barcode types.
 *
 * The type is one of:
 * <table border=2>
 * <tr>
 * <td>CODE39</td>
 * <td>Code 39</td>
 * </tr>
 * <tr>
 * <td>DISCRETE25</td>
 * <td>Discrete 2/5</td>
 * </tr>
 * <tr>
 * <td>MATRIX25</td>
 * <td>Matrix 2/5</td>
 * </tr>
 * <tr>
 * <td>INTERLEAVED25</td>
 * <td>Interleaved 2/5</td>
 * </tr>
 * <tr>
 * <td>CODABAR</td>
 * <td>Codabar</td>
 * </tr>
 * <tr>
 * <td>CODE93</td>
 * <td>Code 93</td>
 * </tr>
 * <tr>
 * <td>CODE128</td>
 * <td>Code 128</td>
 * </tr>
 * <tr>
 * <td>UPCA</td>
 * <td>UPC-A</td>
 * </tr>
 * <tr>
 * <td>UPCE</td>
 * <td>UPC-E</td>
 * </tr>
 * <tr>
 * <td>EAN13</td>
 * <td>EAN-13</td>
 * </tr>
 * <tr>
 * <td>EAN8</td>
 * <td>EAN-8</td>
 * </tr>
 * <tr>
 * <td>MSI</td>
 * <td>MSI</td>
 * </tr>
 * <tr>
 * <td>GS1_14</td>
 * <td>GS1 Databar-14</td>
 * </tr>
 * <tr>
 * <td>GS1_LIMIT</td>
 * <td>GS1 Databar Limited</td>
 * </tr>
 * <tr>
 * <td>GS1_EXP</td>
 * <td>GS1 Databar Expanded</td>
 * </tr>
 * <tr>
 * <td>CODE16K</td>
 * <td>Code 16K</td>
 * </tr>
 * <tr>
 * <td>CODE49</td>
 * <td>Code 49</td>
 * </tr>
 * <tr>
 * <td>PDF417</td>
 * <td>PDF-417</td>
 * </tr>
 * <tr>
 * <td>DATAMATRIX</td>
 * <td>Datamatrix</td>
 * </tr>
 * <tr>
 * <td>MAXICODE</td>
 * <td>Maxicode</td>
 * </tr>
 * <tr>
 * <td>TRIOPTIC</td>
 * <td>Trioptic</td>
 * </tr>
 * <tr>
 * <td>CODE32</td>
 * <td>Code 32</td>
 * </tr>
 * <tr>
 * <td>MICROPDF417</td>
 * <td>MicroPDF417</td>
 * </tr>
 * <tr>
 * <td>COMPOSITE</td>
 * <td>Composite Code</td>
 * </tr>
 * <tr>
 * <td>QRCODE</td>
 * <td>QR Code</td>
 * </tr>
 * <tr>
 * <td>AZTEC</td>
 * <td>Aztec Code</td>
 * </tr>
 * <tr>
 * <td>POSTAL</td>
 * <td>Postal Code</td>
 * </tr>
 * <tr>
 * <td>POSTAL_PLANET</td>
 * <td>Postal Planet</td>
 * </tr>
 * <tr>
 * <td>POSTAL_POSTNET</td>
 * <td>Postal Postnet</td>
 * </tr>
 * <tr>
 * <td>POSTAL_4STATE</td>
 * <td>Postal USPS 4-State</td>
 * </tr>
 * <tr>
 * <td>POSTAL_UPUFICS</td>
 * <td>Postal UPU FICS</td>
 * </tr>
 * <tr>
 * <td>POSTAL_ROYALMAIL</td>
 * <td>Postal Royal Mail</td>
 * </tr>
 * <tr>
 * <td>POSTAL_AUSTRALIAN</td>
 * <td>Australian Postal</td>
 * </tr>
 * <tr>
 * <td>POSTAL_KIX</td>
 * <td>Kix Postal</td>
 * </tr>
 * <tr>
 * <td>POSTAL_JAPAN</td>
 * <td>Japan Postal</td>
 * </tr>
 * <tr>
 * <td>NONE</td>
 * <td>No decoded data</td>
 * </tr>
 * </table>
 */
public enum Symbology {
	NONE(Constants.Symbology.NONE), //
    CODE39(Constants.Symbology.CODE39), //
    DISCRETE25(Constants.Symbology.DISCRETE25), //
    MATRIX25(Constants.Symbology.MATRIX25), //
    INTERLEAVED25(Constants.Symbology.INTERLEAVED25), //
    CODABAR(Constants.Symbology.CODABAR), //
    COMPOSITE_TLC_39(Constants.Symbology.RESERVED_6), //
    CODE93(Constants.Symbology.CODE93), //
    CODE128(Constants.Symbology.CODE128), //
    UPCA(Constants.Symbology.UPCA), //
    UPCE(Constants.Symbology.UPCE), //
    EAN13(Constants.Symbology.EAN13), //
    EAN8(Constants.Symbology.EAN8), //
    GRIDMATRIX(Constants.Symbology.RESERVED_13), //
    MSI(Constants.Symbology.MSI), //
    CODE49(Constants.Symbology.RESERVED_15), //
    TELEPEN(Constants.Symbology.RESERVED_16), //
    GS1_14(Constants.Symbology.GS1_14), //
    GS1_LIMIT(Constants.Symbology.GS1_LIMIT), //
    GS1_EXP(Constants.Symbology.GS1_EXP), //
    CODABLOCK_A(Constants.Symbology.RESERVED_20), //
    CODABLOCK_F(Constants.Symbology.RESERVED_21), //
    PDF417(Constants.Symbology.PDF417), //
    DATAMATRIX(Constants.Symbology.DATAMATRIX), //
    MAXICODE(Constants.Symbology.MAXICODE), //
    TRIOPTIC(Constants.Symbology.TRIOPTIC), //
    CODE32(Constants.Symbology.CODE32), //
    NEC25(Constants.Symbology.RESERVED_27), //
    KOREA_POST(Constants.Symbology.RESERVED_28), //
    MICROPDF417(Constants.Symbology.MICROPDF417), //
    MICROQR(Constants.Symbology.RESERVED_30), //
    QRCODE(Constants.Symbology.QRCODE), //
    AZTEC(Constants.Symbology.AZTEC), //
    CANADA_POST(Constants.Symbology.RESERVED_33), //
    POSTAL_PLANET(Constants.Symbology.POSTAL_PLANET), //
    POSTAL_POSTNET(Constants.Symbology.POSTAL_POSTNET), //
    POSTAL_4STATE(Constants.Symbology.POSTAL_4STATE), //
    POSTAL_UPUFICS(Constants.Symbology.POSTAL_UPUFICS), //
    POSTAL_ROYALMAIL(Constants.Symbology.POSTAL_ROYALMAIL), //
    POSTAL_AUSTRALIAN(Constants.Symbology.POSTAL_AUSTRALIAN), //
    POSTAL_KIX(Constants.Symbology.POSTAL_KIX), //
    POSTAL_JAPAN(Constants.Symbology.POSTAL_JAPAN), //
	GS1_128(Constants.Symbology.GS1_128), //
	COMPOSITE_CC_C(Constants.Symbology.COMPOSITE_CC_C), //
    COMPOSITE_CC_AB(Constants.Symbology.COMPOSITE_CC_AB),
    CHINESE25(Constants.Symbology.CHINESE25),
    CODE11(Constants.Symbology.CODE11),
    UPCE1(Constants.Symbology.UPCE1),
    HANXIN(Constants.Symbology.HANXIN),
    DOTCODE(Constants.Symbology.DOTCODE);

	/**
	 * @hide
	 */
	private final int value;

	/**
	 * @hide
	 */
    private static Symbology[] allValues = values();
    
    /**
     * @hide
     */
    private Symbology(int type_number) {
        value = type_number;
    }
	
	/**
	 * From the ordered enum to Symobology.
	 * 
	 * @return Symbology the corresponding one.
	 */
	public static Symbology fromOrdinal(int n) {
		return allValues[n];
	}

	/**
	 * From an integer value, retrieves the corresponding Symbology.
	 * 
	 * @param n
	 *            <code>int</code>
	 * @return Symbology the corresponding Symbology.
	 */
	public static Symbology fromInt(int n) {
		for(int i = 0; i < allValues.length; i++) {
			if (allValues[i].value == n)
				return allValues[i];
		}
		return NONE;
	}

	/**
	 * Converts the Symbology to its corresponding integer value.
	 * 
	 * @return int representing a Symbology value.
	 */
	public int toInt() {
		return value;
	}
}