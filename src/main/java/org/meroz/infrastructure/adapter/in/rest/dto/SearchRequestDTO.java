package org.meroz.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SearchRequestDTO (

		@NotBlank
		@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "hotelId must be alphanumeric")
		String hotelId,

		@NotNull
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate checkIn,

		@NotNull
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate checkOut,

		@NotNull
		@Size(min = 1, message = "at least one age is required")
		List<Integer> ages
) {

	public SearchRequestDTO{
		ages = ages == null ? List.of() : List.copyOf(ages);
	}
}
