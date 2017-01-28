package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;

/**
 * This is {@link FirstMultifactorAuthenticationProviderSelector}
 * that grabs onto the first authentication provider in the collection.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class FirstMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirstMultifactorAuthenticationProviderSelector.class);
    
    @Override
    public MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service, final Principal principal) {
        final Iterator<MultifactorAuthenticationProvider> it = providers.iterator();
        final MultifactorAuthenticationProvider provider = it.next();
        LOGGER.debug("Selected the first provider [{}] for service [{}] out of [{}] providers", provider, service, providers.size());
        return provider;
    }
}
