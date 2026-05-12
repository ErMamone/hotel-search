package org.meroz.infrastructure.adapter.out.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.infrastructure.config.properties.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaSearchEventPublisherTest {

	private KafkaTemplate<String, Object> template;
	private KafkaSearchEventPublisher publisher;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		template = (KafkaTemplate<String, Object>) mock(KafkaTemplate.class);
		var properties = new KafkaProperties(
				new KafkaProperties.Topics("hotel_availability_searches")
		);
		publisher = new KafkaSearchEventPublisher(template, properties);
	}

	@Test
	void shouldPublishSuccessfully() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));

		var producerRecord = new ProducerRecord<String, Object>("hotel_availability_searches", "id1", search);
		var metadata = new RecordMetadata(new TopicPartition("hotel_availability_searches", 0),
				0, 0, 0, 0, 0);
		var sendResult = new SendResult<>(producerRecord, metadata);
		when(template.send(anyString(), anyString(), any(HotelSearch.class)))
				.thenReturn(CompletableFuture.completedFuture(sendResult));

		publisher.publish(search);

		verify(template).send("hotel_availability_searches", "id1", search);
	}

	@Test
	void shouldWrapExecutionException() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));

		var failed = CompletableFuture.<SendResult<String, Object>>failedFuture(
				new ExecutionException("boom", new RuntimeException("network")));
		when(template.send(anyString(), anyString(), any(HotelSearch.class))).thenReturn(failed);

		assertThatThrownBy(() -> publisher.publish(search))
				.isInstanceOf(EventPublishException.class)
				.hasMessageContaining("Failed to publish");
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldWrapInterruptedException() throws Exception {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));

		var future = (CompletableFuture<SendResult<String, Object>>) mock(CompletableFuture.class);
		when(future.get(anyLong(), any(TimeUnit.class)))
				.thenThrow(new InterruptedException("interrupted"));
		when(template.send(anyString(), anyString(), any(HotelSearch.class))).thenReturn(future);

		assertThatThrownBy(() -> publisher.publish(search))
				.isInstanceOf(EventPublishException.class)
				.hasMessageContaining("Interrupted");

		assertThat(Thread.interrupted()).isTrue();
	}
}