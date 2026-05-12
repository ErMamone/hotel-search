package org.meroz.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SearchRequestDTO(

		@Schema(description = "ID alfanumerico del hotel", example = "RIU0001")
		@NotBlank
		@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "hotelId must be alphanumeric")
		String hotelId,

		@Schema(description = "Fecha de check-in", example = "19/05/2026", type = "string")
		@NotNull
		@FutureOrPresent(message = "checkIn must not be in the past")
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate checkIn,

		@Schema(description = "Fecha de check-out", example = "26/05/2026", type = "string")
		@NotNull
		@FutureOrPresent(message = "checkOut must not be in the past")
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate checkOut,

		@Schema(description = "Edades de los huespedes", example = "[30, 29, 1, 3]")
		@NotNull
		@Size(min = 1, message = "at least one age is required")
		List<@PositiveOrZero(message = "age must be zero or positive") Integer> ages
) {

	public SearchRequestDTO {
		ages = ages == null ? List.of() : List.copyOf(ages);
	}
}
