package org.meroz;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class HotelSearchApplicationTest {

	@Test
	void mainShouldStartSpringApplication() {
		try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
			HotelSearchApplication.main(new String[]{});
			mocked.verify(() -> SpringApplication.run(HotelSearchApplication.class, new String[]{}));
		}
	}
}
