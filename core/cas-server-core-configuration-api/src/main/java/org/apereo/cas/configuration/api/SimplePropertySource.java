package org.apereo.cas.configuration.api;

import module java.base;
import org.springframework.core.env.MapPropertySource;

/**
 * This is {@link SimplePropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SimplePropertySource extends MapPropertySource
    implements MutablePropertySource<Map<String, Object>> {

    public SimplePropertySource() {
        super(SimplePropertySource.class.getName() + UUID.randomUUID(),
            new ConcurrentHashMap<>());
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        getSource().put(name, value);
        return this;
    }

    @Override
    public void removeProperty(final String name) {
        getSource().remove(name);
    }

    @Override
    public void removeAll() {
        getSource().clear();
    }

}
