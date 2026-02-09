package org.apereo.cas.ticket.registry.mysql;

import module java.base;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

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
@Table(name = "CasTickets", indexes = {
    @Index(name = "idx_ticket_type", columnList = "type"),
    @Index(name = "idx_ticket_principal", columnList = "principalId"),
    @Index(name = "idx_ticket_parent", columnList = "parentId"),
    @Index(name = "idx_ticket_service", columnList = "service"),
    @Index(name = "idx_type_principal", columnList = "type,principalId")
})
@Setter
@Getter
@Accessors(chain = true)
public class MySQLJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 6546716187959834795L;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, List<Object>> attributes;
}
