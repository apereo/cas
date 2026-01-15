package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientRegistrationRequestTranslatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDC")
class OidcClientRegistrationRequestTranslatorTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-mode=OPEN")
    class OpenRegistrationMode extends AbstractOidcTests {

        @Autowired
        @Qualifier("oidcClientRegistrationRequestTranslator")
        private OidcClientRegistrationRequestTranslator oidcClientRegistrationRequestTranslator;

        @Test
        void verifyBadLogo() {
            val registrationRequest = new OidcClientRegistrationRequest();
            registrationRequest.setRedirectUris(List.of("https://apereo.github.io"));
            registrationRequest.setLogo("https://github.com/apereo.can");
            assertThrows(IllegalArgumentException.class,
                () -> oidcClientRegistrationRequestTranslator.translate(registrationRequest, Optional.empty()));
        }

        @Test
        void verifyBadPolicy() {
            val registrationRequest = new OidcClientRegistrationRequest();
            registrationRequest.setRedirectUris(List.of("https://apereo.github.io"));
            registrationRequest.setPolicyUri("https://github.com/apereo.can");
            assertThrows(IllegalArgumentException.class,
                () -> oidcClientRegistrationRequestTranslator.translate(registrationRequest, Optional.empty()));
        }
    }
}
