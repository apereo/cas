package org.apereo.cas.ticket.registry.mssql;

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
 * This is {@link MsSqlServerJpaTicketEntity}.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@AttributeOverrides(@AttributeOverride(name = "attributes",
    column = @Column(columnDefinition = "varchar(max) CHECK(ISJSON(attributes) = 1)")))
@Entity(name = "MsSqlServerJpaTicketEntity")
@Table(name = "CasTickets")
public class MsSqlServerJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 6546716187959834795L;
}
