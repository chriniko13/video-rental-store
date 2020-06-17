package com.chriniko.domain.calculator;

import com.chriniko.domain.Film;
import com.chriniko.domain.FilmType;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class FilmPriceCalculatorTest {

	@Test
	public void process() {

		Film newReleasedFilm = Film.create("film 1", FilmType.NEW_RELEASE);
		BigDecimal result = FilmPriceCalculator.process(newReleasedFilm, 10);
		assertEquals(BigDecimal.valueOf(400.0), result);

		Film regularFilm = Film.create("film 2", FilmType.REGULAR);
		result = FilmPriceCalculator.process(regularFilm, 5);
		assertEquals(BigDecimal.valueOf(90.0), result);

		Film oldFilm = Film.create("film 3", FilmType.OLD);
		result = FilmPriceCalculator.process(oldFilm, 7);
		assertEquals(BigDecimal.valueOf(90.0), result);

	}
}
