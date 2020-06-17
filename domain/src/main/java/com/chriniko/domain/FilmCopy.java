package com.chriniko.domain;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.domain.base.Record;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@EqualsAndHashCode(of = { "reference" }, callSuper = false)
@ToString(of = { "id", "reference" })

@Entity

@Table(
		name = "film_copies",
		uniqueConstraints = {
				@UniqueConstraint(name = "copy_reference_unique", columnNames = { "reference" })
		}
)
public class FilmCopy implements Record<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filmCopy_Sequence")
	@SequenceGenerator(name = "filmCopy_Sequence", sequenceName = "FILM_COPY_SEQ")
	private Long id;

	@Column(nullable = false, length = 100)
	private String reference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FilmCopyStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	private Film film;

	@OneToMany(
			mappedBy = "filmCopy",
			fetch = FetchType.LAZY
	)
	private Set<CustomerRentalHistoryFilmCopy> rentalHistories = new LinkedHashSet<>();

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

	public static FilmCopy create(String reference, FilmCopyStatus status) {
		FilmCopy fc = new FilmCopy();
		fc.setReference(reference);
		fc.setStatus(status);
		return fc;
	}

	@Override public Long extractId() {
		return id;
	}


}
