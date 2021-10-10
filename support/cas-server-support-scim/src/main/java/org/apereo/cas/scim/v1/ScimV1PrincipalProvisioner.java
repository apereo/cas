package org.apereo.cas.scim.v1;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.schema.CoreSchema;
import com.unboundid.scim.sdk.OAuthToken;
import com.unboundid.scim.sdk.ResourceNotFoundException;
import com.unboundid.scim.sdk.SCIMEndpoint;
import com.unboundid.scim.sdk.SCIMService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 * This is {@link ScimV1PrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated Since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "6.4.0")
public class ScimV1PrincipalProvisioner implements PrincipalProvisioner {

    private final ScimProperties scimProperties;

    private final ScimV1PrincipalAttributeMapper mapper;

    @Override
    public boolean create(final Authentication auth, final Credential credential, final RegisteredService registeredService) {
        val principal = auth.getPrincipal();
        try {
            val resources = getScimEndpoint().query("userName eq \"" + principal.getId() + '"');
            if (resources.getTotalResults() <= 0) {
                LOGGER.debug("User [{}] not found", principal.getId());
                return false;
            }

            val user = resources.iterator().next();
            if (user != null) {
                return updateUserResource(user, principal, credential);
            }
            return createUserResource(principal, credential);
        } catch (final ResourceNotFoundException e) {
            LOGGER.debug("User [{}] not found", principal.getId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Create user resource boolean.
     *
     * @param p          the p
     * @param credential the credential
     * @return true/false
     */
    @SneakyThrows
    protected boolean createUserResource(final Principal p, final Credential credential) {
        val user = new UserResource(CoreSchema.USER_DESCRIPTOR);
        this.mapper.map(user, p, credential);
        return getScimEndpoint().create(user) != null;
    }

    /**
     * Update user resource boolean.
     *
     * @param user       the user
     * @param p          the p
     * @param credential the credential
     * @return true/false
     */
    @SneakyThrows
    protected boolean updateUserResource(final UserResource user, final Principal p, final Credential credential) {
        this.mapper.map(user, p, credential);
        return getScimEndpoint().update(user) != null;
    }

    /**
     * Gets scim endpoint.
     *
     * @return the scim endpoint
     */
    protected SCIMEndpoint<UserResource> getScimEndpoint() {
        val uri = URI.create(scimProperties.getTarget());
        val scimService = StringUtils.isNotBlank(scimProperties.getOauthToken())
            ? new SCIMService(uri, new OAuthToken(scimProperties.getOauthToken()))
            : new SCIMService(uri, scimProperties.getUsername(), scimProperties.getPassword());

        scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        return scimService.getUserEndpoint();
    }
}
