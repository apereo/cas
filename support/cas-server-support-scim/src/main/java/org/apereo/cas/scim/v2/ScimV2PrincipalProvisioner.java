package org.apereo.cas.scim.v2;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;

import com.unboundid.scim2.client.ScimService;
import com.unboundid.scim2.common.types.UserResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;

import javax.ws.rs.client.ClientBuilder;

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
    public boolean create(final Authentication auth, final Credential credential,
                          final RegisteredService registeredService) {
        try {
            val principal = auth.getPrincipal();
            val currentUser = getScimService(registeredService)
                .retrieve("Users", principal.getId(), UserResource.class);
            if (currentUser != null) {
                return updateUserResource(currentUser, principal, credential, registeredService);
            }
            return createUserResource(principal, credential, registeredService);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Update user resource boolean.
     *
     * @param user              the user
     * @param principal         the principal
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     */
    @SneakyThrows
    protected boolean updateUserResource(final UserResource user, final Principal principal,
                                         final Credential credential,
                                         final RegisteredService registeredService) {
        this.mapper.map(user, principal, credential);
        return getScimService(registeredService).replace(user) != null;
    }

    /**
     * Create user resource boolean.
     *
     * @param principal         the principal
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     */
    @SneakyThrows
    protected boolean createUserResource(final Principal principal, final Credential credential,
                                         final RegisteredService registeredService) {
        val user = new UserResource();
        this.mapper.map(user, principal, credential);
        return getScimService(registeredService).create("Users", user) != null;
    }

    /**
     * Gets scim service.
     *
     * @param registeredService the registered service
     * @return the scim service
     */
    protected ScimService getScimService(final RegisteredService registeredService) {
        val config = new ClientConfig();
        val client = ClientBuilder.newClient(config);

        if (StringUtils.isNotBlank(scimProperties.getOauthToken())) {
            client.register(OAuth2ClientSupport.feature(scimProperties.getOauthToken()));
        }
        if (StringUtils.isNotBlank(scimProperties.getUsername()) && StringUtils.isNotBlank(scimProperties.getPassword())) {
            client.register(HttpAuthenticationFeature.basic(scimProperties.getUsername(), scimProperties.getPassword()));
        }
        val webTarget = client.target(scimProperties.getTarget());
        return new ScimService(webTarget);
    }
}
