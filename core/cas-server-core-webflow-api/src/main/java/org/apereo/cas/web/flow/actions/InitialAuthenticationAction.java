package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

/**
 * This is {@link InitialAuthenticationAction},
 * which serves as a placeholder for now to control
 * initial authn behavior, and to accommodate audit log events.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Monitorable
public class InitialAuthenticationAction extends AbstractAuthenticationAction {
    public InitialAuthenticationAction(final CasDelegatingWebflowEventResolver delegatingWebflowEventResolver,
                                       final CasWebflowEventResolver webflowEventResolver,
                                       final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(delegatingWebflowEventResolver, webflowEventResolver, adaptiveAuthenticationPolicy);
    }
}
