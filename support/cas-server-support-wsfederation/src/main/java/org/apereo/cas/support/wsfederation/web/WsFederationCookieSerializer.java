package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

import java.util.Map;

/**
 * This is {@link WsFederationCookieSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WsFederationCookieSerializer extends AbstractJacksonBackedStringSerializer<Map> {
    private static final long serialVersionUID = -1152522695984638020L;

    public WsFederationCookieSerializer() {
        super(new MinimalPrettyPrinter());
    }

    @Override
    public Class<Map> getTypeToSerialize() {
        return Map.class;
    }
}
