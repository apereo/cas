package org.apereo.cas.scim.v2;

import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import de.captaingoldfish.scim.sdk.client.ScimClientConfig;
import de.captaingoldfish.scim.sdk.client.ScimRequestBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link BaseScimService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseScimService<T extends ScimProperties> {
    private final T scimProperties;

    protected ScimRequestBuilder getScimService(final Optional<RegisteredService> givenService) {
        val headersMap = new HashMap<String, String>();

        var token = scimProperties.getOauthToken();
        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperty.RegisteredServiceProperties.SCIM_OAUTH_TOKEN.isAssignedTo(registeredService)) {
                token = RegisteredServiceProperty.RegisteredServiceProperties.SCIM_OAUTH_TOKEN.getPropertyValue(registeredService).value();
            }
        }
        if (StringUtils.isNotBlank(token)) {
            headersMap.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        var username = scimProperties.getUsername();
        var password = scimProperties.getPassword();
        var target = scimProperties.getTarget();

        val scimClientConfigBuilder = ScimClientConfig.builder();
        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperty.RegisteredServiceProperties.SCIM_USERNAME.isAssignedTo(registeredService)) {
                username = RegisteredServiceProperty.RegisteredServiceProperties.SCIM_USERNAME.getPropertyValue(registeredService).value();
            }
            if (RegisteredServiceProperty.RegisteredServiceProperties.SCIM_PASSWORD.isAssignedTo(registeredService)) {
                password = RegisteredServiceProperty.RegisteredServiceProperties.SCIM_PASSWORD.getPropertyValue(registeredService).value();
            }
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            scimClientConfigBuilder.basic(username, password);
        }

        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperty.RegisteredServiceProperties.SCIM_TARGET.isAssignedTo(registeredService)) {
                target = RegisteredServiceProperty.RegisteredServiceProperties.SCIM_TARGET.getPropertyValue(registeredService).value();
            }
        }
        LOGGER.debug("Using SCIM provisioning target [{}]", target);
        val scimClientConfig = scimClientConfigBuilder
            .connectTimeout(5)
            .requestTimeout(5)
            .socketTimeout(5)
            .hostnameVerifier((s, sslSession) -> true)
            .httpHeaders(headersMap)
            .build();
        return new ScimRequestBuilder(target, scimClientConfig);
    }
}
