package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.util.Locale;

/**
 * This is {@link RestMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {

    @Serial
    private static final long serialVersionUID = -7553888418344342672L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public RestMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                final String providerId,
                                                                final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
        this.bypassProperties = bypassProperties;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        try {
            val principal = resolvePrincipal(authentication.getPrincipal());
            val rest = bypassProperties.getRest();
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                    + "service [{}] and provider [{}] via REST endpoint [{}]",
                principal.getId(), registeredService, provider, rest.getUrl());

            val parameters = CollectionUtils.<String, String>wrap("principal", principal.getId(), "provider", provider.getId());
            if (registeredService != null) {
                parameters.put("service", registeredService.getServiceId());
            }

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.valueOf(rest.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .headers(rest.getHeaders())
                .url(rest.getUrl())
                .parameters(parameters)
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .build();

            val response = HttpUtils.execute(exec);
            return response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return true;
        }
    }
}
