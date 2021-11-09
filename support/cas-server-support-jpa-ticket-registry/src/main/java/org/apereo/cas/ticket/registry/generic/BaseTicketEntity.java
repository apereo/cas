package org.apereo.cas.ticket.registry.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link BaseTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@MappedSuperclass
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class BaseTicketEntity implements Serializable {
    private static final long serialVersionUID = 6534421912995436609L;

    @Column(nullable = false, length = 32_000)
    private String body;

    @Column(nullable = false, length = 768)
    @Id
    private String id;

    @Column(length = 1024)
    private String parentId;

    @Column(length = 1024)
    private String principalId;

    @Column(nullable = false, length = 1024)
    private String type;

    @Column(nullable = false, length = 512)
    private ZonedDateTime creationTime;
}
