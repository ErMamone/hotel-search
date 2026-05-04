package org.meroz.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.meroz.domain.model.HotelSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HotelSearchPersistenceAdapter.class)
@Testcontainers
class HotelSearchPersistenceAdapterTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private HotelSearchPersistenceAdapter adapter;

	@Test
	void shouldPersistAndRetrieveByeSearchId() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29, 1));

		adapter.save(search);

		var found = adapter.findFirstBySearchId("id1");
		assertThat(found).isPresent();
		assertThat(found.get().hotelId()).isEqualTo("hotelA");
		assertThat(found.get().ages()).containsExactly(30, 29, 1);
	}

	@Test
	void shouldPreserveAgesOrder() {
		var search = new HotelSearch("id-order", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(3, 1, 2, 29, 30));
		adapter.save(search);

		var retrieved = adapter.findFirstBySearchId("id-order").orElseThrow();
		assertThat(retrieved.ages()).containsExactly(3, 1, 2, 29, 30);
	}

	@Test
	void shouldReturnEmptyWhenSearchIdNotFound() {
		assertThat(adapter.findFirstBySearchId("missing")).isEmpty();
	}

	@Test
	void shouldCountMultipleEntriesWithSameSearchId() {
		var search = new HotelSearch("dup", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		adapter.save(search);
		adapter.save(search);
		adapter.save(search);

		assertThat(adapter.countBySearchId("dup")).isEqualTo(3L);
	}

	@Test
	void shouldReturnZeroWhenNoMatches() {
		assertThat(adapter.countBySearchId("ghost")).isZero();
	}
}