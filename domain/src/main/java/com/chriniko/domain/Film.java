package com.chriniko.domain;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.domain.base.Record;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@EqualsAndHashCode(of = { "name" }, callSuper = false)
@ToString(of = { "id", "name", "filmType" })

@Entity

@Table(
		name = "films",
		uniqueConstraints = {
				@UniqueConstraint(name = "film_name_unique", columnNames = { "name" })
		}
)

@NamedQuery(name = "Film.findByName", query = "select f from Film f where f.name = :_name")

public class Film implements Record<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "film_Sequence")
	@SequenceGenerator(name = "film_Sequence", sequenceName = "FILM_SEQ")
	private Long id;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FilmType filmType;

	@OneToMany(
			mappedBy = "film",
			cascade = CascadeType.ALL,
			fetch = FetchType.LAZY,
			orphanRemoval = true
	)
	private Set<FilmCopy> copies = new LinkedHashSet<>();


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

	public static Film create(String name, FilmType type) {
		Film f = new Film();
		f.setName(name);
		f.setFilmType(type);
		return f;
	}

	public void addCopy(FilmCopy filmCopy) {
		this.copies.add(filmCopy);
		filmCopy.setFilm(this);
	}

	@Override public Long extractId() {
		return id;
	}
}
