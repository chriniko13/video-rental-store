package com.chriniko.domain;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.domain.base.Record;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor

@EqualsAndHashCode(of = { "customer", "filmCopy" }, callSuper = false)

@ToString(of = { "id", "rented", "returned", "cost", "bonus" })

@Entity
@Table(name = "customers_rental_history_films")

@Cache(region = "customerRentalHistoryCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class CustomerRentalHistoryFilmCopy implements Record<CustomerRentalHistoryFilmCopyId> {

	@EmbeddedId
	private CustomerRentalHistoryFilmCopyId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("customerId")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("filmCopyId")
	private FilmCopy filmCopy;

	@Column(nullable = false)
	private Instant rented = Instant.now(ClockProvider.getClock());

	private Instant returned;

	private BigDecimal cost;

	private Integer bonus;

	private Instant createdAt;

	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now(ClockProvider.getClock());
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now(ClockProvider.getClock());
	}

	@Version
	private int version;

	public CustomerRentalHistoryFilmCopy(Customer customer, FilmCopy filmCopy) {
		this.customer = customer;
		this.filmCopy = filmCopy;
		this.id = new CustomerRentalHistoryFilmCopyId(customer.getId(), filmCopy.getId());
	}

	@Override public CustomerRentalHistoryFilmCopyId extractId() {
		return id;
	}
}
