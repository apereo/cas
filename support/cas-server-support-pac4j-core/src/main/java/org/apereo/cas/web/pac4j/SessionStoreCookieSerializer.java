package org.apereo.cas.web.pac4j;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import java.util.Map;

/**
 * This is {@link SessionStoreCookieSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SessionStoreCookieSerializer extends AbstractJacksonBackedStringSerializer<Map<String, Object>> {
    private static final long serialVersionUID = -1152522695984638020L;


    public SessionStoreCookieSerializer(final Module... additionalModules) {
        super(new MinimalPrettyPrinter());
        for (Module module: additionalModules) {
            getObjectMapper().registerModule(module);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Map<String, Object>> getTypeToSerialize() {
        return (Class<Map<String,Object>>)(Class<?>) Map.class;
    }
}
