package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads client credentials from the HTTP Basic {@code Authorization} header. A header that is not a Basic one, or
 * that cannot be decoded, fails with a {@link org.springframework.security.authentication.BadCredentialsException},
 * which counts as no credentials.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20BasicAuthenticationConverter implements AuthenticationConverter {
    @Override
    public @Nullable Authentication convert(final HttpServletRequest request) {
        return OAuth20Utils.readClientBasicAuthorization(request)
            .map(credentials -> new OAuth20ClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.HEADER,
                credentials.getLeft(), credentials.getRight()))
            .orElse(null);
    }
}
