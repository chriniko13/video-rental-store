package com.chriniko.e2e;

import com.chriniko.e2e.infra.FileLoader;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateFilmFeature {

	public static JsonObject filmJson;
	public static RestTemplate restTemplate = new RestTemplate();
	public static ResponseEntity<String> createFilmResponseOperation;

	public static long justCreatedFilmId;

	@Given("we have a film which we want to store it in the system")
	public void we_have_a_film_which_we_want_to_store_it_in_the_system() {

		filmJson = new JsonObject();
		filmJson.put("name", "12 angry men");
		filmJson.put("type", "OLD");
	}

	@When("we submit the film")
	public void we_submit_the_film() throws Exception {

		String payload = Json.encode(filmJson);

		createFilmResponseOperation
				= restTemplate.postForEntity("http://localhost:8080/films", payload, String.class);

		assertEquals(201, createFilmResponseOperation.getStatusCodeValue());

		String responseBody = createFilmResponseOperation.getBody();
		assertNotNull(responseBody);

		JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);

		String serviceOperationResult = jsonNode.get("serviceOperationResult").asText();
		assertEquals("FILM_CREATED", serviceOperationResult);

		String resultType = jsonNode.get("result").get("view-type").asText();
		assertEquals("entity-id-view", resultType);

		justCreatedFilmId = jsonNode.get("result").get("id").asInt();
	}

	@Then("the film has successfully stored in the system")
	public void the_film_has_successfully_stored_in_the_system() throws Exception {

		ResponseEntity<String> getFilmByIdResponseOperation = restTemplate.getForEntity("http://localhost:8080/films/" + justCreatedFilmId, String.class);
		assertEquals(200, getFilmByIdResponseOperation.getStatusCodeValue());

		String responseBody = getFilmByIdResponseOperation.getBody();

		String expected = FileLoader.load("/responses/getFilmById.json");

		JSONAssert.assertEquals(expected, responseBody, true);

	}

}
