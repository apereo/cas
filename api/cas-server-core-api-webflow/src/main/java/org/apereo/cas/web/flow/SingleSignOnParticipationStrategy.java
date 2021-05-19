package org.apereo.cas.web.flow;

import org.apereo.cas.util.model.TriStateBoolean;

import org.springframework.core.Ordered;

/**
 * This is {@link SingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface SingleSignOnParticipationStrategy extends Ordered {

    /**
     * Always participating single sign on participation strategy.
     *
     * @return the single sign on participation strategy
     */
    static SingleSignOnParticipationStrategy alwaysParticipating() {
        return context -> true;
    }

    /**
     * Never participating single sign on participation strategy.
     *
     * @return the single sign on participation strategy
     */
    static SingleSignOnParticipationStrategy neverParticipating() {
        return context -> false;
    }

    /**
     * Tries to determine if this request should participate in SSO.
     * Services can opt out of SSO conditionally either per policy
     * or per request parameters. Various internal processes in CAS
     * also try to determine whether SSO should be honored for this request.
     *
     * @param ssoRequest the request
     * @return true if authn is renewed
     */
    boolean isParticipating(SingleSignOnParticipationRequest ssoRequest);

    /**
     * Does strategy support this request or not?
     *
     * @param ssoRequest the context
     * @return true/false
     */
    default boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.getRequestContext().isPresent() || ssoRequest.getHttpServletRequest().isPresent();
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Is creating single sign on session cookie on renewed authentication?
     *
     * @param context the context
     * @return true/false
     */
    default TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        return TriStateBoolean.UNDEFINED;
    }

    /**
     * Returns the friendly name of this strategy.
     *
     * @return the name.
     * @since 6.4.0
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
