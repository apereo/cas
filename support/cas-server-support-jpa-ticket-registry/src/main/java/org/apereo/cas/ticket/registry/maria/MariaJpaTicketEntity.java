package org.apereo.cas.ticket.registry.maria;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.*;
import java.util.List;
import java.util.Map;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.hibernate.annotations.Type;

/**
 * {@link BaseTicketEntity} for MariaDB which covers the fact that there is no native JSON datatype.
 *
 * @author Thomas Seliger
 * @since 7.0.0
 */
@SuperBuilder
@NoArgsConstructor
@AttributeOverrides(@AttributeOverride(name = "body", column = @Column(columnDefinition = "text")))
@Entity(name = "MySQLJpaTicketEntity")
@Table(name = "CasTickets")
@Setter
@Getter
@Accessors(chain = true)
public class MariaJpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 1325901961547418201L;

    @Type(JsonType.class)
    @Column(columnDefinition = "longtext CHECK (JSON_VALID(attributes))")
    private Map<String, List<Object>> attributes;
}
