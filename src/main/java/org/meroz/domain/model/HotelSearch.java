package org.meroz.domain.model;

import java.time.LocalDate;
import java.util.List;

public record HotelSearch(
		String searchId,
		String hotelId,
		LocalDate checkIn,
		LocalDate checkOut,
		List<Integer> ages
) {
	public HotelSearch {
		ages = List.copyOf(ages);
	}
}