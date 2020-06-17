package com.chriniko.service.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmViewDto implements ViewDto {

	private long id;
	private String name;
	private String type;

}
