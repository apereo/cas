package org.apereo.cas.ticket.registry.oracle;

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
import jakarta.persistence.Table;


/**
 * This is {@link OracleJpaTicketEntity}.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@Entity(name = "OracleJpaTicketEntity")
@Table(name = "CasTickets")
@Setter
@Getter
@Accessors(chain = true)
public class OracleJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 6546716187959834795L;

    @Type(JsonType.class)
    @Column(columnDefinition = "varchar2(4000 char)")
    private Map<String, List<Object>> attributes;
}
