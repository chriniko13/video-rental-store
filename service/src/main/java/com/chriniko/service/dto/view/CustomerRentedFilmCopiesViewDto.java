package com.chriniko.service.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRentedFilmCopiesViewDto implements ViewDto {

	private List<CustomerRentedFilmCopyViewDto> rentedResults;

}
