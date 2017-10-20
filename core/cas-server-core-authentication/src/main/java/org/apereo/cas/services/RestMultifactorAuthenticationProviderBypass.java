package org.apereo.cas.services;

import org.apache.http.HttpResponse;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestMultifactorAuthenticationProviderBypass extends DefaultMultifactorAuthenticationProviderBypass {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestMultifactorAuthenticationProviderBypass.class);
    private static final long serialVersionUID = -7553888418344342672L;
    
    public RestMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties bypassProperties) {
        super(bypassProperties);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication, final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider) {
        try {
            final Principal principal = authentication.getPrincipal();
            final MultifactorAuthenticationProviderBypassProperties.Rest rest = bypassProperties.getRest();
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                            + "service [{}] and provider [{}] via REST endpoint [{}]",
                    principal.getId(), registeredService, provider, rest.getUrl());

            final HttpResponse response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
                    rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                    CollectionUtils.wrap("principal", CollectionUtils.wrap(principal.getId()),
                            "service", CollectionUtils.wrap(registeredService.getServiceId()),
                            "provider", CollectionUtils.wrap(provider.getId())));
            return response.getStatusLine().getStatusCode() == HttpStatus.ACCEPTED.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return super.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider);
    }
}
