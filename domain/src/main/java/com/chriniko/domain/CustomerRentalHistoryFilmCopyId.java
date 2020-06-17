package com.chriniko.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@EqualsAndHashCode
@ToString

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter

@Embeddable
public class CustomerRentalHistoryFilmCopyId implements Serializable {

	@Column(name = "customer_id")
	private Long customerId;

	@Column(name = "film_copy_id")
	private Long filmCopyId;

}
