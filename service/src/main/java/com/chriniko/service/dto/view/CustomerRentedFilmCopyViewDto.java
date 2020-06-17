package com.chriniko.service.dto.view;

import com.chriniko.domain.CustomerRentalHistoryFilmCopy;
import com.chriniko.domain.Film;
import com.chriniko.domain.FilmCopy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRentedFilmCopyViewDto implements ViewDto {

	private Instant rented;
	private Instant returned;

	private BigDecimal cost;

	private Integer bonus;

	private long filmCopyId;
	private String filmCopyReference;
	private String filmCopyStatus;

	private long filmId;
	private String filmName;
	private String filmType;

	public static CustomerRentedFilmCopyViewDto transform(CustomerRentalHistoryFilmCopy r) {
		FilmCopy filmCopy = r.getFilmCopy();
		Film film = filmCopy.getFilm();

		return new CustomerRentedFilmCopyViewDto(
				r.getRented(),
				r.getReturned(),

				r.getCost(),

				r.getBonus(),

				filmCopy.getId(),
				filmCopy.getReference(),
				filmCopy.getStatus().name(),

				film.getId(),
				film.getName(),
				film.getFilmType().name()
		);
	}

}
