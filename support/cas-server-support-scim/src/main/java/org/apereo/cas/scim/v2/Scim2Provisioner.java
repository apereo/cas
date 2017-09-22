package org.apereo.cas.scim.v2;

import com.unboundid.scim2.client.ScimService;
import com.unboundid.scim2.common.types.UserResource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.scim.api.ScimProvisioner;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * This is {@link Scim2Provisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Scim2Provisioner implements ScimProvisioner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scim2Provisioner.class);
    private final ScimService scimService;
    private final Scim2PrincipalAttributeMapper mapper;

    public Scim2Provisioner(final String target, final String oauthToken,
                            final String username, final String password,
                            final Scim2PrincipalAttributeMapper mapper) {
        final ClientConfig config = new ClientConfig();
        final ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
        config.connectorProvider(connectorProvider);
        final Client client = ClientBuilder.newClient(config);
        
        if (StringUtils.isNotBlank(oauthToken)) {
            client.register(OAuth2ClientSupport.feature(oauthToken));
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            client.register(HttpAuthenticationFeature.basic(username, password));
        }
        
        final WebTarget webTarget = client.target(target);
        this.scimService = new ScimService(webTarget);
        this.mapper = mapper;
    }

    @Override
    public boolean create(final Principal p, final UsernamePasswordCredential credential) {
        try {
            final UserResource currentUser = scimService.retrieve("Users", p.getId(), UserResource.class);
            if (currentUser != null) {
                return updateUserResource(currentUser, p, credential);
            }
            return createUserResource(p, credential);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean updateUserResource(final UserResource user, final Principal p,
                                       final UsernamePasswordCredential credential) throws Exception {
        this.mapper.map(user, p, credential);
        return scimService.replace(user) != null;
    }

    private boolean createUserResource(final Principal p, final UsernamePasswordCredential credential) throws Exception {
        final UserResource user = new UserResource();
        this.mapper.map(user, p, credential);
        return scimService.create("Users", user) != null;
    }
}
