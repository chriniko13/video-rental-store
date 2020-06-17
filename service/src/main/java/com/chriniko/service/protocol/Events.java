package com.chriniko.service.protocol;

public class Events {

	// creations
	public static final String CREATE_FILM_COPY = "create_film_copy";
	public static final String CREATE_FILM = "create_film";
	public static final String CREATE_CUSTOMER = "create_customer";

	// searches
	public static final String FIND_FILM_BY_ID = "find_film_by_id";
	public static final String FIND_FILM_COPY_BY_ID = "find_film_copy_by_id";
	public static final String FIND_CUSTOMER_BY_ID = "find_customer_by_id";
	public static final String FIND_CUSTOMER_BY_NAME = "find_customer_by_name";
	public static final String FIND_CUSTOMER_RENTED_FILM_COPIES = "find_customer_rented_film_copies";
	public static final String GET_CUSTOMER_BONUS = "get_customer_bonus";

	// business operations
	public static final String RENT_FILM_COPY = "rent_film_copy";
	public static final String RETURN_FILM_COPY = "return_film_copy";

	private Events() {
	}
}
