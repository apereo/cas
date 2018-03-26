package org.apereo.cas.config;

import org.apereo.cas.support.jpa.DefaultJpaStreamerFactory;
import org.apereo.cas.support.jpa.JpaStreamer;

/**
 * Configures factories for creating objects to handle turning a JPA query into a stream of objects. This component provides a way to inject stateless factories
 * into components that produce stateful JpaStreamer instances to decouple the JPA implementation from core so other JPA implementations may be used without
 * adding the dependency to core.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@FunctionalInterface
public interface JpaStreamerFactoryConfigurer {
    /**
     * Callback for registering a {@link JpaStreamer}.
     * @param factory Streamer factory with which to register a streamer.
     */
    void configureDefaultJpaStreamerFactory(DefaultJpaStreamerFactory factory);
}
