package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementWebflowUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Webflow")
class PasswordManagementWebflowUtilsTests {
    @Test
    void verifyResetQs() throws Throwable {
        val context = MockRequestContext.create();
        PasswordManagementWebflowUtils.putPasswordResetSecurityQuestions(context, List.of("Q1", "Q2"));
        WebUtils.putPasswordPolicyPattern(context, ".*");
        assertFalse(PasswordManagementWebflowUtils.getPasswordResetQuestions(context, List.class).isEmpty());
    }

}
