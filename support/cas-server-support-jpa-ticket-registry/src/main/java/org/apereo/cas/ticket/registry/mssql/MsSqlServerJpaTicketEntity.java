package org.apereo.cas.ticket.registry.mssql;

import module java.base;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;


/**
 * This is {@link MsSqlServerJpaTicketEntity}.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@Entity(name = "MsSqlServerJpaTicketEntity")
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
public class MsSqlServerJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 6546716187959834795L;

    @Type(JsonType.class)
    @Column(columnDefinition = "varchar(max) CHECK(ISJSON(attributes) = 1)")
    private Map<String, List<Object>> attributes;
}
