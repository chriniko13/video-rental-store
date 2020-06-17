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

public class CreateFilmCopyFeature {

	public static JsonObject filmJson;

	public static long justCreatedFilmId;
	public static long justCreatedFilmCopyId;

	public static RestTemplate restTemplate = new RestTemplate();

	@Given("we have an already registered film")
	public void we_have_an_already_registered_film() throws Exception {

		filmJson = new JsonObject();
		filmJson.put("name", "Planet of Apes");
		filmJson.put("type", "OLD");

		String payload = Json.encode(filmJson);

		ResponseEntity<String> createFilmResponseOperation
				= restTemplate.postForEntity("http://localhost:8080/films", payload, String.class);

		assertEquals(201, createFilmResponseOperation.getStatusCodeValue());

		String responseBody = createFilmResponseOperation.getBody();
		assertNotNull(responseBody);

		JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);
		justCreatedFilmId = jsonNode.get("result").get("id").asInt();

	}

	@When("we submit the film copy")
	public void we_submit_the_film_copy() throws Exception {

		JsonObject filmCopyJson = new JsonObject();
		filmCopyJson.put("status", "EXCELLENT");

		String payload = Json.encode(filmCopyJson);

		ResponseEntity<String> createFilmCopyResponseOperation
				= restTemplate.postForEntity("http://localhost:8080/films/" + justCreatedFilmId + "/copies/", payload, String.class);

		assertEquals(201, createFilmCopyResponseOperation.getStatusCodeValue());

		String responseBody = createFilmCopyResponseOperation.getBody();
		assertNotNull(responseBody);

		JsonNode jsonNode = DatabindCodec.mapper().readTree(responseBody);

		String serviceOperationResult = jsonNode.get("serviceOperationResult").asText();
		assertEquals("FILM_COPY_CREATED", serviceOperationResult);

		String resultType = jsonNode.get("result").get("view-type").asText();
		assertEquals("entity-id-view", resultType);

		justCreatedFilmCopyId = jsonNode.get("result").get("id").asInt();

	}

	@Then("the film copy has successfully stored in the system")
	public void the_film_copy_has_successfully_stored_in_the_system() throws Exception {

		ResponseEntity<String> result = restTemplate.getForEntity("http://localhost:8080/copies/" + justCreatedFilmCopyId, String.class);

		assertEquals(200, result.getStatusCodeValue());

		String expected = FileLoader.load("/responses/getFilmCopyById.json");
		String actual = result.getBody();

		JSONAssert.assertEquals(expected, actual, true);
	}

}
