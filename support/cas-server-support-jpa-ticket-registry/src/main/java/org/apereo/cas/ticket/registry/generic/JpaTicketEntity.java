package org.apereo.cas.ticket.registry.generic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link JpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Table(name = "CasTickets")
@Entity(name = "JpaTicketEntity")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class JpaTicketEntity implements Serializable {
    private static final long serialVersionUID = 6534421912995436609L;

    @Column(nullable = false, length = 12_000)
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

    @Column(nullable = false, length= 512)
    private ZonedDateTime creationTime;
}
