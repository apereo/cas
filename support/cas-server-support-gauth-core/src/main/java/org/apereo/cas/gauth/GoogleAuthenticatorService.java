package org.apereo.cas.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * This is {@link GoogleAuthenticatorService}.
 * A wrapper around the google authenticator instance {@link GoogleAuthenticator}
 * to allow for {@link org.springframework.cloud.context.config.annotation.RefreshScope}
 * on beans since original class is marked as final and cannot be refreshed
 * via proxies.
 * This uses the delegate pattern to route calls to the real instance.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorService implements IGoogleAuthenticator {
    @Delegate(types = IGoogleAuthenticator.class)
    private final GoogleAuthenticator googleAuthenticator;
}
