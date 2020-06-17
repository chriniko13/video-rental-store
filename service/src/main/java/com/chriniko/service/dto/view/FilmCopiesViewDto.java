package com.chriniko.service.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmCopiesViewDto implements ViewDto {

	private Set<FilmCopyViewDto> filmCopies;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FilmCopyViewDto {

		private long id;
		private String reference;
		private String status;
	}
}
