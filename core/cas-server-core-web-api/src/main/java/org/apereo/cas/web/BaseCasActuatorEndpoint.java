package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link BaseCasActuatorEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "CAS Actuator")
public abstract class BaseCasActuatorEndpoint {
    /**
     * Spring Boot v2 JSON media type.
     */
    protected static final String MEDIA_TYPE_SPRING_BOOT_V2_JSON = "application/vnd.spring-boot.actuator.v2+json";

    /**
     * Spring Boot v3 JSON media type.
     */
    protected static final String MEDIA_TYPE_SPRING_BOOT_V3_JSON = "application/vnd.spring-boot.actuator.v3+json";

    protected static final String MEDIA_TYPE_CAS_YAML = "application/vnd.cas.services+yaml";

    /**
     * The CAS properties.
     */
    protected final CasConfigurationProperties casProperties;
}
