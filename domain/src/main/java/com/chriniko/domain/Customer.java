package com.chriniko.domain;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.domain.base.Record;
import com.chriniko.domain.calculator.BonusRentalCalculator;
import com.chriniko.domain.calculator.FilmPriceCalculator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@EqualsAndHashCode(of = { "username" }, callSuper = false)

@ToString(of = { "id", "username" })

@Entity

@Table(
		name = "customers",
		uniqueConstraints = {
				@UniqueConstraint(name = "cust_username_unique", columnNames = { "username" })
		}
)

@NamedQuery(
		name = "Customer.getRentalHistories_Returned",
		query = "select cust.rentalHistories from Customer cust "
				+ "left join CustomerRentalHistoryFilmCopy rhf on rhf.id.customerId = cust.id "
				+ "where cust.id = :_id "
				+ "and rhf.returned is not null"
)

@NamedQuery(
		name = "Customer.getRentalHistories_NotReturned",
		query = "select cust.rentalHistories from Customer cust "
				+ "left join CustomerRentalHistoryFilmCopy rhf on rhf.id.customerId = cust.id "
				+ "where cust.id = :_id "
				+ "and rhf.returned is null"
)

@NamedQuery(
		name = "Customer.findByUsername",
		query = "select c from Customer c where c.username = :_username"
)

@Cache(region = "customerCache", usage = CacheConcurrencyStrategy.READ_WRITE)

public class Customer implements Record<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cust_Sequence")
	@SequenceGenerator(name = "cust_Sequence", sequenceName = "CUST_SEQ")
	private Long id;

	@Column(nullable = false, length = 20)
	private String username;

	@Column(nullable = false, length = 50)
	private String firstname;

	@Column(length = 50)
	private String initials;

	@Column(nullable = false, length = 50)
	private String surname;

	@OneToMany(
			mappedBy = "customer",
			cascade = CascadeType.ALL,
			fetch = FetchType.LAZY,
			orphanRemoval = true
	)
	private Set<CustomerRentalHistoryFilmCopy> rentalHistories = new LinkedHashSet<>();

	@Column(nullable = false)
	private Integer totalBonuses = 0;

	private Instant createdAt;

	private Instant updatedAt;
	@Version
	private int version;

	public static Customer create(String username, String firstname, String initials, String surname) {
		Customer c = new Customer();
		c.setUsername(username);
		c.setFirstname(firstname);
		c.setInitials(initials);
		c.setSurname(surname);
		return c;
	}

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now(ClockProvider.getClock());
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now(ClockProvider.getClock());
	}

	public CustomerRentalHistoryFilmCopy addRentalHistory(FilmCopy filmCopy) {
		CustomerRentalHistoryFilmCopy customerRentalHistoryFilmCopy = new CustomerRentalHistoryFilmCopy(this, filmCopy);
		rentalHistories.add(customerRentalHistoryFilmCopy);
		filmCopy.getRentalHistories().add(customerRentalHistoryFilmCopy);
		return customerRentalHistoryFilmCopy;
	}

	public void returnFilm(CustomerRentalHistoryFilmCopy record) {

		Instant returned = Instant.now(ClockProvider.getClock());
		record.setReturned(returned);

		Film film = record.getFilmCopy().getFilm();

		long daysDiff = Duration.between(record.getRented(), returned).abs().toDays();
		BigDecimal result = FilmPriceCalculator.process(film, daysDiff);
		record.setCost(result);

		if (result.compareTo(BigDecimal.ZERO) > 0) {
			int bonus = BonusRentalCalculator.process(film);
			record.setBonus(bonus);

			this.setTotalBonuses(this.getTotalBonuses() + bonus);
		}
	}

	@Override public Long extractId() {
		return id;
	}
}
