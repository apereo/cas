package org.apereo.cas.ticket.registry.generic;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serial;

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
    @Serial
    private static final long serialVersionUID = 4589814295318995496L;
}
