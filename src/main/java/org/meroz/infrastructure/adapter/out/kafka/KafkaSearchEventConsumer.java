package org.meroz.infrastructure.adapter.out.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchRepositoryPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaSearchEventConsumer {

	private final SearchRepositoryPort repository;

	@KafkaListener(
			topics = "${app.kafka.topics.hotel-availability-searches}",
			groupId = "${app.kafka.consumer.group-id}"
	)
	public void onMessage(HotelSearch search) {
		log.debug("Received search event: searchId={}", search.searchId());
		repository.save(search);
	}

}
