package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.util.LoggingUtils;

import de.captaingoldfish.scim.sdk.client.ScimClientConfig;
import de.captaingoldfish.scim.sdk.client.ScimRequestBuilder;
import de.captaingoldfish.scim.sdk.common.constants.EndpointPaths;
import de.captaingoldfish.scim.sdk.common.constants.enums.Comparator;
import de.captaingoldfish.scim.sdk.common.resources.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link ScimV2PrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ScimV2PrincipalProvisioner implements PrincipalProvisioner {

    private final ScimProperties scimProperties;

    private final ScimV2PrincipalAttributeMapper mapper;

    @Override
    public boolean provision(final Principal principal, final Credential credential) {
        return provision(credential, Optional.empty(), principal);
    }

    @Override
    public boolean provision(final Authentication auth, final Credential credential,
                             final RegisteredService registeredService) {
        val principal = auth.getPrincipal();
        return provision(credential, Optional.ofNullable(registeredService), principal);
    }

    private boolean provision(final Credential credential,
                              final Optional<RegisteredService> registeredService,
                              final Principal principal) {
        try {
            LOGGER.info("Attempting to execute provisioning ops for [{}]", principal.getId());
            val scimService = getScimService(registeredService);
            val response = scimService.list(User.class, EndpointPaths.USERS)
                .count(1)
                .filter("userName", Comparator.EQ, principal.getId())
                .build()
                .get()
                .sendRequest();

            if (response.isSuccess() && response.getResource().getTotalResults() > 0) {
                val user = (User) response.getResource().getListedResources().getFirst();
                return updateUserResource(user, principal, credential, registeredService);
            }
            return createUserResource(principal, credential, registeredService);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    protected boolean updateUserResource(final User user, final Principal principal,
                                         final Credential credential,
                                         final Optional<RegisteredService> registeredService) throws Exception {
        this.mapper.map(user, principal, credential);
        LOGGER.trace("Updating user resource [{}]", user);
        val response = getScimService(registeredService)
            .update(User.class, EndpointPaths.USERS, user.getId().orElseThrow())
            .setResource(user)
            .sendRequest();
        return response.isSuccess();
    }

    protected boolean createUserResource(final Principal principal, final Credential credential,
                                         final Optional<RegisteredService> registeredService) throws Exception {
        val user = new User();
        mapper.map(user, principal, credential);
        LOGGER.trace("Creating user resource [{}]", user);
        val response = getScimService(registeredService)
            .create(User.class, EndpointPaths.USERS)
            .setResource(user)
            .sendRequest();
        return response.isSuccess();
    }

    protected ScimRequestBuilder getScimService(final Optional<RegisteredService> givenService) {
        val headersMap = new HashMap<String, String>();

        var token = scimProperties.getOauthToken();
        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_OAUTH_TOKEN.isAssignedTo(registeredService)) {
                token = RegisteredServiceProperties.SCIM_OAUTH_TOKEN.getPropertyValue(registeredService).value();
            }
        }
        if (StringUtils.isNotBlank(token)) {
            headersMap.put("Authorization", "Bearer " + token);
        }

        var username = scimProperties.getUsername();
        var password = scimProperties.getPassword();
        var target = scimProperties.getTarget();

        val scimClientConfigBuilder = ScimClientConfig.builder();
        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_USERNAME.isAssignedTo(registeredService)) {
                username = RegisteredServiceProperties.SCIM_USERNAME.getPropertyValue(registeredService).value();
            }
            if (RegisteredServiceProperties.SCIM_PASSWORD.isAssignedTo(registeredService)) {
                password = RegisteredServiceProperties.SCIM_PASSWORD.getPropertyValue(registeredService).value();
            }
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            scimClientConfigBuilder.basic(username, password);
        }

        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_TARGET.isAssignedTo(registeredService)) {
                target = RegisteredServiceProperties.SCIM_TARGET.getPropertyValue(registeredService).value();
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
