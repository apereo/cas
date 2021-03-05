package org.apereo.cas.audit.spi;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link AuditActionContextJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AuditActionContextJsonSerializer extends AbstractJacksonBackedStringSerializer<AuditActionContext> {
    private static final long serialVersionUID = -8983370764375218898L;

    @Override
    public Class<AuditActionContext> getTypeToSerialize() {
        return AuditActionContext.class;
    }

    @Override
    protected void configureObjectMapper(final ObjectMapper mapper) {
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        super.configureObjectMapper(mapper);
    }
}
