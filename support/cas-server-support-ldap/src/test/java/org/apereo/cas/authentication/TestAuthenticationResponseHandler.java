package org.apereo.cas.authentication;

import lombok.NoArgsConstructor;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;

/**
 * This is {@link TestAuthenticationResponseHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
public class TestAuthenticationResponseHandler implements AuthenticationResponseHandler {
    @Override
    public void handle(final AuthenticationResponse response) {
    }
}
