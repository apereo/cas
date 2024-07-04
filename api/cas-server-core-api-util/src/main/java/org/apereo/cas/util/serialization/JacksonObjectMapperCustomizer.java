package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link JacksonObjectMapperCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface JacksonObjectMapperCustomizer extends Ordered {
    
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

    /**
     * Mapped injectable values.
     *
     * @param injectables the injectables
     * @return the jackson object mapper customizer
     */
    static JacksonObjectMapperCustomizer mappedInjectableValues(final Map<String, ?> injectables) {
        return new JacksonObjectMapperCustomizer() {
            @Override
            public Map<String, ?> getInjectableValues() {
                return injectables;
            }
        };
    }
}
