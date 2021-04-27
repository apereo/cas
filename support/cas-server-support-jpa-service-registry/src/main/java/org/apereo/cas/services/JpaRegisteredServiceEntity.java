package org.apereo.cas.services;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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

    private static final long serialVersionUID = 6534421912995436609L;

    private static StringSerializer<RegisteredService> SERIALIZER =
        new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter());

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

    /**
     * From registered service.
     *
     * @param service the service
     * @return the jpa registered service entity
     */
    public static JpaRegisteredServiceEntity fromRegisteredService(final RegisteredService service) {
        val jsonBody = SERIALIZER.toString(service);
        return JpaRegisteredServiceEntity.builder()
            .id(service.getId())
            .name(service.getName())
            .serviceId(service.getServiceId())
            .evaluationOrder(service.getEvaluationOrder())
            .body(jsonBody)
            .build();
    }

    /**
     * To registered service.
     *
     * @return the registered service
     */
    public RegisteredService toRegisteredService() {
        val service = SERIALIZER.from(this.body);
        service.setId(this.id);
        LOGGER.trace("Converted JPA entity [{}] to [{}]", this, service);
        return service;
    }
}
