package com.crm.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Idempotency guard for inbound events: a row means the event has already been
 * processed and must not be handled again.
 *
 * <p>The primary key is the id carried by the inbound event itself — it is
 * client-assigned, never generated here.
 */
@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    /** Id of the inbound event (assigned by the producer, not generated). */
    @Id
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
