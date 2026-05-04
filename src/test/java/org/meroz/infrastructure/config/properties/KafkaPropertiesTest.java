package org.meroz.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaPropertiesTest {

	@Test
	void shouldExposeTopic() {
		var props = new KafkaProperties(new KafkaProperties.Topics("topic-x"));
		assertThat(props.topics().hotelAvailabilitySearches()).isEqualTo("topic-x");
	}
}