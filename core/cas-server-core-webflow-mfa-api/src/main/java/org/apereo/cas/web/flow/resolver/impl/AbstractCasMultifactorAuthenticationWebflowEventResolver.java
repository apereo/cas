package org.apereo.cas.web.flow.resolver.impl;

/**
 * This is {@link AbstractCasMultifactorAuthenticationWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle MFA webflow changes.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
public abstract class AbstractCasMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    public AbstractCasMultifactorAuthenticationWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }
}
