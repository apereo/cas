package org.apereo.cas.scim.v1;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.schema.CoreSchema;
import com.unboundid.scim.sdk.OAuthToken;
import com.unboundid.scim.sdk.SCIMEndpoint;
import com.unboundid.scim.sdk.SCIMService;
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
 */
@Slf4j
public class ScimV1PrincipalProvisioner implements PrincipalProvisioner {

    private final SCIMEndpoint<UserResource> endpoint;
    private final ScimV1PrincipalAttributeMapper mapper;

    public ScimV1PrincipalProvisioner(final String target, final String oauthToken,
                                      final String username, final String password,
                                      final ScimV1PrincipalAttributeMapper mapper) {
        this.mapper = mapper;

        val uri = URI.create(target);
        val scimService = StringUtils.isNotBlank(oauthToken)
            ? new SCIMService(uri, new OAuthToken(oauthToken))
            : new SCIMService(uri, username, password);

        scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        this.endpoint = scimService.getUserEndpoint();
    }

    @Override
    public boolean create(final Authentication auth, final Principal p, final Credential credential) {
        try {
            val resources = endpoint.query("userName eq \"" + p.getId() + '"');
            if (resources.getTotalResults() <= 0) {
                LOGGER.debug("User [{}] not found", p.getId());
                return false;
            }

            val user = resources.iterator().next();
            if (user != null) {
                return updateUserResource(user, p, credential);
            }
            return createUserResource(p, credential);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        return endpoint.create(user) != null;
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
        return endpoint.update(user) != null;
    }
}
