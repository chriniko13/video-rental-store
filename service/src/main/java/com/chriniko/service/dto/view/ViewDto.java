package com.chriniko.service.dto.view;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "view-type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = FilmViewDto.class, name = "film-view"),
		@JsonSubTypes.Type(value = FilmCopiesViewDto.class, name = "film-copies-view"),
		@JsonSubTypes.Type(value = CustomerViewDto.class, name = "customer-view"),
		@JsonSubTypes.Type(value = EntityIdViewDto.class, name = "entity-id-view"),
		@JsonSubTypes.Type(value = CompositeEntityIdViewDto.class, name = "composite-entity-id-view"),
		@JsonSubTypes.Type(value = FilmCopiesViewDto.FilmCopyViewDto.class, name = "film-copy-view"),
		@JsonSubTypes.Type(value = CustomerRentedFilmCopiesViewDto.class, name = "customer-rented-film-copies-view"),
		@JsonSubTypes.Type(value = CustomerRentedFilmCopyViewDto.class, name = "customer-rented-film-copy-view"),
		@JsonSubTypes.Type(value = CustomerBonusViewDto.class, name = "customer-bonus-view")

})
public interface ViewDto {

}
