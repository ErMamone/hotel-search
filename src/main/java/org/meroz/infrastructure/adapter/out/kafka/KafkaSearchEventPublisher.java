package org.meroz.infrastructure.adapter.out.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchEventPublisherPort;
import org.meroz.infrastructure.config.properties.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class KafkaSearchEventPublisher implements SearchEventPublisherPort {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String topic;

	public KafkaSearchEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
	                                 KafkaProperties properties) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = properties.topics().hotelAvailabilitySearches();
	}

	@Override
	public void publish(HotelSearch search) {
		try {
			kafkaTemplate.send(topic, search.searchId(), search).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventPublishException("Interrupted publishing search event", e);
		} catch (ExecutionException | TimeoutException e) {
			throw new EventPublishException("Failed to publish search event", e);
		}
	}
}