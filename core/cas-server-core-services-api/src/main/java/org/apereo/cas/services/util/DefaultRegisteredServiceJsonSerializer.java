package org.apereo.cas.services.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultRegisteredServiceJsonSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisteredServiceJsonSerializer.class);

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

    @Override
    public boolean supports(final File file) {
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()).contains(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
        } catch (final Exception e) {
            return false;
        }
    }
}
