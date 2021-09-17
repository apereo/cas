package org.apereo.cas.ticket.registry.mysql;

import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link MySQLJpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "body", column = @Column(columnDefinition = "text"))
})
@Entity(name = "MySQLJpaTicketEntity")
@Table(name = "CasTickets")
public class MySQLJpaTicketEntity extends BaseTicketEntity {
    private static final long serialVersionUID = 6546716187959834795L;
}
