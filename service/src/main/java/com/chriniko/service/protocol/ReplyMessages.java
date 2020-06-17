package com.chriniko.service.protocol;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.service.dto.view.ViewDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

public class ReplyMessages {

	// Note: Marker Interface for Algebraic Data Type.
	interface ServiceOperationMessage {
	}

	// --- FOR SUCCESS ---

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Getter
	@RequiredArgsConstructor
	public static class ServiceOperationSuccessMessage<R extends ViewDto> implements ServiceOperationMessage {

		private final ServiceOperationResult serviceOperationResult;
		private final R result;
	}


	public enum ServiceOperationResult {

		// creations
		FILM_CREATED("film created successfully"),
		FILM_COPY_CREATED("film copy created successfully"),
		CUSTOMER_CREATED("customer created successfully"),

		// searches
		FIND_FILM_BY_ID_EXECUTED("find film by id executed"),
		FIND_FILM_COPY_BY_ID_EXECUTED("find film copy by id executed"),
		FIND_CUSTOMER_BY_ID_EXECUTED("find customer by id executed"),
		FIND_CUSTOMER_RENTED_FILM_COPIES_EXECUTED("find customer rented film copies executed"),
		GET_CUSTOMER_BONUS_EXECUTED("get customer bonus executed"),

		// modifications
		FILM_COPY_RENT_EXECUTED("film copy rent operation executed"),
		FILM_COPY_RETURN_EXECUTED("film copy return operation executed");


		@Getter
		private final String message;

		ServiceOperationResult(String message) {
			this.message = message;
		}
	}

	// --- FOR FAILURE ---

	@Getter
	public static class ServiceOperationFailureMessage implements ServiceOperationMessage {

		private final Instant occurredAt = Instant.now(ClockProvider.getClock());
		private final String message;

		public ServiceOperationFailureMessage(String message) {
			this.message = message;
		}
	}

}
