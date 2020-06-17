package com.chriniko.domain.calculator;

import com.chriniko.common.infra.PropertiesHolder;
import com.chriniko.domain.Film;
import com.chriniko.domain.FilmType;

import java.math.BigDecimal;

public class FilmPriceCalculator {

	private static final double premiumPrice;
	private static final double basicPrice;

	static {
		premiumPrice = PropertiesHolder.getAsDouble("premium.price");
		basicPrice = PropertiesHolder.getAsDouble("basic.price");
	}

	private FilmPriceCalculator() {
	}

	public static BigDecimal process(Film film, long daysDiff) {

		double result = 0;
		long delta;
		FilmType type = film.getFilmType();

		switch (type) {

			case NEW_RELEASE:
				result = premiumPrice * daysDiff;
				break;

			case OLD:
			case REGULAR:
				if (type == FilmType.OLD) {
					delta = daysDiff - 5;
				} else {
					delta = daysDiff - 3;
				}

				if (delta > 0) {
					result = basicPrice + (delta * basicPrice);
				} else {
					result = basicPrice;
				}

				break;

			default:
				throw new IllegalStateException("this should never happen");
		}
		return BigDecimal.valueOf(result);
	}

}
