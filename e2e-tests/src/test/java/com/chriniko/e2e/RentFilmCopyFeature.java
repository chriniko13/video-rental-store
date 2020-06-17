package com.chriniko.e2e;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RentFilmCopyFeature {

	public static String username;

	public static long justCreatedFilmId;
	public static long justCreatedFilmCopyId;
	public static long justCreatedCustomerId;

	public static RestTemplate restTemplate = new RestTemplate();

	@Given("we have an already registered film, registered film copy and a registered customer to the system")
	public void we_have_an_already_registered_film_registered_film_copy_and_a_registered_customer_to_the_system()
			throws Exception {

		// create film section
		JsonObject filmJson = new JsonObject();
		filmJson.put("name", "Ocean Eleven");
		filmJson.put("type", "OLD");

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
		username = "user_sample_2";

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

	}

	@When("we rent the selected film copy to the customer")
	public void we_rent_the_selected_film_copy_to_the_customer() throws Exception {

		JsonObject rentFilmJson = new JsonObject();
		rentFilmJson.put("filmCopyId", justCreatedFilmCopyId);

		String payload = Json.encode(rentFilmJson);

		ResponseEntity<String> rentFilmCopyResultOperation = restTemplate.postForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental", payload, String.class);

		assertEquals(201, rentFilmCopyResultOperation.getStatusCodeValue());

		String expected = FileLoader.load("/responses/rentFilmCopy.json");

		JSONAssert.assertEquals(expected, rentFilmCopyResultOperation.getBody(), true);
	}

	@Then("a film copy entry has been successfully created to the system")
	public void a_film_copy_entry_has_been_successfully_created_to_the_system() throws Exception {

		ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/customer/" + justCreatedCustomerId + "/rental/copies", String.class);

		assertEquals(200, responseEntity.getStatusCodeValue());

		String expected = FileLoader.load("/responses/getCustomerRentedFilmCopies.json");

		String actual = responseEntity.getBody();

		JSONAssert.assertEquals(expected, actual, new CustomComparator(JSONCompareMode.STRICT,
				new Customization("result.rentedResults[0].rented", (o1, o2) -> true)));
	}
}
