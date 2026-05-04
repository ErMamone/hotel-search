package org.meroz.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "hotel_search", indexes = @Index(name = "idx_search_id", columnList = "search_id"))
@Getter
@NoArgsConstructor
public class HotelSearchEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "search_id", nullable = false, length = 128)
	private String searchId;

	@Column(name = "hotel_id", nullable = false)
	private String hotelId;

	@Column(name = "check_in", nullable = false)
	private LocalDate checkIn;

	@Column(name = "check_out", nullable = false)
	private LocalDate checkOut;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "hotel_search_ages", joinColumns = @JoinColumn(name = "search_id_ref"))
	@OrderColumn(name = "age_order")
	@Column(name = "age")
	private List<Integer> ages;

	public HotelSearchEntity(String searchId, String hotelId, LocalDate checkIn, LocalDate checkOut, List<Integer> ages) {
		this.searchId = searchId;
		this.hotelId = hotelId;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.ages = List.copyOf(ages);
	}
}
