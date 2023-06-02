package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorAuthenticationProperties;

import com.authy.AuthyApiClient;
import com.authy.api.User;
import lombok.val;

/**
 * This is {@link DefaultAuthyClientInstance}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public record DefaultAuthyClientInstance(AuthyApiClient authyClient, AuthyMultifactorAuthenticationProperties properties) implements AuthyClientInstance {
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
