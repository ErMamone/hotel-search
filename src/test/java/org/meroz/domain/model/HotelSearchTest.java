package org.meroz.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HotelSearchTest {

	@Test
	void shouldCreateHotelSearchWithImmutableAges() {
		var ages = new ArrayList<>(List.of(30, 29, 1));
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), ages);

		ages.add(99);

		assertThat(search.ages()).containsExactly(30, 29, 1);
	}

	@Test
	void agesListShouldBeUnmodifiable() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		var ages = search.ages();

		assertThatThrownBy(() -> ages.add(99))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}