package eu.verdelhan.ta4j.indicator.tracker.bollingerbands;

import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicator.helper.StandardDeviation;

/**
 * Buy - Occurs when the price line cross from down to up de Bollinger Band Low.
 * Sell - Occurs when the price line cross from up to down de Bollinger Band
 * High.
 * 
 */
public class BollingerBandsLower implements Indicator<Double> {

	private final Indicator<? extends Number> indicator;

	private final BollingerBandsMiddle bbm;

	public BollingerBandsLower(BollingerBandsMiddle bbm, StandardDeviation standardDeviation) {
		this.bbm = bbm;
		this.indicator = standardDeviation;
	}

	public BollingerBandsLower(BollingerBandsMiddle bbm, Indicator<? extends Number> indicator) {
		this.bbm = bbm;
		this.indicator = indicator;
	}

	@Override
	public Double getValue(int index) {
		return bbm.getValue(index).doubleValue() - 2 * indicator.getValue(index).doubleValue();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "deviation: " + indicator + "series: " + bbm;
	}
}