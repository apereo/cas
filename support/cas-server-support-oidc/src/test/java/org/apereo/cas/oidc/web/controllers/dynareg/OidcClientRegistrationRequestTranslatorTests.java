package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.util.OidcOutboundHttpRequestUtils;
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

    @Test
    void verifySectorIdentifierUriAddressValidation() {
        assertDoesNotThrow(() -> OidcOutboundHttpRequestUtils.validate("https://8.8.8.8/sector-identifier"));

        val unsafeUris = List.of(
            "http://8.8.8.8/sector-identifier",
            "https://127.0.0.1/sector-identifier",
            "https://10.0.0.1/sector-identifier",
            "https://100.64.0.1/sector-identifier",
            "https://169.254.169.254/latest/meta-data",
            "https://192.168.1.1/sector-identifier",
            "https://[::1]/sector-identifier",
            "https://[fc00::1]/sector-identifier",
            "https://user@example.org/sector-identifier");
        unsafeUris.forEach(uri -> assertThrows(IllegalArgumentException.class,
            () -> OidcOutboundHttpRequestUtils.validate(uri)));
    }

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
