package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class PrincipalAttributeMultifactorAuthenticationTriggerTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=mail",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=.+@example.*"
    })
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProvider() throws Throwable {
            val result = principalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=mail",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=.+@example.*",
        "cas.authn.mfa.triggers.principal.reverse-match=true"
    })
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class ReversedMatchTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProvider() throws Throwable {
            val result = principalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=mail",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=-nothing-",
        "cas.authn.mfa.triggers.principal.deny-if-unmatched=true"
    })
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DenyTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyDenyWhenUnmatched() {
            val exception = assertThrows(AuthenticationException.class,
                () -> principalAttributeMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                    new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
            assertNotNull(exception.getCode());
            assertTrue(exception.getHandlerErrors().containsKey(MultifactorAuthenticationRequiredException.class.getSimpleName()));
        }
    }
}
