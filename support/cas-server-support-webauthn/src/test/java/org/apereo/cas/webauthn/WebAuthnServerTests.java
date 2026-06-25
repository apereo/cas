package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.core.WebAuthnServer;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnServerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class WebAuthnServerTests {

    abstract static class BaseWebAuthnServerStartRegistrationTests {
        @Autowired
        @Qualifier("webAuthnServer")
        private WebAuthnServer webAuthnServer;

        protected abstract AuthenticatorAttachment expectedAuthenticatorAttachment();

        protected abstract UserVerificationRequirement expectedUserVerificationRequirement();

        protected abstract ResidentKeyRequirement requestedResidentKeyRequirement();

        @Test
        void verifyOperation() {
            val request = new MockHttpServletRequest();
            val username = UUID.randomUUID().toString();
            val registration = webAuthnServer.startRegistration(
                request,
                username,
                Optional.of("CAS User"),
                Optional.of("key"),
                requestedResidentKeyRequirement(),
                Optional.empty()
            );

            assertTrue(registration.isRight());
            val options = registration.right().orElseThrow().publicKeyCredentialCreationOptions();
            val authenticatorSelection = options.getAuthenticatorSelection().orElseThrow();
            assertEquals(expectedAuthenticatorAttachment(), authenticatorSelection.getAuthenticatorAttachment().orElse(null));
            assertEquals(expectedUserVerificationRequirement(), authenticatorSelection.getUserVerification().orElse(null));
            assertEquals(requestedResidentKeyRequirement(), authenticatorSelection.getResidentKey().orElseThrow());
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = "cas.server.name=https://localhost:8443")
    class WebAuthnServerStartRegistrationNullTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return null;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return null;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.REQUIRED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=REQUIRED"
        })
    class WebAuthnServerStartRegistrationPlatformRequiredTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.REQUIRED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.REQUIRED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=CROSS_PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=DISCOURAGED"
        })
    class WebAuthnServerStartRegistrationCrossPlatformDiscouragedTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.CROSS_PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.DISCOURAGED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.DISCOURAGED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=PREFERRED"
        })
    class WebAuthnServerStartRegistrationPlatformPreferredTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.PREFERRED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.DISCOURAGED;
        }
    }
}
