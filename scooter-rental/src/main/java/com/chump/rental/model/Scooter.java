package com.chump.rental.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.chump.rental.model.status.ScooterStatus;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "scooters")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Scooter {

    @Id
    // TODO настроить batching через GenerationType.SEQUENCE + ручной increment в БД вместо SERIAL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_no", nullable = false)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id")
    private ScooterModel model;

    @Column(name = "battery", nullable = false)
    private Integer battery;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScooterStatus status;
}

