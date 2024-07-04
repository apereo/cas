package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link ProxyTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ProxyTicketStringSerializer extends AbstractJacksonBackedStringSerializer<ProxyTicketImpl> {
    @Serial
    private static final long serialVersionUID = -6343596853082798477L;

    public ProxyTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext);
    }

    @Override
    public Class<ProxyTicketImpl> getTypeToSerialize() {
        return ProxyTicketImpl.class;
    }
}
