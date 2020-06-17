package com.chriniko.e2e;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
		strict = true,
		glue = { "com.chriniko.e2e" },
		plugin = { "pretty", "json:target/cucumber-reports/Cucumber.json",
				"junit:target/cucumber-reports/Cucumber.xml",
				"html:target/cucumber-reports" },
		monochrome = true,
		features = {
				"src/test/resources/features/createFilm.feature",
				"src/test/resources/features/createCustomer.feature",
				"src/test/resources/features/createFilmCopy.feature",
				"src/test/resources/features/rentFilmCopy.feature",
				"src/test/resources/features/returnFilmCopy.feature",
				"src/test/resources/features/getCustomerBonus.feature"
		}
)
public class RunCucumberTests {

}
