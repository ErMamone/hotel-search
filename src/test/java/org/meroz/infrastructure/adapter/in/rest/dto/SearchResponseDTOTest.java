package org.meroz.infrastructure.adapter.in.rest.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;

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