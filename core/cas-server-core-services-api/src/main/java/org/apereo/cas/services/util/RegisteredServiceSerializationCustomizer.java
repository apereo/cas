package org.apereo.cas.services.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RegisteredServiceSerializationCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface RegisteredServiceSerializationCustomizer extends Ordered {

    /**
     * No-Op registered service serialization customizer.
     *
     * @return the registered service serialization customizer
     */
    static RegisteredServiceSerializationCustomizer noOp() {
        return new RegisteredServiceSerializationCustomizer() {
        };
    }

    /**
     * Get injectable values from this customer to ultimately
     * collect and stuff into the mapper itself.
     *
     * @return the map
     */
    default Map<String, ?> getInjectableValues() {
        return new HashMap<>();
    }

    /**
     * Customize the mapper.
     *
     * @param objectMapper the object mapper
     */
    default void customize(final ObjectMapper objectMapper) {
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
