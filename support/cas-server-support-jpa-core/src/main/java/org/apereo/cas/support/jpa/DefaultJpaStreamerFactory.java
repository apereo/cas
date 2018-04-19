package org.apereo.cas.support.jpa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to abstract the creation of streams from JPA queries to decouple core from any specific JPA implementation.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
public class DefaultJpaStreamerFactory {
    private final Map<AbstractJpaProperties.JpaType, JpaStreamer> jpaStreamerMap;

    public DefaultJpaStreamerFactory() {
        jpaStreamerMap = new ConcurrentHashMap<>();
    }

    /**
     * Register a {@link JpaStreamer}.
     * @param jpaType jpa type for the streamer.
     * @param jpaStreamer {@link JpaStreamer} to register.
     */
    public void registerJpaStreamer(final AbstractJpaProperties.JpaType jpaType, final JpaStreamer jpaStreamer) {
        jpaStreamerMap.put(jpaType, jpaStreamer);
    }

    /**
     * Obtains the JPA streamer for a given type.
     * @param jpaType Type for which to obtain the streamer.
     * @return JPA streamer for type.
     */
    public JpaStreamer getStreamerForType(final AbstractJpaProperties.JpaType jpaType) {
        return jpaStreamerMap.get(jpaType);
    }

}
