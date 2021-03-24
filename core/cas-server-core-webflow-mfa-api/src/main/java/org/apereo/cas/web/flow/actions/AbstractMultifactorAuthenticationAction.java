package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;

/**
 * Abstract class that provides the doPreExecute() hook to set the find the provider for this webflow to be used by
 * extending classes in doExecute().
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMultifactorAuthenticationAction<T extends MultifactorAuthenticationProvider> extends AbstractAction {
    /**
     * The resolved provider for this flow.
     */
    protected transient T provider;

    private final transient ApplicationContext applicationContext;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        val providerId = WebUtils.getMultifactorAuthenticationProviderById(requestContext);
        this.provider = (T) MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext)
            .orElseThrow(() -> new AuthenticationException("Unable to determine multifactor authentication provider for " + providerId));
        return null;
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    protected Principal resolvePrincipal(final Principal principal) {
        val resolvers = new ArrayList<>(applicationContext.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class).values());
        AnnotationAwareOrderComparator.sort(resolvers);
        
        return resolvers
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }
}
