package com.chriniko.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindCustomerByUsernameCommand {

	private String username;

	private boolean approximatelySearch = false;
}
