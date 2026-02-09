package org.apereo.cas.support.wsfederation.web;

import module java.base;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link WsFederationServerStateSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class WsFederationServerStateSerializer extends BaseJacksonSerializer<Map> {
    @Serial
    private static final long serialVersionUID = -1152522695984638020L;

    public WsFederationServerStateSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, Map.class);
    }
}
