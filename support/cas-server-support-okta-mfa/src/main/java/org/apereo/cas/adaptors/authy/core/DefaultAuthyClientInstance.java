package org.apereo.cas.adaptors.authy.core;

import org.apereo.cas.adaptors.authy.core.AuthyClientInstance;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorAuthenticationProperties;

import com.authy.AuthyApiClient;
import com.authy.api.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link DefaultAuthyClientInstance}.
 *
 * @author Jérémie POISSON
 * 
 */
@Getter
@RequiredArgsConstructor
public class DefaultAuthyClientInstance implements AuthyClientInstance {
    private final AuthyApiClient authyClient;

    private final AuthyMultifactorAuthenticationProperties properties;

    /**
     * Gets or create user.
     *
     * @param principal the principal
     * @return the or create user
     * @throws Exception the exception
     */
    @Override
    public User getOrCreateUser(final Principal principal) throws Exception {
        val attributes = principal.getAttributes();
        if (!attributes.containsKey(properties.getMailAttribute())) {
            throw new IllegalArgumentException("No email address found for " + principal.getId());
        }
        if (!attributes.containsKey(properties.getPhoneAttribute())) {
            throw new IllegalArgumentException("No phone number found for " + principal.getId());
        }

        val email = attributes.get(properties.getMailAttribute()).get(0).toString();
        val phone = attributes.get(properties.getPhoneAttribute()).get(0).toString();
        return authyClient.getUsers().createUser(email, phone, "1");
    }
}
