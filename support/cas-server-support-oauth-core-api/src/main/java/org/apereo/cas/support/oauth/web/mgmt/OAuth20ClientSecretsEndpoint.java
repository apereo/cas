package org.apereo.cas.support.oauth.web.mgmt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * This is {@link OAuth20ClientSecretsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "oauthClientSecrets", defaultAccess = Access.NONE)
@Slf4j
public class OAuth20ClientSecretsEndpoint extends BaseCasActuatorEndpoint {
    private final RandomStringGenerator clientSecretGenerator = new DefaultRandomStringGenerator();
    private final ObjectProvider<ServicesManager> servicesManager;

    public OAuth20ClientSecretsEndpoint(
        final ObjectProvider<ServicesManager> servicesManager,
        final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.servicesManager = servicesManager;
    }

    /**
     * Rotate client secrets.
     *
     * @param clientId    the client id
     * @param expireIn    the expire in
     * @param expiredOnly the expired only
     * @return the registered service
     */
    @WriteOperation
    @Operation(
        summary = "Rotate all or expired client secrets for a client application",
        parameters = {
            @Parameter(name = "clientId", required = true, in = ParameterIn.PATH, description = "The client id"),
            @Parameter(name = "expiredOnly", required = false, in = ParameterIn.QUERY,
                allowEmptyValue = true, description = "If true, only expired secrets will be rotated"),
            @Parameter(name = "expireIn", required = false, in = ParameterIn.QUERY,
                allowEmptyValue = true, description = "Duration value (Default P90D) to set the expiration date")
        })
    public RegisteredService rotate(@Selector final String clientId,
                                    @Nullable final String expireIn,
                                    @Nullable final Boolean expiredOnly) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager.getObject(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val clientSecretExp = StringUtils.isNotBlank(expireIn)
            ? Beans.newDuration(expireIn).toSeconds()
            : Beans.newDuration("P90D").toSeconds();
        val currentTime = ZonedDateTime.now(ZoneOffset.UTC);
        val expirationDate = currentTime.plusSeconds(clientSecretExp);
        val secrets = registeredService
            .getClientSecrets()
            .stream()
            .map(secret -> !BooleanUtils.toBoolean(expiredOnly) || secret.hasClientSecretExpired(registeredService)
                ? new OAuthRegisteredServiceClientSecret(clientSecretGenerator.getNewString(), expirationDate)
                : secret)
            .collect(Collectors.toList());
        registeredService.setClientSecrets(secrets);
        return servicesManager.getObject().save(registeredService);
    }
}
