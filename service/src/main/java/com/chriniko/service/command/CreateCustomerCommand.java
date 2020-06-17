package com.chriniko.service.command;

import com.chriniko.service.error.CommandValidationException;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerCommand {

	private String username;
	private String firstname;
	private String initials;
	private String surname;

	public void validate() {

		String[][] info = new String[][] {
				{ "username", username },
				{ "firstname", firstname },
				{ "surname", surname }
		};

		for (String[] rec : info) {

			String label = rec[0];
			String value = rec[1];

			if (Strings.isNullOrEmpty(value) || value.length() < 5) {
				throw new CommandValidationException(label + " is required");
			}
		}

	}
}
