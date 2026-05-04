package org.meroz.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record CountResponseDTO (
		String searchId,
		SearchPayload search,
		long count
) {
	public record SearchPayload(
			String hotelId,
			@JsonFormat(pattern = "dd/MM/yyyy") LocalDate checkIn,
			@JsonFormat(pattern = "dd/MM/yyyy") LocalDate checkOut,
			List<Integer> ages
	) {
		public SearchPayload {
			ages = List.copyOf(ages);
		}
	}
}
