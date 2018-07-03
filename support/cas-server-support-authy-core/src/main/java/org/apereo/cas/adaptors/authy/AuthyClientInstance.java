package org.apereo.cas.adaptors.authy;

import com.authy.AuthyApiClient;
import com.authy.api.Tokens;
import com.authy.api.User;
import com.authy.api.Users;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;

import java.net.URL;

/**
 * This is {@link AuthyClientInstance}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class AuthyClientInstance {

    private final AuthyApiClient authyClient;
    private final Users authyUsers;
    private final Tokens authyTokens;

    private final String mailAttribute;
    private final String phoneAttribute;
    private final String countryCode;

    @SneakyThrows
    public AuthyClientInstance(final String apiKey,
                               final String apiUrl,
                               final String mailAttribute,
                               final String phoneAttribute,
                               final String countryCode) {

        this.mailAttribute = mailAttribute;
        this.phoneAttribute = phoneAttribute;
        this.countryCode = countryCode;

        final var authyUrl = StringUtils.defaultIfBlank(apiUrl, AuthyApiClient.DEFAULT_API_URI);
        final var url = new URL(authyUrl);
        final var testFlag = url.getProtocol().equalsIgnoreCase("http");
        this.authyClient = new AuthyApiClient(apiKey, authyUrl, testFlag);
        this.authyUsers = this.authyClient.getUsers();
        this.authyTokens = this.authyClient.getTokens();

    }

    public Users getAuthyUsers() {
        return authyUsers;
    }

    public Tokens getAuthyTokens() {
        return authyTokens;
    }

    /**
     * Gets authy error message.
     *
     * @param err the err
     * @return the authy error message
     */
    public static String getErrorMessage(final com.authy.api.Error err) {
        final var builder = new StringBuilder();
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
        final var email = (String) principal.getAttributes().get(this.mailAttribute);
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("No email address found for " + principal.getId());
        }
        final var phone = (String) principal.getAttributes().get(this.phoneAttribute);
        if (StringUtils.isBlank(phone)) {
            throw new IllegalArgumentException("No phone number found for " + principal.getId());
        }
        return this.authyUsers.createUser(email, phone, this.countryCode);
    }
}
