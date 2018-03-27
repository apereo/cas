package org.apereo.cas.web.flow.resolver;

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
