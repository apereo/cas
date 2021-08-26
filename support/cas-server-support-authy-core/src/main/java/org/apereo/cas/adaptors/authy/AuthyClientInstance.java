package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorAuthenticationProperties;

import com.authy.AuthyApiClient;
import com.authy.api.Tokens;
import com.authy.api.User;
import com.authy.api.Users;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

/**
 * This is {@link AuthyClientInstance}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class AuthyClientInstance {

    private final Users authyUsers;

    private final Tokens authyTokens;

    private final AuthyMultifactorAuthenticationProperties properties;

    @SneakyThrows
    public AuthyClientInstance(final AuthyMultifactorAuthenticationProperties settings) {
        this.properties = settings;
        val authyUrl = StringUtils.defaultIfBlank(properties.getApiUrl(), AuthyApiClient.DEFAULT_API_URI);
        val url = new URL(authyUrl);
        val testFlag = url.getProtocol().equalsIgnoreCase("http");
        val authyClient = new AuthyApiClient(properties.getApiKey(), authyUrl, testFlag);
        this.authyUsers = authyClient.getUsers();
        this.authyTokens = authyClient.getTokens();
    }

    /**
     * Gets authy error message.
     *
     * @param err the err
     * @return the authy error message
     */
    public static String getErrorMessage(final com.authy.api.Error err) {
        val builder = new StringBuilder(100);
        if (err != null) {
            builder.append("Authy Error");
            if (StringUtils.isNotBlank(err.getCountryCode())) {
                builder.append(": Country Code: ").append(err.getCountryCode());
            }
            if (StringUtils.isNotBlank(err.getMessage())) {
                builder.append(": Message: ").append(err.getMessage());
            }
        } else {
            builder.append("An unknown error has occurred. Check your API key and URL settings.");
        }
        return builder.toString();
    }

    /**
     * Gets or create user.
     *
     * @param principal the principal
     * @return the or create user
     */
    @SneakyThrows
    public User getOrCreateUser(final Principal principal) {
        val attributes = principal.getAttributes();
        if (!attributes.containsKey(properties.getMailAttribute())) {
            throw new IllegalArgumentException("No email address found for " + principal.getId());
        }
        if (!attributes.containsKey(properties.getPhoneAttribute())) {
            throw new IllegalArgumentException("No phone number found for " + principal.getId());
        }

        val email = attributes.get(properties.getMailAttribute()).get(0).toString();
        val phone = attributes.get(properties.getPhoneAttribute()).get(0).toString();
        return this.authyUsers.createUser(email, phone, "1");
    }
}
