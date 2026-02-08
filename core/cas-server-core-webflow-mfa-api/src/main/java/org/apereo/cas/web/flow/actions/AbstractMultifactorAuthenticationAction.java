package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class that provides the {@link #doPreExecute} hook to set the find the provider for this webflow to be used by
 * extending classes in {@link #doExecute}.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@SuppressWarnings("NullAway.Init")
public abstract class AbstractMultifactorAuthenticationAction<T extends MultifactorAuthenticationProvider> extends BaseCasWebflowAction {
    protected Principal resolvePrincipal(final Principal principal, final RequestContext requestContext) {
        val beanFactory = ((ConfigurableApplicationContext) requestContext.getActiveFlow().getApplicationContext()).getBeanFactory();
        val resolvers = new ArrayList<>(BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, MultifactorAuthenticationPrincipalResolver.class).values());
        AnnotationAwareOrderComparator.sort(resolvers);
        return resolvers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }

    protected T resolveMultifactorAuthenticationProvider(final RequestContext requestContext) {
        val providerId = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(requestContext);
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val provider = (T) MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext)
            .orElseThrow(() -> new AuthenticationException("Unable to determine multifactor authentication provider for " + providerId));
        Assert.isTrue(providerId.equalsIgnoreCase(provider.getId()),
            "Requested provider id %s does not match the available provider id %s".formatted(providerId, provider.getId()));
        return provider;
    }

    protected String getThrottledRequestKeyFor(final Authentication authentication,
                                               final RequestContext requestContext) {
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        return principal.getId();
    }
}
