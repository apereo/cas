package org.apereo.cas.web.flow.resolver;
import module java.base;

/**
 * This is {@link CasDelegatingWebflowEventResolver}
 * that delegates the actual task of event resolution
 * to a number of inner resolvers. Each resolver filters
 * the set of events, and passes it onto the next.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasDelegatingWebflowEventResolver extends CasWebflowEventResolver {

    /**
     * The bean name for the initial authentication attempt resolution.
     */
    String BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER = "initialAuthenticationAttemptWebflowEventResolver";
    /**
     * The bean name for the selective authentication event resolver.
     */
    String BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER = "selectiveAuthenticationProviderWebflowEventResolver";

    /**
     * Add delegate.
     *
     * @param r the resolver to delegate
     */
    void addDelegate(CasWebflowEventResolver r);

    /**
     * Add delegate given an index/position.
     *
     * @param r     the delegated resolver
     * @param index the index
     */
    void addDelegate(CasWebflowEventResolver r, int index);
}
