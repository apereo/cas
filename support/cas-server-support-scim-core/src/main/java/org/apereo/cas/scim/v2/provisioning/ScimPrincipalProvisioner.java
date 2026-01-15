package org.apereo.cas.scim.v2.provisioning;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties;
import org.apereo.cas.scim.v2.BaseScimService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
            val scimService = getScimRequestBuilder(registeredService);
            val response = findUser(scimService, principal.getId());
            return createOrUpdate(credential, registeredService, principal, response);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    private boolean createOrUpdate(final Credential credential,
                                   final Optional<RegisteredService> registeredService,
                                   final Principal principal,
                                   final ServerResponse<ListResponse<User>> response) {
        if (response.isSuccess()) {
            if (response.getResource().getTotalResults() > 0) {
                val user = response.getResource().getListedResources().getFirst();
                return updateUserResource(user, principal, credential, registeredService);
            }
            return createUserResource(principal, credential, registeredService);
        }
        return false;
    }

    protected boolean updateUserResource(final User user,
                                         final Principal principal,
                                         final Credential credential,
                                         final Optional<RegisteredService> registeredService) {
        mapper.forUpdate(user, principal, credential);
        LOGGER.trace("Updating user resource [{}]", user);

        val scimService = getScimRequestBuilder(registeredService);
        val updatedResult = updateUser(scimService, user);
        if (updatedResult.isPresent()) {
            val updatedUser = updatedResult.get();
            val groupsToCreateOrUpdate = mapper.forGroups(principal, updatedUser);
            val currentGroups = findUserGroups(scimService, updatedUser.getId().orElseThrow()).getResource().getListedResources();
            groupsToCreateOrUpdate.stream().parallel().forEach(group -> createOrUpdateGroups(scimService, group));
            currentGroups
                .stream()
                .parallel()
                .filter(group -> groupsToCreateOrUpdate.stream().noneMatch(g -> g.getDisplayName().equals(group.getDisplayName())))
                .forEach(group -> removeUserFromGroup(scimService, group, updatedUser));
        }
        return updatedResult.isPresent();
    }

    protected boolean createUserResource(final Principal principal, final Credential credential,
                                         final Optional<RegisteredService> registeredService) {
        val user = mapper.forCreate(principal, credential);
        LOGGER.trace("Creating user resource [{}]", user);
        val scimRequestBuilder = getScimRequestBuilder(registeredService);
        val createdResult = createUser(scimRequestBuilder, user);
        if (createdResult.isPresent()) {
            val createdUser = createdResult.get();
            val groupsToCreateOrUpdate = mapper.forGroups(principal, createdUser);
            groupsToCreateOrUpdate
                .stream()
                .parallel()
                .forEach(group -> createOrUpdateGroups(scimRequestBuilder, group));
        }
        return createdResult.isPresent();
    }
}
