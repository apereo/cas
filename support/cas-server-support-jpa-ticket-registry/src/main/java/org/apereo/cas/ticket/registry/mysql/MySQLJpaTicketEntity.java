package org.apereo.cas.ticket.registry.mysql;

import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link MySQLJpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@NoArgsConstructor
@AttributeOverrides(@AttributeOverride(name = "body", column = @Column(columnDefinition = "text")))
@Entity(name = "MySQLJpaTicketEntity")
@Table(name = "CasTickets")
public class MySQLJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 6546716187959834795L;
}
