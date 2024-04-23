package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import org.apereo.cas.authentication.principal.Service;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=email",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=.+@example.*"
    })
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProvider() throws Throwable {
            val resolver = new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical());
            val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=email",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=.+@example.*",
        "cas.authn.mfa.triggers.principal.reverse-match=true"
    })
    class ReversedMatchTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProvider() throws Throwable {
            val resolver = new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical());
            val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
            val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=email",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=-nothing-",
        "cas.authn.mfa.triggers.principal.deny-if-unmatched=true"
    })
    class DenyTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyDenyWhenUnmatched() throws Throwable {
            val resolver = new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical());
            val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
            val exception = assertThrows(AuthenticationException.class,
                () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
            assertNotNull(exception.getCode());
            assertTrue(exception.getHandlerErrors().containsKey(MultifactorAuthenticationRequiredException.class.getSimpleName()));
        }
    }
}
