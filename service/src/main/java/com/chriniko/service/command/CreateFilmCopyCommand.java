package com.chriniko.service.command;

import com.chriniko.domain.FilmCopyStatus;
import com.chriniko.service.error.CommandValidationException;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilmCopyCommand {

	private long filmId;
	private String copyStatus;

	public void validate() {
		if (Strings.isNullOrEmpty(copyStatus)) {
			throw new CommandValidationException("copyStatus is required");
		}

		try {
			FilmCopyStatus.valueOf(copyStatus);
		} catch (IllegalArgumentException ex) {

			String correctValues = Arrays.stream(FilmCopyStatus.values())
					.map(Enum::name)
					.collect(Collectors.joining(","));

			throw new CommandValidationException("copyStatus provided is not correct, correct values are: " + correctValues);
		}
	}
}
