package org.openl.meta;


public class DoubleValuePercent extends DoubleValue {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6543033363886217906L;

	public DoubleValuePercent() {
		setFormat(PERCENT_FORMAT);
	}

	public DoubleValuePercent(double d) {
		super(d);
		setFormat(PERCENT_FORMAT);
	}

	public DoubleValuePercent(String valueStr) {
		super(valueStr);
		setFormat(PERCENT_FORMAT);
	}


	static public final String PERCENT_FORMAT = "#.####%";


}
