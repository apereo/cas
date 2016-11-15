package org.apereo.cas.util.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceJsonSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {
    private static final long serialVersionUID = 7645698151115635245L;

    /**
     * Mixins are added to the object mapper in order to
     * ignore certain method signatures from serialization
     * that are otherwise treated as getters. Each mixin
     * implements the appropriate interface as a private
     * dummy class and is annotated with JsonIgnore elements
     * throughout. This helps us catch errors at compile-time
     * when the interface changes.
     *
     * @return the prepped object mapper.
     */
    @Override
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = super.initializeObjectMapper();
        mapper.addHandler(new JasigRegisteredServiceDeserializationProblemHandler());
        return mapper;
    }


    @Override
    protected Class<RegisteredService> getTypeToSerialize() {
        return RegisteredService.class;
    }
}
