package org.apereo.cas.pm.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategyTests}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Tag("WebflowConfig")
public class PasswordManagementSingleSignOnParticipationStrategyTests {

    @Test
    public void verifyStrategyWithANonPmRequest() {
        val s = new PasswordManagementSingleSignOnParticipationStrategy();
        assertFalse(s.supports(new MockRequestContext()));
    }

    @Test
    public void verifyStrategyWithAPmRequest() {
        val s = new PasswordManagementSingleSignOnParticipationStrategy();
        val ctx = new MockRequestContext();
        ctx.putRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, "resetToken");

        assertTrue(s.supports(ctx));
        assertFalse(s.isParticipating(ctx));
    }
}
