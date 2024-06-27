package org.apereo.cas.pm;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordStrengthAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Webflow")
@TestPropertySource(properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.core.password-policy-pattern=^Th!.+{8,10}"
})
class PasswordStrengthAuthenticationPostProcessorTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier("passwordStrengthAuthenticationPostProcessor")
    private AuthenticationPostProcessor passwordStrengthAuthenticationPostProcessor;


    @Autowired
    @Qualifier("weakPasswordWebflowExceptionHandler")
    private CasWebflowExceptionHandler weakPasswordWebflowExceptionHandler;

    @Test
    void verifyOperation() throws Throwable {
        val credential = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        assertTrue(passwordStrengthAuthenticationPostProcessor.supports(credential));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService("service"), credential);
        val builder = mock(AuthenticationBuilder.class);
        val exception = assertThrows(WeakPasswordException.class,
            () -> passwordStrengthAuthenticationPostProcessor.process(builder, transaction));

        val requestContext = MockRequestContext.create(applicationContext);
        assertTrue(weakPasswordWebflowExceptionHandler.supports(exception, requestContext));
        val event = weakPasswordWebflowExceptionHandler.handle(exception, requestContext);
        assertEquals(WeakPasswordException.class.getSimpleName(), event.getId());
    }
}
