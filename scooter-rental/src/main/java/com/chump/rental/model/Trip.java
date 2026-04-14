package com.chump.rental.model;

import jakarta.persistence.*;
import lombok.*;
import com.chump.rental.model.status.TripStatus;
import com.chump.user.model.User;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "scooter_id")
    private Scooter scooter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "discount_at_start", nullable = false)
    private BigDecimal discountAtStart;

    @Column(name = "distance")
    private Float distance;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
