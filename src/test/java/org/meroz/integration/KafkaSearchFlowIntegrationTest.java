package org.meroz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.meroz.domain.port.out.SearchRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class KafkaSearchFlowIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Container
	@ServiceConnection
	static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
			DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SearchRepositoryPort repository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldPublishToKafkaConsumeAndPersist() throws Exception {
		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of(30, 29, 1)
		);

		var response = mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.searchId").exists())
				.andReturn();

		var searchId = objectMapper.readTree(response.getResponse().getContentAsString())
				.get("searchId").asText();

		await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
				assertThat(repository.countBySearchId(searchId)).isEqualTo(1L)
		);

		mockMvc.perform(get("/count").param("searchId", searchId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.searchId").value(searchId))
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.search.hotelId").value("hotelA"))
				.andExpect(jsonPath("$.search.ages[0]").value(30));
	}

	@Test
	void multipleEqualSearchesShouldIncrementCount() throws Exception {
		var body = Map.of(
				"hotelId", "hotelB",
				"checkIn", "10/06/2025",
				"checkOut", "15/06/2025",
				"ages", List.of(40, 8)
		);
		var json = objectMapper.writeValueAsString(body);

		String searchId = null;
		for (int i = 0; i < 3; i++) {
			var response = mockMvc.perform(post("/search")
							.contentType(MediaType.APPLICATION_JSON)
							.content(json))
					.andExpect(status().isOk())
					.andReturn();
			searchId = objectMapper.readTree(response.getResponse().getContentAsString())
					.get("searchId").asText();
		}

		final String finalSearchId = searchId;
		await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
				assertThat(repository.countBySearchId(finalSearchId)).isEqualTo(3L)
		);
	}
}