package org.apereo.cas.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InternalTicketValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class InternalTicketValidatorTests {

    @Test
    public void verifyOperation() {
        val cas = mock(CentralAuthenticationService.class);
        val assertion = mock(Assertion.class);
        when(assertion.getPrimaryAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(cas.validateServiceTicket(anyString(), any(Service.class))).thenReturn(assertion);
        val validator = new InternalTicketValidator(cas, new WebApplicationServiceFactory());
        val assertionResult = validator.validate("ST-12345", RegisteredServiceTestUtils.CONST_TEST_URL2);
        assertNotNull(assertionResult);
        assertTrue(assertionResult.getAttributes().containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(assertionResult.getAttributes().containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
    }

}
