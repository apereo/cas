package org.apereo.cas.web.flow.resolver.impl;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link AbstractCasMultifactorAuthenticationWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle MFA webflow changes.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Slf4j
public abstract class AbstractCasMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    public AbstractCasMultifactorAuthenticationWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }
}
