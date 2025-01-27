package org.apereo.cas.scim.v2.provisioning;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties;
import org.apereo.cas.scim.v2.BaseScimService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import de.captaingoldfish.scim.sdk.common.constants.EndpointPaths;
import de.captaingoldfish.scim.sdk.common.constants.enums.Comparator;
import de.captaingoldfish.scim.sdk.common.resources.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Optional;

/**
 * This is {@link ScimPrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class ScimPrincipalProvisioner extends BaseScimService<ScimProvisioningProperties> implements PrincipalProvisioner {

    private final ScimPrincipalAttributeMapper mapper;

    public ScimPrincipalProvisioner(final ScimProvisioningProperties scimProperties,
                                    final ScimPrincipalAttributeMapper mapper) {
        super(scimProperties);
        this.mapper = mapper;
    }

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
                val user = response.getResource().getListedResources().getFirst();
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
                                         final Optional<RegisteredService> registeredService) {
        mapper.map(user, principal, credential);
        LOGGER.trace("Updating user resource [{}]", user);
        val response = getScimService(registeredService)
            .update(User.class, EndpointPaths.USERS, user.getId().orElseThrow())
            .setResource(user)
            .sendRequest();
        return response.isSuccess();
    }

    protected boolean createUserResource(final Principal principal, final Credential credential,
                                         final Optional<RegisteredService> registeredService) {
        val user = new User();
        mapper.map(user, principal, credential);
        LOGGER.trace("Creating user resource [{}]", user);
        val response = getScimService(registeredService)
            .create(User.class, EndpointPaths.USERS)
            .setResource(user)
            .sendRequest();
        return response.isSuccess();
    }

}
