/*
  The MIT License (MIT)

  Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)

  Permission is hereby granted, free of charge, to any person obtaining a copy of
  this software and associated documentation files (the "Software"), to deal in
  the Software without restriction, including without limitation the rights to
  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.candles;

import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;

/**
 * Three black crows indicator.
 * <p></p>
 * @see <a href="http://www.investopedia.com/terms/t/three_black_crows.asp">
 *     http://www.investopedia.com/terms/t/three_black_crows.asp</a>
 */
public class ThreeBlackCrowsIndicator extends CachedIndicator<Boolean> {

    private final TimeSeries series;

    /** Lower shadow */
    private final LowerShadowIndicator lowerShadowInd;
    /** Average lower shadow */
    private final SMAIndicator averageLowerShadowInd;
    /** Factor used when checking if a candle has a very short lower shadow */
    private final Decimal factor;

    private int whiteCandleIndex = -1;

    /**
     * Constructor.
     * @param series a time series
     * @param timeFrame the number of bars used to calculate the average lower shadow
     * @param factor the factor used when checking if a candle has a very short lower shadow
     */
    public ThreeBlackCrowsIndicator(TimeSeries series, int timeFrame, Decimal factor) {
        super(series);
        this.series = series;
        lowerShadowInd = new LowerShadowIndicator(series);
        averageLowerShadowInd = new SMAIndicator(lowerShadowInd, timeFrame);
        this.factor = factor;
    }

    @Override
    protected Boolean calculate(int index) {
        if (index < 3) {
            // We need 4 candles: 1 white, 3 black
            return false;
        }
        whiteCandleIndex = index - 3;
        return series.getBar(whiteCandleIndex).isBullish()
                && isBlackCrow(index - 2)
                && isBlackCrow(index - 1)
                && isBlackCrow(index);
    }

    /**
     * @param index the bar/candle index
     * @return true if the bar/candle has a very short lower shadow, false otherwise
     */
    private boolean hasVeryShortLowerShadow(int index) {
        Decimal currentLowerShadow = lowerShadowInd.getValue(index);
        // We use the white candle index to remove to bias of the previous crows
        Decimal averageLowerShadow = averageLowerShadowInd.getValue(whiteCandleIndex);

        return currentLowerShadow.isLessThan(averageLowerShadow.multipliedBy(factor));
    }

    /**
     * @param index the current bar/candle index
     * @return true if the current bar/candle is declining, false otherwise
     */
    private boolean isDeclining(int index) {
        Bar prevBar = series.getBar(index-1);
        Bar currBar = series.getBar(index);
        final Decimal prevOpenPrice = prevBar.getOpenPrice();
        final Decimal prevClosePrice = prevBar.getClosePrice();
        final Decimal currOpenPrice = currBar.getOpenPrice();
        final Decimal currClosePrice = currBar.getClosePrice();

        // Opens within the body of the previous candle
        return currOpenPrice.isLessThan(prevOpenPrice) && currOpenPrice.isGreaterThan(prevClosePrice)
                // Closes below the previous close price
                && currClosePrice.isLessThan(prevClosePrice);
    }

    /**
     * @param index the current bar/candle index
     * @return true if the current bar/candle is a black crow, false otherwise
     */
    private boolean isBlackCrow(int index) {
        Bar prevBar = series.getBar(index-1);
        Bar currBar = series.getBar(index);
        if (currBar.isBearish()) {
            if (prevBar.isBullish()) {
                // First crow case
                return hasVeryShortLowerShadow(index)
                        && currBar.getOpenPrice().isLessThan(prevBar.getMaxPrice());
            } else {
                return hasVeryShortLowerShadow(index) && isDeclining(index);
            }
        }
        return false;
    }
}