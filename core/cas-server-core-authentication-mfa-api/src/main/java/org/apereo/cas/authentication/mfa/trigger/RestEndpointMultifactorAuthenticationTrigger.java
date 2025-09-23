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
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CasConfigurationProperties casProperties;

    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) {
        val restEndpoint = casProperties.getAuthn().getMfa().getTriggers().getRest();
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
        val results = FunctionUtils.doUnchecked(() -> callRestEndpointForMultifactor(principal, service));
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
     * @return the rest response, typically the mfa id.
     * @throws Exception the exception
     */
    protected String callRestEndpointForMultifactor(final Principal principal,
                                                    final Service resolvedService) throws Exception {
        HttpResponse response = null;
        try {
            val rest = casProperties.getAuthn().getMfa().getTriggers().getRest();
            val entity = new RestEndpointEntity(principal.getId(), resolvedService.getId());

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.valueOf(rest.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(rest.getUrl())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(entity))
                .build();
            response = HttpUtils.execute(exec);
            val status = HttpStatus.valueOf(response.getCode());
            if (status.is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    return IOUtils.toString(content, StandardCharsets.UTF_8);
                }
            }
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    /**
     * The Rest endpoint entity passed along to the API.
     */
    public record RestEndpointEntity(String principalId, String serviceId) {
    }

}
