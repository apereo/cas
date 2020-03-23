package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@NoArgsConstructor
public class RegisteredServiceJsonSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {

    private static final long serialVersionUID = 7645698151115635245L;

    public RegisteredServiceJsonSerializer(final PrettyPrinter prettyPrinter) {
        super(prettyPrinter);
    }

    @Override
    protected ObjectMapper initializeObjectMapper() {
        val mapper = super.initializeObjectMapper();
        mapper.addHandler(new JasigRegisteredServiceDeserializationProblemHandler());
        mapper.addHandler(new RegisteredServiceMultifactorPolicyDeserializationProblemHandler());
        return mapper;
    }

    @Override
    public boolean supports(final File file) {
        try {
            val content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
            return supports(content);
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean supports(final String content) {
        return content.contains(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
    }

    @Override
    public Class<RegisteredService> getTypeToSerialize() {
        return RegisteredService.class;
    }
}
