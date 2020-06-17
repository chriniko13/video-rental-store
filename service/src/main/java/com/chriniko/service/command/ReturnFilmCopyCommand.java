package com.chriniko.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnFilmCopyCommand {

	private long customerId;
	private long filmCopyId;

}
