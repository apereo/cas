package org.apereo.cas.ticket.registry.mssql;

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
import jakarta.persistence.Table;

import java.io.Serial;
import java.util.List;
import java.util.Map;


/**
 * This is {@link MsSqlServerJpaTicketEntity}.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@Entity(name = "MsSqlServerJpaTicketEntity")
@Table(name = "CasTickets")
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
