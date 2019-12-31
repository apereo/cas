package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link DuoSecurityPingEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Endpoint(id = "duoPing", enableByDefault = false)
public class DuoSecurityPingEndpoint extends BaseCasActuatorEndpoint {
    private static final int MAP_SIZE = 8;

    private final ApplicationContext applicationContext;

    public DuoSecurityPingEndpoint(final CasConfigurationProperties casProperties,
                                   final ApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Ping duo and return availability result.
     *
     * @param providerId the provider id, if any.
     * @return the map
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<?, ?> pingDuo(@Nullable final String providerId) {
        val results = new LinkedHashMap<>(MAP_SIZE);
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .forEach(p -> {
                val duoService = p.getDuoAuthenticationService();
                val available = duoService.ping();
                results.put(p.getId(),
                    CollectionUtils.wrap("duoApiHost", duoService.getApiHost(),
                        "name", p.getFriendlyName(),
                        "availability", available
                    ));
            });
        return results;
    }
}
