package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfirmConsentActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
class ConfirmConsentActionTests extends BaseConsentActionTests {

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("reminder", "10");
        context.setParameter("reminderTimeUnit", ChronoUnit.DAYS.name());
        context.setParameter("option", String.valueOf(ConsentReminderOptions.ALWAYS.getValue()));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("consentService"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, confirmConsentAction.execute(context).getId());
    }
}
