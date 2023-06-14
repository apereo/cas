package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.WebApplicationService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationServiceSelectionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class DefaultAuthenticationServiceSelectionPlanTests {

    @Test
    void verifyOperation() {
        val input = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        assertThrows(ClassCastException.class,
            () -> input.resolveService(CoreAuthenticationTestUtils.getService(), WebApplicationService.class));
    }

}
