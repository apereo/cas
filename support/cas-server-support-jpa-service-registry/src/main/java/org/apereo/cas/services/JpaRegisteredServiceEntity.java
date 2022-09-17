package org.apereo.cas.services;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link JpaRegisteredServiceEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Table(name = "RegisteredServices")
@Entity(name = JpaRegisteredServiceEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaRegisteredServiceEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaRegisteredServiceEntity";

    @Serial
    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "service_sequence")
    @SequenceGenerator(name = "service_sequence", allocationSize = 100)
    @Builder.Default
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int evaluationOrder;

    @Column(nullable = false)
    private int evaluationPriority;

    @Column(nullable = false, length = 8_000)
    private String body;

}
