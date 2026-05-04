package org.meroz.domain.service;

import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.in.CreateSearchCommand;
import org.meroz.domain.port.in.CreateSearchUseCase;
import org.meroz.domain.port.out.SearchEventPublisherPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;

public class CreateSearchService implements CreateSearchUseCase {

	private static final String HASH_ALGORITHM = "SHA-256";
	private static final String FIELD_SEPARATOR = "|";
	private static final int SEARCH_ID_LENGTH = 32;

	private final SearchEventPublisherPort publisher;

	public CreateSearchService(SearchEventPublisherPort publisher) {
		this.publisher = publisher;
	}

	@Override
	public HotelSearch createSearch(CreateSearchCommand command) {
		validate(command);

		String searchId = generateSearchId(command);

		HotelSearch search = new HotelSearch(
				searchId,
				command.hotelId(),
				command.checkIn(),
				command.checkOut(),
				command.ages()
		);

		publisher.publish(search);
		return search;
	}

	private void validate(CreateSearchCommand command) {
		if (!command.checkIn().isBefore(command.checkOut())) {
			throw new InvalidDateRangeException("checkIn must be before checkOut");
		}
	}

	private String generateSearchId(CreateSearchCommand command) {
		String fingerprint = String.join(FIELD_SEPARATOR,
				command.hotelId(),
				command.checkIn().toString(),
				command.checkOut().toString(),
				command.ages().stream()
						.map(String::valueOf)
						.collect(Collectors.joining(","))
		);

		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash).substring(0, SEARCH_ID_LENGTH);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}