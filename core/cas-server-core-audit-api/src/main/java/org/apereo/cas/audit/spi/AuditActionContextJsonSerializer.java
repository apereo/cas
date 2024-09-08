package org.apereo.cas.audit.spi;

import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link AuditActionContextJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AuditActionContextJsonSerializer extends BaseJacksonSerializer<AuditActionContext> {
    @Serial
    private static final long serialVersionUID = -8983370764375218898L;

    public AuditActionContextJsonSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, AuditActionContext.class);
    }
}
