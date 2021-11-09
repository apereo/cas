package org.apereo.cas.ticket.registry.generic;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link JpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@Entity(name = "JpaTicketEntity")
@Table(name = "CasTickets")
public class JpaTicketEntity extends BaseTicketEntity {
}
