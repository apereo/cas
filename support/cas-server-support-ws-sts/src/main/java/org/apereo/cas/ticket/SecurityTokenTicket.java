package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link SecurityTokenTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Table(name = "SECURITYTOKENTICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(SecurityTokenTicket.PREFIX)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface SecurityTokenTicket extends Ticket {
    /**
     * Ticket Prefix.
     */
    String PREFIX = "STS";
}
