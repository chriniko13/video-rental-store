package com.chriniko.service.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerViewDto implements ViewDto {

	private long id;
	private String username;
	private String firstname;
	private String initials;
	private String surname;

}
