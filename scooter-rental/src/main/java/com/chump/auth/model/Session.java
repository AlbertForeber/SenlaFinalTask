package com.chump.auth.model;

import com.chump.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_token", referencedColumnName = "refresh_token")
    private Session replacedByToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "ip_address", nullable = false, length = 15)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder.Default
    @Column(name = "terminated", nullable = false)
    private Boolean terminated = false;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isNotValid() {
        return isExpired() || terminated || replacedByToken != null;
    }
}
