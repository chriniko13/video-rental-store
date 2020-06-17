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

public class ReturnFilmCopyFeature {

	public static String username;

	public static long justCreatedFilmId;
	public static long justCreatedFilmCopyId;
	public static long justCreatedCustomerId;

	public static RestTemplate restTemplate = new RestTemplate();

	@Given("we have a registered customer, which has rented a film copy")
	public void we_have_a_registered_customer_which_has_rented_a_film_copy() throws Exception {

		// create film section
		JsonObject filmJson = new JsonObject();
		filmJson.put("name", "The Big Short");
		filmJson.put("type", "REGULAR");

		String payload = Json.encode(filmJson);

		ResponseEntity<String> createFilmResponseOperation
				= restTemplate.postForEntity("http://localhost:8080/films", payload, String.class);

		assertEquals(201, createFilmResponseOperation.getStatusCodeValue());

		String responseBody = createFilmResponseOperation.getBody();
		assertNotNull(responseBody);

		JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);
		justCreatedFilmId = jsonNode.get("result").get("id").asInt();

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

		justCreatedFilmCopyId = jsonNode.get("result").get("id").asInt();

		// create customer section
		username = "user_sample_3";

		JsonObject customerJson = new JsonObject();
		customerJson.put("username", username);
		customerJson.put("firstname", "firstname sample");
		customerJson.put("initials", "initials sample");
		customerJson.put("surname", "surname sample");
		payload = Json.encode(customerJson);

		ResponseEntity<String> createCustomerOperationResponse
				= restTemplate.postForEntity("http://localhost:8080/customers", payload, String.class);

		assertEquals(201, createCustomerOperationResponse.getStatusCodeValue());

		responseBody = createCustomerOperationResponse.getBody();
		assertNotNull(responseBody);

		jsonNode = DatabindCodec.mapper().readTree(responseBody);

		serviceOperationResult = jsonNode.get("serviceOperationResult").asText();
		assertEquals("CUSTOMER_CREATED", serviceOperationResult);

		resultType = jsonNode.get("result").get("view-type").asText();
		assertEquals("entity-id-view", resultType);

		justCreatedCustomerId = jsonNode.get("result").get("id").asInt();

		// go back 5 days in the past
		Instant instant = Instant.now().minus(5, ChronoUnit.DAYS);
		Clock clock = Clock.fixed(
				instant,
				ZoneOffset.UTC);
		ClockProvider.setClock(clock);

		// perform rent film copy
		JsonObject rentFilmJson = new JsonObject();
		rentFilmJson.put("filmCopyId", justCreatedFilmCopyId);

		payload = Json.encode(rentFilmJson);

		ResponseEntity<String> rentFilmCopyResultOperation = restTemplate.postForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental", payload, String.class);

		assertEquals(201, rentFilmCopyResultOperation.getStatusCodeValue());

		String expected = FileLoader.load("/responses/rentFilmCopy2.json");

		JSONAssert.assertEquals(expected, rentFilmCopyResultOperation.getBody(), true);
	}

	@When("this customer returns the rented film copy")
	public void this_customer_returns_the_rented_film_copy() throws Exception {

		// set clock at present-now
		ClockProvider.setClock(Clock.systemDefaultZone());

		// return rented film copy
		JsonObject returnFilmJson = new JsonObject();
		returnFilmJson.put("filmCopyId", justCreatedFilmCopyId);

		String payload = Json.encode(returnFilmJson);

		ResponseEntity<String> returnFilmOperation = restTemplate.postForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/return", payload, String.class);

		assertEquals(200, returnFilmOperation.getStatusCodeValue());

		String body = returnFilmOperation.getBody();

		String expected = FileLoader.load("/responses/returnFilmCopy.json");

		JSONAssert.assertEquals(expected, body, new CustomComparator(JSONCompareMode.STRICT,
						new Customization("result.rented", (o1, o2) -> true),
						new Customization("result.returned", (o1, o2) -> true)
				)
		);
	}

	@Then("a return film copy entry has been successfully created to the system, and bonus awarded to customer")
	public void a_return_film_copy_entry_has_been_successfully_created_to_the_system_and_bonus_awarded_to_customer()
			throws Exception {

		// check customer's returned rented film copies

		ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental/copies?returned=true", String.class);
		assertEquals(200, responseEntity.getStatusCodeValue());

		String expected = FileLoader.load("/responses/getCustomerRentedFilmCopiesReturned.json");
		String actual = responseEntity.getBody();

		JSONAssert.assertEquals(expected, actual, new CustomComparator(JSONCompareMode.STRICT,
				new Customization("result.rentedResults[0].rented", (o1, o2) -> true),
				new Customization("result.rentedResults[0].returned", (o1, o2) -> true))
		);

		// and also check that customer has not any not returned film copies
		responseEntity = restTemplate.getForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental/copies?returned=false", String.class);
		assertEquals(200, responseEntity.getStatusCodeValue());

		assertEquals("{\"serviceOperationResult\":\"FIND_CUSTOMER_RENTED_FILM_COPIES_EXECUTED\",\"result\":{\"view-type\":\"customer-rented-film-copies-view\",\"rentedResults\":[]}}", responseEntity.getBody());
	}

}
