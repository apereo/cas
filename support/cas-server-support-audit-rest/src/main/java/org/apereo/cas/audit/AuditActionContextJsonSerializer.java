package org.apereo.cas.audit;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link AuditActionContextJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AuditActionContextJsonSerializer extends AbstractJacksonBackedStringSerializer<AuditActionContext> {
    private static final long serialVersionUID = -8983370764375218898L;

    @Override
    protected Class<AuditActionContext> getTypeToSerialize() {
        return AuditActionContext.class;
    }
}
