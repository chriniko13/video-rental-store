package com.chriniko.service;

import com.chriniko.domain.Customer;
import com.chriniko.domain.CustomerRentalHistoryFilmCopy;
import com.chriniko.domain.CustomerRentalHistoryFilmCopyId;
import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.base.TxResult;
import com.chriniko.domain.repository.CustomerRepository;
import com.chriniko.domain.repository.FilmCopyRepository;
import com.chriniko.service.command.CreateCustomerCommand;
import com.chriniko.service.command.FindCustomerByIdCommand;
import com.chriniko.service.command.FindCustomerByUsernameCommand;
import com.chriniko.service.command.FindCustomerRentedFilmCopiesCommand;
import com.chriniko.service.command.GetCustomerBonusCommand;
import com.chriniko.service.command.RentFilmCopyCommand;
import com.chriniko.service.command.ReturnFilmCopyCommand;
import com.chriniko.service.dto.view.CompositeEntityIdViewDto;
import com.chriniko.service.dto.view.CustomerBonusViewDto;
import com.chriniko.service.dto.view.CustomerRentedFilmCopiesViewDto;
import com.chriniko.service.dto.view.CustomerRentedFilmCopyViewDto;
import com.chriniko.service.dto.view.CustomerViewDto;
import com.chriniko.service.dto.view.EntityIdViewDto;
import com.chriniko.service.error.ErrorCodes;
import com.chriniko.service.protocol.Events;
import com.chriniko.service.protocol.ReplyMessages;
import com.google.inject.Inject;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.chriniko.service.protocol.ReplyMessages.ServiceOperationResult.*;


public class CustomerServiceVerticle extends ServiceVerticle {

	private static final Logger LOG = Logger.getLogger(CustomerServiceVerticle.class);

	private final CustomerRepository customerRepository;
	private final FilmCopyRepository filmCopyRepository;

	private final BiFunction<Long, String, String> errMsgFunction;
	private final String customerEntityType;

	@Inject
	public CustomerServiceVerticle(CustomerRepository customerRepository, FilmCopyRepository filmCopyRepository) {
		super(LOG);
		this.customerRepository = customerRepository;
		this.filmCopyRepository = filmCopyRepository;

		this.errMsgFunction = (id, entityType) -> entityType + " with id: " + id + " not exists";
		this.customerEntityType = "customer";
	}

	@Override
	public void start(Promise<Void> startPromise) {
		handleCreateCustomerEvent();
		handleRentFilmCopyEvent();
		handleReturnFilmCopyEvent();

		handleFindCustomerByIdEvent();
		handleFindByCustomerNameEvent();
		handleFindCustomerRentedFilmCopiesEvent();
		handleGetCustomerBonusEvent();

		startPromise.complete();
	}

	private void handleGetCustomerBonusEvent() {
		vertx.eventBus().<GetCustomerBonusCommand>consumer(Events.GET_CUSTOMER_BONUS, enrichWithErrorHandling(msg -> {

			GetCustomerBonusCommand cmd = msg.body();
			long customerId = cmd.getCustomerId();
			boolean detailed = cmd.isDetailed();

			TxResult<CustomerBonusViewDto> txResult = customerRepository.getTxBoundary().executeInTx(() -> {

				final Customer customer = customerRepository.find(customerId);
				if (customer == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(customerId, customerEntityType));
				}

				CustomerBonusViewDto viewDto = new CustomerBonusViewDto();
				Integer totalBonuses = customer.getTotalBonuses();
				viewDto.setTotalBonuses(totalBonuses);

				if (detailed) {
					List<CustomerBonusViewDto.CustomerBonusHistory> customerBonusHistories = customer.getRentalHistories()
							.stream()
							.map(CustomerBonusViewDto.CustomerBonusHistory::transform)
							.collect(Collectors.toList());

					viewDto.setHistory(customerBonusHistories);
				}

				return new TxResult<>(viewDto);
			});

			txResult.execute(
					msg::fail,
					r -> msg.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(GET_CUSTOMER_BONUS_EXECUTED, r))
			);

		}));
	}

	private void handleFindCustomerRentedFilmCopiesEvent() {
		vertx.eventBus().<FindCustomerRentedFilmCopiesCommand>consumer(Events.FIND_CUSTOMER_RENTED_FILM_COPIES, enrichWithErrorHandling(msg -> {

			FindCustomerRentedFilmCopiesCommand command = msg.body();

			long customerId = command.getCustomerId();
			boolean returned = command.isReturned();

			TxResult<List<CustomerRentedFilmCopyViewDto>> txResult = customerRepository.getTxBoundary().executeInTx(() -> {

				List<CustomerRentalHistoryFilmCopy> result = customerRepository.getRentalHistories(customerId, returned);

				List<CustomerRentedFilmCopyViewDto> results = result
						.stream()
						.map(CustomerRentedFilmCopyViewDto::transform)
						.collect(Collectors.toList());

				return new TxResult<>(results);
			});

			List<CustomerRentedFilmCopyViewDto> results = txResult.getResult();
			msg.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(FIND_CUSTOMER_RENTED_FILM_COPIES_EXECUTED, new CustomerRentedFilmCopiesViewDto(results)));

		}));
	}

	private void handleReturnFilmCopyEvent() {
		vertx.eventBus().<ReturnFilmCopyCommand>consumer(Events.RETURN_FILM_COPY, enrichWithErrorHandling(msg -> {

			ReturnFilmCopyCommand command = msg.body();

			TxResult<CustomerRentedFilmCopyViewDto> txResult = customerRepository.getTxBoundary().executeInTx(() -> {

				long customerId = command.getCustomerId();
				Customer customer = customerRepository.find(customerId);
				if (customer == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR,  errMsgFunction.apply(customerId, customerEntityType));
				}

				long filmCopyId = command.getFilmCopyId();
				FilmCopy filmCopy = filmCopyRepository.find(filmCopyId);
				if (filmCopy == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR,  errMsgFunction.apply(filmCopyId, "film copy"));
				}

				CustomerRentalHistoryFilmCopy result = customerRepository.returnFilmCopy(filmCopy, customer);
				CustomerRentedFilmCopyViewDto view = CustomerRentedFilmCopyViewDto.transform(result);
				return new TxResult<>(view);

			});

			txResult.execute(
					msg::fail,
					r -> msg.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(FILM_COPY_RETURN_EXECUTED, r))
			);

		}));
	}

	private void handleRentFilmCopyEvent() {
		vertx.eventBus().<RentFilmCopyCommand>consumer(Events.RENT_FILM_COPY, enrichWithErrorHandling(msg -> {

			RentFilmCopyCommand command = msg.body();

			TxResult<CustomerRentalHistoryFilmCopyId> txResult = customerRepository.getTxBoundary().executeInTx(() -> {

				long customerId = command.getCustomerId();
				Customer customer = customerRepository.find(customerId);
				if (customer == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(customerId, customerEntityType));
				}

				long filmCopyId = command.getFilmCopyId();
				FilmCopy filmCopy = filmCopyRepository.find(filmCopyId);
				if (filmCopy == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(filmCopyId, "film copy"));
				}

				CustomerRentalHistoryFilmCopy result = customerRepository.rentFilmCopy(filmCopy, customer);
				CustomerRentalHistoryFilmCopyId id = result.getId();
				return new TxResult<>(id);
			});

			txResult.execute(
					msg::fail,
					r -> {
						JsonObject json = new JsonObject();
						json.put("customerId", r.getCustomerId());
						json.put("filmCopyId", r.getFilmCopyId());
						String payload = Json.encode(json);

						msg.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(FILM_COPY_RENT_EXECUTED, new CompositeEntityIdViewDto(payload)));
					}
			);

		}));
	}

	private void handleCreateCustomerEvent() {
		vertx.eventBus().<CreateCustomerCommand>consumer(Events.CREATE_CUSTOMER, enrichWithErrorHandling(msg -> {

					CreateCustomerCommand command = msg.body();
					Customer customer = Customer.create(command.getUsername(), command.getFirstname(), command.getInitials(), command.getSurname());

					TxResult<Long> txResult = customerRepository.getTxBoundary().executeInTx(() -> {
						Long id = customerRepository.save(customer);
						return new TxResult<>(id);
					});

					Long result = txResult.getResult();
					msg.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(CUSTOMER_CREATED, new EntityIdViewDto(result)));
				})
		);
	}

	private void handleFindByCustomerNameEvent() {
		vertx.eventBus().<FindCustomerByUsernameCommand>consumer(Events.FIND_CUSTOMER_BY_NAME, enrichWithErrorHandling(msg -> {

			FindCustomerByUsernameCommand command = msg.body();
			String username = command.getUsername();

			if (!command.isApproximatelySearch()) {

				TxResult<Customer> txResult = customerRepository.getTxBoundary().executeInTx(() -> {
					Customer result = customerRepository.findByUsername(username);
					if (result == null) {
						return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, "customer with username: " + username + " not exists");
					} else {
						return new TxResult<>(result);
					}
				});

				txResult.execute(
						msg::fail,
						r -> msg.reply(
								new ReplyMessages.ServiceOperationSuccessMessage<>(
										FIND_CUSTOMER_BY_ID_EXECUTED,
										new CustomerViewDto(r.getId(), r.getUsername(), r.getFirstname(),
												r.getInitials(), r.getSurname())
								)
						)
				);

			} else {
				msg.fail(ErrorCodes.OPERATION_NOT_SUPPORTED_YET_ERROR, "");
			}
		}));
	}

	private void handleFindCustomerByIdEvent() {
		vertx.eventBus().<FindCustomerByIdCommand>consumer(Events.FIND_CUSTOMER_BY_ID, enrichWithErrorHandling(msg -> {

			FindCustomerByIdCommand command = msg.body();
			long id = command.getId();

			TxResult<Customer> txResult = customerRepository.getTxBoundary().executeInTx(() -> {
				Customer result = customerRepository.find(id);
				if (result == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(id, customerEntityType));
				} else {
					return new TxResult<>(result);
				}
			});

			txResult.execute(
					msg::fail,
					r -> msg.reply(
							new ReplyMessages.ServiceOperationSuccessMessage<>(
									FIND_CUSTOMER_BY_ID_EXECUTED,
									new CustomerViewDto(r.getId(), r.getUsername(), r.getFirstname(),
											r.getInitials(), r.getSurname())
							)
					)
			);

		}));
	}
}
