package org.meroz.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.meroz.infrastructure.config.properties.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

	@Bean
	public NewTopic hotelAvailabilitySearchesTopic(KafkaProperties properties) {
		return new NewTopic(properties.topics().hotelAvailabilitySearches(), 3, (short) 1);
	}

	@Bean
	public DefaultErrorHandler kafkaErrorHandler() {
		return new DefaultErrorHandler(new FixedBackOff(1000L, 2L));
	}
}