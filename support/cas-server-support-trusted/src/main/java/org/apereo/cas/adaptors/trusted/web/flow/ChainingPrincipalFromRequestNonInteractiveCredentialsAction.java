package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link ChainingPrincipalFromRequestNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ChainingPrincipalFromRequestNonInteractiveCredentialsAction extends BasePrincipalFromNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingPrincipalFromRequestNonInteractiveCredentialsAction.class);

    private List<BasePrincipalFromNonInteractiveCredentialsAction> chain = new ArrayList<>();

    public ChainingPrincipalFromRequestNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                                       final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                                       final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                                       final PrincipalFactory principalFactory,
                                                                       final RemoteRequestPrincipalAttributesExtractor extractor) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy, principalFactory, extractor);
    }

    /**
     * Add action.
     *
     * @param action the action
     */
    public void addAction(final BasePrincipalFromNonInteractiveCredentialsAction action) {
        this.chain.add(action);
    }

    @Override
    protected String getRemotePrincipalId(final HttpServletRequest request) {
        AnnotationAwareOrderComparator.sort(this.chain);
        return this.chain
                .stream()
                .map(action -> action.getRemotePrincipalId(request))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
