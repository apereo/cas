package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link DuoSecurityUserAccountStatusEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Endpoint(id = "duoAccountStatus", enableByDefault = false)
public class DuoSecurityUserAccountStatusEndpoint extends BaseCasActuatorEndpoint {
    private static final int MAP_SIZE = 8;

    private final ApplicationContext applicationContext;

    public DuoSecurityUserAccountStatusEndpoint(final CasConfigurationProperties casProperties,
                                                final ApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Fetch account status map.
     *
     * @param username   the username
     * @param providerId the provider id
     * @return the map
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<?, ?> fetchAccountStatus(@Selector final String username, @Nullable final String providerId) {
        val results = new LinkedHashMap<>(MAP_SIZE);
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .forEach(p -> {
                val duoService = p.getDuoAuthenticationService();
                val accountStatus = duoService.getUserAccount(username);
                results.put(p.getId(),
                    CollectionUtils.wrap("duoApiHost", duoService.getApiHost(),
                        "name", p.getFriendlyName(),
                        "accountStatus", accountStatus
                    ));
            });
        return results;

    }
}
