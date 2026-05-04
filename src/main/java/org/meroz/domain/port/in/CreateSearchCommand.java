package org.meroz.domain.port.in;

import java.time.LocalDate;
import java.util.List;

public record CreateSearchCommand(
		String hotelId,
		LocalDate checkIn,
		LocalDate checkOut,
		List<Integer> ages
) {
	public CreateSearchCommand {
		ages = List.copyOf(ages);
	}
}
