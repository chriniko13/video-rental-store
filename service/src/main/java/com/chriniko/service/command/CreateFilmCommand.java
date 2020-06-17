package com.chriniko.service.command;

import com.chriniko.domain.FilmType;
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
public class CreateFilmCommand {

	private String name;
	private String type;

	public void validate() {

		if (Strings.isNullOrEmpty(name) || name.length() < 2) {
			throw new CommandValidationException("name is required");
		}

		try {
			FilmType.valueOf(type);
		} catch (IllegalArgumentException ex) {

			String correctValues = Arrays.stream(FilmType.values())
					.map(Enum::name)
					.collect(Collectors.joining(","));

			throw new CommandValidationException("type provided is not correct, correct values are: " + correctValues);
		}

	}
}
