package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.web.flow.authn.MultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;

/**
 * This is {@link BaseMultifactorAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver
        implements MultifactorAuthenticationWebflowEventResolver {
}
