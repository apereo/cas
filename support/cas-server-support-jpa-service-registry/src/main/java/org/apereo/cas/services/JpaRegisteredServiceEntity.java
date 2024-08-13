package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Accessors(chain = true)
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
    private long id;

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
