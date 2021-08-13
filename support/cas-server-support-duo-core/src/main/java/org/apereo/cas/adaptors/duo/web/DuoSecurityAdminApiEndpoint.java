package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link DuoSecurityAdminApiEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Endpoint(id = "duoAdmin", enableByDefault = false)
public class DuoSecurityAdminApiEndpoint extends BaseCasActuatorEndpoint {
    private final ApplicationContext applicationContext;

    public DuoSecurityAdminApiEndpoint(final CasConfigurationProperties casProperties,
                                       final ApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Fetch duo user account from admin api.
     *
     * @param username   the username
     * @param providerId the provider id
     * @return the map
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Duo Security user account from Duo Admin API", parameters = {
        @Parameter(name = "username", required = true),
        @Parameter(name = "providerId")
    })
    public Map<String, DuoSecurityUserAccount> getUser(@Selector final String username, @Nullable final String providerId) {
        val results = new LinkedHashMap<String, DuoSecurityUserAccount>();
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .filter(provider -> provider.getDuoAuthenticationService().getAdminApiService().isPresent())
            .forEach(Unchecked.consumer(p -> {
                val duoService = p.getDuoAuthenticationService().getAdminApiService().get();
                duoService.getDuoSecurityUserAccount(username).ifPresent(user -> results.put(p.getId(), user));
            }));
        return results;
    }
}
