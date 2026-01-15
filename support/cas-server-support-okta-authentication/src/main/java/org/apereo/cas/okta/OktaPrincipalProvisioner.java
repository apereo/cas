package org.apereo.cas.okta;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.model.support.okta.OktaPrincipalProvisioningProperties;
import org.apereo.cas.util.CollectionUtils;
import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.CreateUserRequest;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserNextLogin;
import com.okta.sdk.resource.user.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link OktaPrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OktaPrincipalProvisioner implements PrincipalProvisioner {
    private final Client oktaClient;
    private final OktaPrincipalProvisioningProperties properties;

    @Override
    public boolean provision(final Principal principal, final Credential credential) {
        return provision(credential, principal);
    }

    protected boolean provision(final Credential credential, final Principal principal) {
        LOGGER.debug("Searching to find [{}]", principal.getId());
        val existingUser = oktaClient.getUser(principal.getId());
        return (existingUser == null || existingUser.getProfile().isEmpty())
            ? createUser(principal, credential)
            : updateUser(existingUser, principal);
    }

    protected boolean updateUser(final User user, final Principal principal) {
        val userProfile = mapPrincipalToUserProfile(user.getProfile(), principal, null);
        user.setProfile(userProfile);
        val updatedUser = oktaClient.partialUpdateUser(user, user.getId(), true);
        LOGGER.debug("Updated Okta user [{}]", updatedUser);
        return updatedUser != null;
    }

    protected boolean createUser(final Principal principal, final Credential credential) {
        val createUserRequest = oktaClient.instantiate(CreateUserRequest.class);
        val initialUserProfile = oktaClient.instantiate(UserProfile.class);
        initialUserProfile.setLogin(principal.getId());
        val userProfile = mapPrincipalToUserProfile(initialUserProfile, principal, credential);
        createUserRequest.setProfile(userProfile);
        val createdUser = oktaClient.createUser(createUserRequest, true, true, UserNextLogin.SDK_UNKNOWN);
        LOGGER.debug("Created Okta user [{}]", createdUser);
        return createdUser != null;
    }

    protected UserProfile mapPrincipalToUserProfile(final UserProfile userProfile, final Principal principal,
                                                    final Credential credential) {

        updateUserProfileWithAttribute(userProfile, principal, "department", userProfile::setDepartment);
        updateUserProfileWithAttribute(userProfile, principal, "city", userProfile::setCity);
        updateUserProfileWithAttribute(userProfile, principal, "costCenter", userProfile::setCostCenter);
        updateUserProfileWithAttribute(userProfile, principal, "countryCode", userProfile::setCountryCode);
        updateUserProfileWithAttribute(userProfile, principal, "displayName", userProfile::setDisplayName);
        updateUserProfileWithAttribute(userProfile, principal, "division", userProfile::setDivision);
        updateUserProfileWithAttribute(userProfile, principal, "email", userProfile::setEmail);
        updateUserProfileWithAttribute(userProfile, principal, "employeeNumber", userProfile::setEmployeeNumber);
        updateUserProfileWithAttribute(userProfile, principal, "honorificPrefix", userProfile::setHonorificPrefix);
        updateUserProfileWithAttribute(userProfile, principal, "honorificSuffix", userProfile::setHonorificSuffix);
        updateUserProfileWithAttribute(userProfile, principal, "lastName", userProfile::setLastName);
        updateUserProfileWithAttribute(userProfile, principal, "locale", userProfile::setLocale);
        updateUserProfileWithAttribute(userProfile, principal, "manager", userProfile::setManager);
        updateUserProfileWithAttribute(userProfile, principal, "managerId", userProfile::setManagerId);
        updateUserProfileWithAttribute(userProfile, principal, "middleName", userProfile::setMiddleName);
        updateUserProfileWithAttribute(userProfile, principal, "mobilePhone", userProfile::setMobilePhone);
        updateUserProfileWithAttribute(userProfile, principal, "nickName", userProfile::setNickName);
        updateUserProfileWithAttribute(userProfile, principal, "firstName", userProfile::setFirstName);
        updateUserProfileWithAttribute(userProfile, principal, "organization", userProfile::setOrganization);
        updateUserProfileWithAttribute(userProfile, principal, "postalAddress", userProfile::setPostalAddress);
        updateUserProfileWithAttribute(userProfile, principal, "preferredLanguage", userProfile::setPreferredLanguage);
        updateUserProfileWithAttribute(userProfile, principal, "primaryPhone", userProfile::setPrimaryPhone);
        updateUserProfileWithAttribute(userProfile, principal, "secondEmail", userProfile::setSecondEmail);
        updateUserProfileWithAttribute(userProfile, principal, "state", userProfile::setState);
        updateUserProfileWithAttribute(userProfile, principal, "streetAddress", userProfile::setStreetAddress);
        updateUserProfileWithAttribute(userProfile, principal, "timezone", userProfile::setTimezone);
        updateUserProfileWithAttribute(userProfile, principal, "title", userProfile::setTitle);
        updateUserProfileWithAttribute(userProfile, principal, "zipCode", userProfile::setZipCode);

        return userProfile;
    }

    protected void updateUserProfileWithAttribute(final UserProfile userProfile, final Principal principal,
                                                  final String attributeName, final Consumer<String> profileUpdater) {
        val mappedAttributeName = properties.getAttributeMappings().getOrDefault(attributeName, attributeName);
        if (principal.getAttributes().containsKey(mappedAttributeName)) {
            val values = principal.getAttributes().get(mappedAttributeName);
            LOGGER.trace("Setting Okta user profile attribute [{}] to [{}]", mappedAttributeName, values);
            CollectionUtils.firstElement(values).ifPresent(value -> profileUpdater.accept(value.toString()));
        }
    }

}
