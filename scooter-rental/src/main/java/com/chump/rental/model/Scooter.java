package com.chump.rental.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.chump.rental.model.status.ScooterStatus;
import org.locationtech.jts.geom.Point;

@SqlResultSetMapping(
        name = "ScooterWithModelMapping",
        entities = {
                @EntityResult(
                        entityClass = Scooter.class,
                        fields = {
                                @FieldResult(name = "id", column = "s_id"),
                                @FieldResult(name = "serialNumber", column = "s_serial_no"),
                                @FieldResult(name = "model", column = "m_id"), // Ссылка по FK
                                @FieldResult(name = "battery", column = "s_battery"),
                                @FieldResult(name = "location", column = "s_location"),
                                @FieldResult(name = "status", column = "s_status")
                        }
                ),
                @EntityResult(
                        entityClass = ScooterModel.class,
                        fields = {
                                @FieldResult(name = "id", column = "m_id"),
                                @FieldResult(name = "name", column = "m_name"),
                                @FieldResult(name = "vendor", column = "m_vendor")
                        }
                )
        }
)
@Entity
@Table(name = "scooters")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Scooter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_no", nullable = false)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id")
    private ScooterModel model;

    @Column(name = "battery", nullable = false)
    private Integer battery;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScooterStatus status;
}

