package org.meroz.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(Topics topics) {

	public record Topics(String hotelAvailabilitySearches) {
	}

}