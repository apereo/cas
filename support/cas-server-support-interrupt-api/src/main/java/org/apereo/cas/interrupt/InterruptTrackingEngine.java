package org.apereo.cas.interrupt;

import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link InterruptTrackingEngine}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface InterruptTrackingEngine {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "interruptTrackingEngine";

    /**
     * Attribute recorded in authentication to indicate interrupt is finalized.
     */
    String AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT = "finalizedInterrupt";

    /**
     * Track interrupt.
     * <p>
     * An authentication attempt can only contain {@link #AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT}
     * if the attribute was added to the authentication object prior to creating the SSO session.
     * If interrupt checking is set to execute after SSO sessions, then this attribute cannot be retrieved.
     *
     * @param requestContext the request context
     * @param response       the response
     * @throws Throwable the throwable
     */
    void trackInterrupt(RequestContext requestContext, InterruptResponse response) throws Throwable;

    /**
     * Remove interrupt.
     *
     * @param requestContext the request context
     */
    void removeInterrupt(RequestContext requestContext);

    /**
     * Gets current interrupt response.
     *
     * @param requestContext the request context
     * @return the current interrupt response
     */
    Optional<InterruptResponse> forCurrentRequest(RequestContext requestContext);

    /**
     * Is authentication flow interrupted?
     * An authentication attempt can only contain {@link #AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT}
     * if the attribute was added to the authentication object prior to creating the SSO session.
     * If interrupt checking is set to execute after SSO sessions, then this attribute cannot be collected.
     *
     * @param requestContext the request context
     * @return true or false
     */
    boolean isInterrupted(RequestContext requestContext);
}
