package com.chump.billing.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "subscription_tariffs")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubscriptionTariff {

    // Берется tarrif.id автоматически из MapsId
    // id подгружается даже для прокси-пустышки
    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "tariff_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tariff tariff;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;
}
