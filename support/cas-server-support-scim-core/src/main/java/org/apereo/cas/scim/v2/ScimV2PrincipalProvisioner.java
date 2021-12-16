package org.apereo.cas.scim.v2;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.util.LoggingUtils;

import com.unboundid.scim2.client.ScimService;
import com.unboundid.scim2.common.filters.Filter;
import com.unboundid.scim2.common.types.UserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;

import javax.ws.rs.client.ClientBuilder;
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
            val userList = getScimService(registeredService)
                .search("Users", Filter.eq("userName", principal.getId()).toString(), UserResource.class);
            if (userList.getTotalResults() > 0) {
                val user = userList.getResources().iterator().next();
                return updateUserResource(user, principal, credential, registeredService);
            }
            return createUserResource(principal, credential, registeredService);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Update user resource.
     *
     * @param user              the user
     * @param principal         the principal
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     * @throws Exception the exception
     */
    protected boolean updateUserResource(final UserResource user, final Principal principal,
                                         final Credential credential,
                                         final Optional<RegisteredService> registeredService) throws Exception {
        this.mapper.map(user, principal, credential);
        LOGGER.trace("Updating user resource [{}]", user);
        return getScimService(registeredService).replace(user) != null;
    }

    /**
     * Create user resource boolean.
     *
     * @param principal         the principal
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     * @throws Exception the exception
     */
    protected boolean createUserResource(final Principal principal, final Credential credential,
                                         final Optional<RegisteredService> registeredService) throws Exception {
        val user = new UserResource();
        mapper.map(user, principal, credential);
        LOGGER.trace("Creating user resource [{}]", user);
        return getScimService(registeredService).create("Users", user) != null;
    }

    /**
     * Gets scim service.
     *
     * @param givenService the given service
     * @return the scim service
     */
    protected ScimService getScimService(final Optional<RegisteredService> givenService) {
        val config = new ClientConfig();
        val client = ClientBuilder.newClient(config);
        var token = scimProperties.getOauthToken();

        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_OAUTH_TOKEN.isAssignedTo(registeredService)) {
                token = RegisteredServiceProperties.SCIM_OAUTH_TOKEN.getPropertyValue(registeredService).getValue();
            }
        }

        if (StringUtils.isNotBlank(token)) {
            client.register(OAuth2ClientSupport.feature(token));
        }

        var username = scimProperties.getUsername();
        var password = scimProperties.getPassword();
        var target = scimProperties.getTarget();

        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_USERNAME.isAssignedTo(registeredService)) {
                username = RegisteredServiceProperties.SCIM_USERNAME.getPropertyValue(registeredService).getValue();
            }
            if (RegisteredServiceProperties.SCIM_PASSWORD.isAssignedTo(registeredService)) {
                password = RegisteredServiceProperties.SCIM_PASSWORD.getPropertyValue(registeredService).getValue();
            }
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            client.register(HttpAuthenticationFeature.basic(username, password));
        }

        if (givenService.isPresent()) {
            val registeredService = givenService.get();
            if (RegisteredServiceProperties.SCIM_TARGET.isAssignedTo(registeredService)) {
                target = RegisteredServiceProperties.SCIM_TARGET.getPropertyValue(registeredService).getValue();
            }
        }
        LOGGER.debug("Using SCIM provisioning target [{}]", target);
        val webTarget = client.target(target);
        return new ScimService(webTarget);
    }
}
