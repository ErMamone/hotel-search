package org.meroz.infrastructure.adapter.in.rest.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SearchResponseDTOTest {

	@Test
	void shouldExposeSearchId() {
		var dto = new SearchResponseDTO("abc123");

		assertAll(
				() -> assertThat(dto.searchId()).isEqualTo("abc123"),
				() -> assertThat(dto).isEqualTo(new SearchResponseDTO("abc123")),
				() -> assertThat(dto.hashCode()).hasSameHashCodeAs(new SearchResponseDTO("abc123").hashCode())
		);
	}
}
