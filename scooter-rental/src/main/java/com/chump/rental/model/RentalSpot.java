package com.chump.rental.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

@Entity
@Table(name = "rent_spots")
@Getter
@Setter
@ToString(exclude = "parent")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RentalSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RentalSpot parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<RentalSpot> children;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "area", nullable = false, columnDefinition = "geography(Polygon, 4326)")
    private Polygon area;

    @Column(name = "is_zone", nullable = false)
    private Boolean isZone;
}
