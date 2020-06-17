package com.chriniko.domain.calculator;

import com.chriniko.domain.Film;
import com.chriniko.domain.FilmType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BonusRentalCalculatorTest {

	@Test
	public void process() {

		int result = BonusRentalCalculator.process(Film.create("f1", FilmType.NEW_RELEASE));
		assertEquals(2, result);

		result = BonusRentalCalculator.process(Film.create("f2", FilmType.REGULAR));
		assertEquals(1, result);

		result = BonusRentalCalculator.process(Film.create("f3", FilmType.OLD));
		assertEquals(1, result);
	}
}
