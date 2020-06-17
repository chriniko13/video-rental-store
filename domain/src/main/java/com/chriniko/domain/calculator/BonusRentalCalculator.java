package com.chriniko.domain.calculator;

import com.chriniko.common.infra.PropertiesHolder;
import com.chriniko.domain.Film;
import com.chriniko.domain.FilmType;

public class BonusRentalCalculator {

	private static final int newReleaseBonus;
	private static final int fallbackBonus;

	static {
		newReleaseBonus = PropertiesHolder.getAsInt("newrelease.bonus");
		fallbackBonus = PropertiesHolder.getAsInt("fallback.bonus");
	}

	private BonusRentalCalculator() {
	}

	public static int process(Film film) {
		if (film.getFilmType() == FilmType.NEW_RELEASE) {
			return newReleaseBonus;
		}
		return fallbackBonus;
	}
}
