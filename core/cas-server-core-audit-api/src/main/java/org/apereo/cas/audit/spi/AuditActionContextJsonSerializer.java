package org.apereo.cas.audit.spi;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

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

}
