package org.apereo.cas.ticket.registry.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * This is {@link BaseTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@MappedSuperclass
@ToString
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public abstract class BaseTicketEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 6534421912995436609L;

    @Column(nullable = false, length = 32_000)
    private String body;

    @Column(nullable = false, length = 768)
    @Id
    private String id;

    @Column(length = 1024)
    @Getter
    @Setter
    private String parentId;

    @Column(length = 1024)
    private String principalId;

    @Column(nullable = false, length = 1024)
    private String type;

    @Column(nullable = false, length = 512)
    private ZonedDateTime creationTime;

    /**
     * Sets attributes.
     *
     * @param attributes the attributes
     * @return the attributes
     */
    public abstract BaseTicketEntity setAttributes(Map<String, List<Object>> attributes);
}
