package org.apereo.cas.ticket.registry.generic;

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
 * This is {@link JpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@NoArgsConstructor
@Entity(name = "JpaTicketEntity")
@Table(name = "CasTickets")
@Setter
@Getter
@Accessors(chain = true)
public class JpaTicketEntity extends BaseTicketEntity {
    @Serial
    private static final long serialVersionUID = 4589814295318995496L;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, List<Object>> attributes;
}
