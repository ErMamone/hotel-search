package org.meroz.infrastructure.config;

import org.meroz.domain.port.in.CountSearchesUseCase;
import org.meroz.domain.port.in.CreateSearchUseCase;
import org.meroz.domain.port.out.SearchEventPublisherPort;
import org.meroz.domain.port.out.SearchRepositoryPort;
import org.meroz.domain.service.CountSearchesService;
import org.meroz.domain.service.CreateSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

	@Bean
	public CreateSearchUseCase createSearchUseCase(SearchEventPublisherPort publisher) {
		return new CreateSearchService(publisher);
	}

	@Bean
	public CountSearchesUseCase countSearchesUseCase(SearchRepositoryPort repository) {
		return new CountSearchesService(repository);
	}
}