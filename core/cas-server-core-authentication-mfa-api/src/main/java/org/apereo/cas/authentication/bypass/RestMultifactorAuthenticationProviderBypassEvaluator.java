package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {

    private static final long serialVersionUID = -7553888418344342672L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public RestMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                final String providerId) {
        super(providerId);
        this.bypassProperties = bypassProperties;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication, final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        try {
            val principal = authentication.getPrincipal();
            val rest = bypassProperties.getRest();
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                    + "service [{}] and provider [{}] via REST endpoint [{}]",
                principal.getId(), registeredService, provider, rest.getUrl());

            val parameters = CollectionUtils.wrap("principal", principal.getId(), "provider", provider.getId());
            if (registeredService != null) {
                parameters.put("service", registeredService.getServiceId());
            }

            val response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(), parameters, new HashMap<>(0));
            return response.getStatusLine().getStatusCode() == HttpStatus.ACCEPTED.value();
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return true;
        }
    }
}
