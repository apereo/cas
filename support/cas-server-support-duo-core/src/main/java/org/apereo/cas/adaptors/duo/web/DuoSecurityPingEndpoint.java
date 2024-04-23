package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "Ping Duo Security given the provider id", parameters = @Parameter(name = "providerId"))
    public Map<?, ?> pingDuo(@Nullable final String providerId) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val results = new LinkedHashMap<>();
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .forEach(p -> {
                val duoService = p.getDuoAuthenticationService();
                val available = duoService.ping();
                results.put(p.getId(),
                    CollectionUtils.wrap("duoApiHost", resolver.resolve(duoService.getProperties().getDuoApiHost()),
                        "name", p.getFriendlyName(), "availability", available
                    ));
            });
        return results;
    }
}
