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

public class CreateCustomerFeature {

	public static JsonObject customerJson;

	public static String username;

	public static RestTemplate restTemplate = new RestTemplate();
	public static ResponseEntity<String> createCustomerOperationResponse;

	public static long justCreatedCustomerId;

	@Given("we have created a customer account")
	public void weHaveCreatedACustomerAccount() {
		username = "user_sample";

		customerJson = new JsonObject();
		customerJson.put("username", username);
		customerJson.put("firstname", "firstname sample");
		customerJson.put("initials", "initials sample");
		customerJson.put("surname", "surname sample");
	}

	@When("we submit it to the system")
	public void weSubmitItToTheSystem() throws Exception {
		String payload = Json.encode(customerJson);

		createCustomerOperationResponse
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
	}

	@Then("the customer account has been successfully stored in the system")
	public void theCustomerAccountHasBeenSuccessfullyStoredInTheSystem() throws Exception {

		ResponseEntity<String> getCustomerByIdResponseOperation = restTemplate.getForEntity("http://localhost:8080/customers/" + justCreatedCustomerId, String.class);
		assertEquals(200, getCustomerByIdResponseOperation.getStatusCodeValue());

		String responseBody = getCustomerByIdResponseOperation.getBody();
		String expected = FileLoader.load("/responses/getCustomerById.json");
		JSONAssert.assertEquals(expected, responseBody, true);

		ResponseEntity<String> getCustomerByUsernameResponseOperation = restTemplate.getForEntity("http://localhost:8080/search/customers?username=" + username, String.class);
		assertEquals(200, getCustomerByUsernameResponseOperation.getStatusCodeValue());

		responseBody = getCustomerByUsernameResponseOperation.getBody();
		expected = FileLoader.load("/responses/getCustomerByUsername.json");
		JSONAssert.assertEquals(expected, responseBody, true);

	}
}
