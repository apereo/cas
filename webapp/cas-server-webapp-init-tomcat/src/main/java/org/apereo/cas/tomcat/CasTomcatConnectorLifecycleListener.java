package org.apereo.cas.tomcat;

import module java.base;
import org.apereo.cas.util.spring.CasApplicationReadyListener;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link CasTomcatConnectorLifecycleListener}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SuppressWarnings("NullAway.Init")
public interface CasTomcatConnectorLifecycleListener extends CasApplicationReadyListener {
    /**
     * Handle connector paused event.
     *
     * @param tcp the tcp
     */
    @EventListener
    @Async
    void handleConnectorPausedEvent(CasTomcatConnectorPausedEvent tcp);

    /**
     * Resume connector on ready cas tomcat connector lifecycle listener.
     *
     * @return the cas tomcat connector lifecycle listener
     */
    static CasTomcatConnectorLifecycleListener resumeConnectorOnReady() {
        return new CasTomcatConnectorLifecycleListener() {
            private volatile Connector connector;

            @Override
            public void handleConnectorPausedEvent(final CasTomcatConnectorPausedEvent tcp) {
                this.connector = tcp.connector();
            }

            @Override
            public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
                if (connector != null) {
                    connector.resume();
                }
            }
        };
    }
}
