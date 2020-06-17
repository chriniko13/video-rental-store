package com.chriniko.e2e;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.e2e.infra.FileLoader;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetCustomerBonusFeature {

	public static String getCustomerBonusResponseBody;

	public static String username;

	public static long justCreatedCustomerId;

	public static RestTemplate restTemplate = new RestTemplate();

	@Given("we have a registered customer")
	public void we_have_a_registered_customer() throws Exception {

		// create films section
		final String[][] films = new String[][] {
				{ "Dirty Harry", "OLD" },
				{ "Mad Max: Fury Road", "NEW_RELEASE" },
				{ "Die Hard", "OLD" },
				{ "The Bourne Identity", "OLD" },
				{ "The Dark Knight", "REGULAR" }
		};

		final int[] filmCopiesIdsCreated = new int[films.length];
		int filmCopiesIdsCreatedIdx = 0;

		for (String[] film : films) {

			String filmName = film[0];
			String filmType = film[1];

			JsonObject filmJson = new JsonObject();
			filmJson.put("name", filmName);
			filmJson.put("type", filmType);

			String payload = Json.encode(filmJson);

			ResponseEntity<String> createFilmResponseOperation
					= restTemplate.postForEntity("http://localhost:8080/films", payload, String.class);

			assertEquals(201, createFilmResponseOperation.getStatusCodeValue());

			String responseBody = createFilmResponseOperation.getBody();
			assertNotNull(responseBody);

			JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);
			int justCreatedFilmId = jsonNode.get("result").get("id").asInt();

			// create film copy section
			JsonObject filmCopyJson = new JsonObject();
			filmCopyJson.put("status", "EXCELLENT");

			payload = Json.encode(filmCopyJson);

			ResponseEntity<String> createFilmCopyResponseOperation
					= restTemplate.postForEntity("http://localhost:8080/films/" + justCreatedFilmId + "/copies/", payload, String.class);

			assertEquals(201, createFilmCopyResponseOperation.getStatusCodeValue());

			responseBody = createFilmCopyResponseOperation.getBody();
			assertNotNull(responseBody);

			jsonNode = DatabindCodec.mapper().readTree(responseBody);

			System.out.println(jsonNode);

			String serviceOperationResult = jsonNode.get("serviceOperationResult").asText();
			assertEquals("FILM_COPY_CREATED", serviceOperationResult);

			String resultType = jsonNode.get("result").get("view-type").asText();
			assertEquals("entity-id-view", resultType);

			int justCreatedFilmCopyId = jsonNode.get("result").get("id").asInt();

			filmCopiesIdsCreated[filmCopiesIdsCreatedIdx++] = justCreatedFilmCopyId;
		}

		// create customer section
		username = "user_sample_4";

		JsonObject customerJson = new JsonObject();
		customerJson.put("username", username);
		customerJson.put("firstname", "firstname sample");
		customerJson.put("initials", "initials sample");
		customerJson.put("surname", "surname sample");
		String payload = Json.encode(customerJson);

		ResponseEntity<String> createCustomerOperationResponse
				= restTemplate.postForEntity("http://localhost:8080/customers", payload, String.class);

		assertEquals(201, createCustomerOperationResponse.getStatusCodeValue());

		String responseBody = createCustomerOperationResponse.getBody();
		assertNotNull(responseBody);

		JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);

		String serviceOperationResult = jsonNode.get("serviceOperationResult").asText();
		assertEquals("CUSTOMER_CREATED", serviceOperationResult);

		String resultType = jsonNode.get("result").get("view-type").asText();
		assertEquals("entity-id-view", resultType);

		justCreatedCustomerId = jsonNode.get("result").get("id").asInt();

		// time to make some rents
		final int[] daysInThePast = new int[] { 10, 7, 5, 3, 2 };
		if (daysInThePast.length != filmCopiesIdsCreated.length) {
			throw new IllegalStateException();
		}
		filmCopiesIdsCreatedIdx = 0;

		for (int dayInThePast : daysInThePast) {

			Instant instant = Instant.now().minus(dayInThePast, ChronoUnit.DAYS);
			Clock clock = Clock.fixed(
					instant,
					ZoneOffset.UTC);
			ClockProvider.setClock(clock);

			JsonObject rentFilmJson = new JsonObject();
			rentFilmJson.put("filmCopyId", filmCopiesIdsCreated[filmCopiesIdsCreatedIdx++]);

			payload = Json.encode(rentFilmJson);

			ResponseEntity<String> rentFilmCopyResultOperation = restTemplate.postForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental", payload, String.class);
			assertEquals(201, rentFilmCopyResultOperation.getStatusCodeValue());
		}

		// set clock at present-now
		ClockProvider.setClock(Clock.systemDefaultZone());

		// time to make returns
		for (int filmCopy : filmCopiesIdsCreated) {

			JsonObject returnFilmJson = new JsonObject();
			returnFilmJson.put("filmCopyId", filmCopy);

			payload = Json.encode(returnFilmJson);

			ResponseEntity<String> returnFilmOperation = restTemplate.postForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/return", payload, String.class);

			assertEquals(200, returnFilmOperation.getStatusCodeValue());
		}

	}

	@When("we select to see registered customer's total bonus")
	public void we_select_to_see_registered_customer_s_total_bonus() {

		ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/bonus?detailed=true", String.class);

		assertEquals(200, responseEntity.getStatusCodeValue());

		getCustomerBonusResponseBody = responseEntity.getBody();

	}

	@Then("we get a detailed response of customer's total bonus")
	public void we_get_a_detailed_response_of_customer_s_total_bonus() throws Exception {

		String expected = FileLoader.load("/responses/getCustomerBonus.json");

		JSONAssert.assertEquals(expected, expected, new CustomComparator(JSONCompareMode.STRICT,
						new Customization("result.history[*].rented", (o1, o2) -> true),
						new Customization("result.history[*].returned", (o1, o2) -> true)
				)
		);
	}

}
