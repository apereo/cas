package org.apereo.cas.scim.v2;

import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import de.captaingoldfish.scim.sdk.client.ScimClientConfig;
import de.captaingoldfish.scim.sdk.client.ScimRequestBuilder;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.EndpointPaths;
import de.captaingoldfish.scim.sdk.common.constants.enums.Comparator;
import de.captaingoldfish.scim.sdk.common.constants.enums.PatchOp;
import de.captaingoldfish.scim.sdk.common.resources.Group;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import java.util.ArrayList;
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
public abstract class BaseScimService<T extends ScimProperties> implements ScimService {
    private final T scimProperties;

    @Override
    public ScimRequestBuilder getScimRequestBuilder(final Optional<RegisteredService> givenService) {
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

    @Override
    public ServerResponse<ListResponse<User>> findUser(final ScimRequestBuilder scimService, final String uid) {
        return scimService.list(User.class, EndpointPaths.USERS)
            .count(1)
            .filter("userName", Comparator.EQ, uid)
            .build()
            .get()
            .sendRequest();
    }

    @Override
    public ServerResponse<ListResponse<Group>> findGroup(final ScimRequestBuilder scimService, final String group) {
        return scimService.list(Group.class, EndpointPaths.GROUPS)
            .count(1)
            .filter("displayName", Comparator.EQ, group)
            .build()
            .get()
            .sendRequest();
    }


    @Override
    public ServerResponse<ListResponse<Group>> findUserGroups(final ScimRequestBuilder scimService, final String memberId) {
        return scimService.list(Group.class, EndpointPaths.GROUPS)
            .filter("members.$ref", Comparator.EQ, memberId)
            .build()
            .get()
            .sendRequest();
    }

    /**
     * Update user.
     *
     * @param scimService the scim service
     * @param user        the user
     * @return the boolean
     */
    public Optional<User> updateUser(final ScimRequestBuilder scimService, final User user) {
        val response = scimService
            .update(User.class, EndpointPaths.USERS, user.getId().orElseThrow())
            .setResource(user)
            .sendRequest();
        return response.isSuccess() ? Optional.of(response.getResource()) : Optional.empty();
    }

    /**
     * Remove user from group.
     *
     * @param scimService the scim service
     * @param group       the group
     * @param user        the user
     */
    public void removeUserFromGroup(final ScimRequestBuilder scimService, final Group group, final User user) {
        val path = "members[$ref eq \"" + user.getId().orElseThrow() + "\"]";
        scimService.patch(Group.class, EndpointPaths.GROUPS, group.getId().orElseThrow())
            .addOperation()
            .op(PatchOp.REMOVE)
            .path(path)
            .build()
            .sendRequest();
    }

    /**
     * Create user.
     *
     * @param scimService the scim service
     * @param user        the user
     * @return the boolean
     */
    public Optional<User> createUser(final ScimRequestBuilder scimService, final User user) {
        val response = scimService
            .create(User.class, EndpointPaths.USERS)
            .setResource(user)
            .sendRequest();
        return response.isSuccess() ? Optional.of(response.getResource()) : Optional.empty();
    }

    /**
     * Create or update groups.
     *
     * @param scimService the scim service
     * @param group       the group
     * @return the boolean
     */
    public boolean createOrUpdateGroups(final ScimRequestBuilder scimService, final Group group) {
        val response = findGroup(scimService, group.getDisplayName().orElseThrow());
        if (response.isSuccess()) {
            if (response.getResource().getTotalResults() > 0) {
                val existingGroup = response.getResource().getListedResources().getFirst();
                group.getDisplayName().ifPresent(existingGroup::setDisplayName);
                group.getExternalId().ifPresent(existingGroup::setExternalId);

                val currentMembers = new ArrayList<>(existingGroup.getMembers());
                currentMembers.removeIf(member -> group.getMembers().contains(member));
                currentMembers.addAll(group.getMembers());
                existingGroup.setMembers(currentMembers);

                val groupServerResponse = scimService.update(Group.class, EndpointPaths.GROUPS, existingGroup.getId().orElseThrow())
                    .setResource(existingGroup)
                    .sendRequest();
                return groupServerResponse.isSuccess();
            }
            return scimService.create(Group.class, EndpointPaths.GROUPS)
                .setResource(group)
                .sendRequest()
                .isSuccess();
        }
        return false;
    }
}
