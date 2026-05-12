package org.meroz.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class HotelSearchEntityTest {

	@Test
	void shouldCreateEntityWithAllFields() {
		var entity = new HotelSearchEntity("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));

		assertAll(
				() -> assertThat(entity.getSearchId()).isEqualTo("id1"),
				() -> assertThat(entity.getHotelId()).isEqualTo("hotelA"),
				() -> assertThat(entity.getCheckIn()).isEqualTo(LocalDate.of(2025, 1, 1)),
				() -> assertThat(entity.getCheckOut()).isEqualTo(LocalDate.of(2025, 1, 5)),
				() -> assertThat(entity.getAges()).containsExactly(30, 29)
		);
	}

	@Test
	void noArgsConstructorShouldExistForJpa() {
		var entity = new HotelSearchEntity();
		assertThat(entity.getId()).isNull();
	}
}
