package org.meroz.infrastructure.adapter.out.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchRepositoryPort;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaSearchEventConsumerTest {

	private SearchRepositoryPort repository;
	private KafkaSearchEventConsumer consumer;

	@BeforeEach
	void setUp() {
		repository = mock(SearchRepositoryPort.class);
		consumer = new KafkaSearchEventConsumer(repository);
	}

	@Test
	void shouldDelegateToRepositoryOnMessage() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));

		consumer.onMessage(search);

		verify(repository).save(search);
	}
}