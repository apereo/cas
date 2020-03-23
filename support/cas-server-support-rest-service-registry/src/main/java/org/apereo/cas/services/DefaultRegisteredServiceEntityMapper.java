package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

/**
 * This is {@link DefaultRegisteredServiceEntityMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DefaultRegisteredServiceEntityMapper
    implements RegisteredServiceEntityMapper<RegisteredService, Serializable> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

    @Override
    public RegisteredService toRegisteredService(final Serializable object) {
        return MAPPER.convertValue(object, RegisteredService.class);
    }

    @Override
    public Serializable fromRegisteredService(final RegisteredService service) {
        return service;
    }
}
