package org.apereo.cas.scim.v1;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.schema.CoreSchema;
import com.unboundid.scim.sdk.OAuthToken;
import com.unboundid.scim.sdk.Resources;
import com.unboundid.scim.sdk.SCIMEndpoint;
import com.unboundid.scim.sdk.SCIMService;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.scim.api.ScimProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 * This is {@link Scim1Provisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Scim1Provisioner implements ScimProvisioner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scim1Provisioner.class);
    private final SCIMEndpoint<UserResource> endpoint;
    private final Scim1PrincipalAttributeMapper mapper;

    public Scim1Provisioner(final String target, final String oauthToken,
                            final String username, final String password,
                            final Scim1PrincipalAttributeMapper mapper) {
        this.mapper = mapper;

        final URI uri = URI.create(target);
        final SCIMService scimService;

        if (StringUtils.isNotBlank(oauthToken)) {
            scimService = new SCIMService(uri, new OAuthToken(oauthToken));
        } else {
            scimService = new SCIMService(uri, username, password);
        }
        scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        this.endpoint = scimService.getUserEndpoint();
    }

    @Override
    public boolean create(final Principal p, final UsernamePasswordCredential credential) {
        try {
            final Resources<UserResource> resources = endpoint.query("userName eq \"" + p.getId() + "\"");
            if (resources.getItemsPerPage() == 0) {
                LOGGER.debug("User [{}] not found", p.getId());
                return false;
            }

            final UserResource user = resources.iterator().next();
            if (user != null) {
                return updateUserResource(user, p, credential);
            }
            return createUserResource(p, credential);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean createUserResource(final Principal p, final UsernamePasswordCredential credential) throws Exception {
        final UserResource user = new UserResource(CoreSchema.USER_DESCRIPTOR);
        this.mapper.map(user, p, credential);
        return endpoint.create(user) != null;
    }

    private boolean updateUserResource(final UserResource user, final Principal p, final UsernamePasswordCredential credential) throws Exception {
        this.mapper.map(user, p, credential);
        return endpoint.update(user) != null;
    }
}
