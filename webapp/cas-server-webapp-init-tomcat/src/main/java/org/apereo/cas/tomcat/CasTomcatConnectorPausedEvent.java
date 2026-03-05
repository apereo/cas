package org.apereo.cas.tomcat;

import module java.base;
import org.apache.catalina.connector.Connector;

/**
 * This is {@link CasTomcatConnectorPausedEvent}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record CasTomcatConnectorPausedEvent(Connector connector) {
}
