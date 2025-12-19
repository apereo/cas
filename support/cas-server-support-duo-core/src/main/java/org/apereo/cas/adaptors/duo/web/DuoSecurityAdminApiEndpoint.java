package org.apereo.cas.adaptors.duo.web;

import module java.base;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link DuoSecurityAdminApiEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Endpoint(id = "duoAdmin", defaultAccess = Access.NONE)
public class DuoSecurityAdminApiEndpoint extends BaseCasRestActuatorEndpoint {

    public DuoSecurityAdminApiEndpoint(final CasConfigurationProperties casProperties,
                                       final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
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
        @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to fetch"),
        @Parameter(name = "providerId", description = "The multifactor authentication provider id defined in CAS settings")
    })
    public Map<String, DuoSecurityUserAccount> getUser(@PathVariable("username") final String username,
                                                       @RequestParam(required = false) final String providerId) {
        val results = new LinkedHashMap<String, DuoSecurityUserAccount>();
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .filter(provider -> provider.getDuoAuthenticationService().getAdminApiService().isPresent())
            .forEach(Unchecked.consumer(p -> {
                val duoService = p.getDuoAuthenticationService().getAdminApiService().orElseThrow();
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
        @Parameter(name = "username", required = true, in = ParameterIn.QUERY, description = "The username to create bypass codes for"),
        @Parameter(name = "providerId", description = "The multifactor authentication provider id defined in CAS settings"),
        @Parameter(name = "userId", description = "The user identifier, supplied by Duo Security to create bypass codes for")
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
            .filter(BeanSupplier::isNotProxy)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .filter(provider -> provider.getDuoAuthenticationService().getAdminApiService().isPresent())
            .forEach(Unchecked.consumer(provider -> {
                val duoService = provider.getDuoAuthenticationService().getAdminApiService().orElseThrow();
                val uid = StringUtils.isBlank(userId)
                    ? duoService.getDuoSecurityUserAccount(username).map(DuoSecurityUserAccount::getUserId).orElse(StringUtils.EMPTY)
                    : userId;
                if (StringUtils.isNotBlank(uid)) {
                    val codes = duoService.createDuoSecurityBypassCodesFor(uid);
                    results.put(provider.getId(), codes);
                }
            }));
        return results;
    }

    /**
     * Update user and return response entity.
     *
     * @param username   the username
     * @param providerId the provider id
     * @param account    the account
     * @return the response entity
     */
    @PutMapping(path = "/{username}",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Duo Security user account from Duo Admin API", parameters = {
        @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to update"),
        @Parameter(name = "providerId", description = "The multifactor authentication provider id defined in CAS settings")
    })
    public ResponseEntity updateUser(@PathVariable("username") final String username,
                                     @RequestParam(required = false) final String providerId,
                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                         description = "Duo user account as a JSON entity to update",
                                         required = true,
                                         content = @Content(schema = @Schema(implementation = DuoSecurityUserAccount.class)))
                                     @RequestBody final DuoSecurityUserAccount account) {
        val results = new LinkedHashMap<String, DuoSecurityUserAccount>();
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .filter(provider -> StringUtils.isBlank(providerId) || provider.matches(providerId))
            .filter(provider -> provider.getDuoAuthenticationService().getAdminApiService().isPresent())
            .forEach(Unchecked.consumer(provider -> {
                val duoService = provider.getDuoAuthenticationService().getAdminApiService().orElseThrow();
                duoService.modifyDuoSecurityUserAccount(account.withUsername(username))
                    .ifPresent(user -> results.put(provider.getId(), user));
            }));
        return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results);
    }
}
