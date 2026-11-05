package com.truecarbon.trading.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "trading_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id",  nullable = false) private UUID   userId;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    @Column(nullable = false, length = 50)               private String type;

    @Column(name = "is_read", nullable = false) @Builder.Default private boolean read = false;
    @Column(name = "ref_id")                                       private UUID    refId;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private OffsetDateTime createdAt;
}
