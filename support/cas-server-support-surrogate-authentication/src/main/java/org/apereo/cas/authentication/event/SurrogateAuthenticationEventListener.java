package org.apereo.cas.authentication.event;

import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.spring.CasEventListener;

/**
 * Interface for {@code SurrogateAuthenticationEventListenerImpl} to allow spring {@code @Async} support to use JDK proxy.
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface SurrogateAuthenticationEventListener extends CasEventListener {

    /**
     * Handle failure event.
     *
     * @param event the event
     */
    void handleSurrogateAuthenticationFailureEvent(CasSurrogateAuthenticationFailureEvent event);

    /**
     * Handle success event.
     *
     * @param event the event
     */
    void handleSurrogateAuthenticationSuccessEvent(CasSurrogateAuthenticationSuccessfulEvent event);
}
