package org.apereo.cas.ticket.registry.mysql;

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
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.List;
import java.util.Map;

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
