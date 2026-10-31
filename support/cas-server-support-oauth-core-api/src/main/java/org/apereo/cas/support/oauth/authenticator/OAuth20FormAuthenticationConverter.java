package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads credentials from two request parameters, such as {@code client_id} and {@code client_secret}; the request carries
 * none unless both are present.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OAuth20FormAuthenticationConverter implements AuthenticationConverter {
    private final String usernameParameter;

    private final String passwordParameter;

    @Override
    public @Nullable Authentication convert(final HttpServletRequest request) {
        val username = request.getParameter(usernameParameter);
        val password = request.getParameter(passwordParameter);
        return username != null && password != null
            ? new OAuth20ClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.FORM, username, password)
            : null;
    }
}
