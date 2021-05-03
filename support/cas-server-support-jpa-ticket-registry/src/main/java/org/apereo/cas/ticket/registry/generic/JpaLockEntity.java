package org.apereo.cas.ticket.registry.generic;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link JpaLockEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Entity
@Table(name = "locks")
@Getter
@Setter
@Embeddable
public class JpaLockEntity implements Serializable {

    private static final long serialVersionUID = -5750740484289616656L;

    @Version
    @Column(name = "lockVer", columnDefinition = "integer DEFAULT 0", nullable = false)
    private final Long version = 0L;

    /**
     * column name that holds application identifier.
     */
    @org.springframework.data.annotation.Id
    @Id
    @Column(name = "application_id")
    private String applicationId;

    /**
     * Database column name that holds unique identifier.
     */
    @Column(name = "unique_id")
    private String uniqueId;

    /**
     * Database column name that holds expiration date.
     */
    @Column(name = "expiration_date")
    private ZonedDateTime expirationDate;
}
