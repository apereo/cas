package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link RestEndpointMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class RestEndpointMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;
    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {
        val restEndpoint = casProperties.getAuthn().getMfa().getRest();
        if (service == null || authentication == null) {
            LOGGER.trace("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }
        val principal = authentication.getPrincipal();
        if (StringUtils.isBlank(restEndpoint.getUrl())) {
            LOGGER.trace("Rest endpoint to determine event is not configured for [{}]", principal.getId());
            return Optional.empty();
        }
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return Optional.empty();
        }

        LOGGER.debug("Contacting [{}] to inquire about [{}]", restEndpoint, principal.getId());
        val results = callRestEndpointForMultifactor(principal, service);
        if (StringUtils.isNotBlank(results)) {
            return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(results, applicationContext);
        }

        return Optional.empty();
    }

    /**
     * Call rest endpoint for multifactor.
     *
     * @param principal       the principal
     * @param resolvedService the resolved service
     * @return return the rest response, typically the mfa id.
     */
    protected String callRestEndpointForMultifactor(final Principal principal, final Service resolvedService) {
        HttpResponse response = null;
        try {
            val rest = casProperties.getAuthn().getMfa().getRest();
            val entity = new RestEndpointEntity(principal.getId(), resolvedService.getId());
            response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(), MAPPER.writeValueAsString(entity));
            val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
            if (status.is2xxSuccessful()) {
                val content = response.getEntity().getContent();
                return IOUtils.toString(content, StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    /**
     * The Rest endpoint entity passed along to the API.
     */
    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class RestEndpointEntity {
        private final String principalId;
        private final String serviceId;
    }

}
