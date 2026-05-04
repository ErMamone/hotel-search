package org.meroz.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI hotelSearchOpenAPI() {
		return new OpenAPI().info(new Info()
				.title("Hotel Search API")
				.description("Hotel availability search service")
				.version("1.0.0"));
	}
}