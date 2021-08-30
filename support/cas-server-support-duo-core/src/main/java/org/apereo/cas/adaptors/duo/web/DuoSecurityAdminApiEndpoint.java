package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link DuoSecurityAdminApiEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RestControllerEndpoint(id = "duoAdmin", enableByDefault = false)
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
    @GetMapping(path = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch Duo Security user account from Duo Admin API", parameters = {
        @Parameter(name = "username", required = true, in = ParameterIn.PATH),
        @Parameter(name = "providerId")
    })
    public Map<String, DuoSecurityUserAccount> getUser(@PathVariable("username") final String username,
                                                       @RequestParam(required = false) final String providerId) {
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

    /**
     * Create bypass codes.
     *
     * @param username   the username
     * @param providerId the provider id
     * @param userId     the user id
     * @return the map
     */
    @Operation(summary = "Create bypass codes using Duo Admin API", parameters = {
        @Parameter(name = "username", required = true),
        @Parameter(name = "providerId"),
        @Parameter(name = "userId")
    })
    @PostMapping(path = "/bypassCodes",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Long>> createBypassCodes(@RequestParam(value = "username", required = false) final String username,
                                                     @RequestParam(value = "providerId", required = false) final String providerId,
                                                     @RequestParam(value = "userId", required = false) final String userId) {
        val results = new LinkedHashMap<String, List<Long>>();
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .filter(provider -> provider.getDuoAuthenticationService().getAdminApiService().isPresent())
            .forEach(Unchecked.consumer(p -> {
                val duoService = p.getDuoAuthenticationService().getAdminApiService().get();
                val uid = StringUtils.isBlank(userId)
                    ? duoService.getDuoSecurityUserAccount(username).map(DuoSecurityUserAccount::getUserId).orElse(StringUtils.EMPTY)
                    : userId;
                if (StringUtils.isNotBlank(uid)) {
                    val codes = duoService.createDuoSecurityBypassCodesFor(uid);
                    results.put(p.getId(), codes);
                }
            }));
        return results;
    }
}
