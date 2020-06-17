package com.chriniko.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCustomerBonusCommand {

	private long customerId;
	private boolean detailed;

}
