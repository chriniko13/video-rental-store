package com.chriniko.service.dto.view;

import com.chriniko.domain.CustomerRentalHistoryFilmCopy;
import com.chriniko.domain.Film;
import com.chriniko.domain.FilmCopy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBonusViewDto implements ViewDto {

	private int totalBonuses;

	private List<CustomerBonusHistory> history;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CustomerBonusHistory {

		private long filmId;
		private String filmName;
		private String filmType;

		private long filmCopyId;
		private String filmCopyReference;
		private String filmCopyStatus;

		private Instant rented;
		private Instant returned;

		private BigDecimal cost;
		private Integer bonus;

		public static CustomerBonusViewDto.CustomerBonusHistory transform(CustomerRentalHistoryFilmCopy rH) {
			FilmCopy filmCopy = rH.getFilmCopy();
			Film film = filmCopy.getFilm();

			return new CustomerBonusViewDto.CustomerBonusHistory(
					film.getId(),
					film.getName(),
					film.getFilmType().name(),

					filmCopy.getId(),
					filmCopy.getReference(),
					filmCopy.getStatus().name(),

					rH.getRented(),
					rH.getReturned(),

					rH.getCost(),
					rH.getBonus()
			);
		}

	}
}
