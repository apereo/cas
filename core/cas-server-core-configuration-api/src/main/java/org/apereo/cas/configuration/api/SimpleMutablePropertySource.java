package org.apereo.cas.configuration.api;

import module java.base;
import org.springframework.core.env.MapPropertySource;

/**
 * This is {@link SimpleMutablePropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SimpleMutablePropertySource extends MapPropertySource
    implements MutablePropertySource<Map<String, Object>> {

    public SimpleMutablePropertySource() {
        super(SimpleMutablePropertySource.class.getName() + UUID.randomUUID(),
            new ConcurrentHashMap<>());
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        getSource().put(name, value);
        return this;
    }
}
