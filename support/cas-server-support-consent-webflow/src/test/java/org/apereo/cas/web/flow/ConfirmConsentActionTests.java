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

    @Test
    void verifyReminderOptionsThatCannotBeApplied() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("reminder", "not-a-number");
        context.setParameter("reminderTimeUnit", ChronoUnit.FOREVER.name());
        context.setParameter("option", "unknown");

        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authentication, context);
        val service = CoreAuthenticationTestUtils.getWebApplicationService("consentService");
        WebUtils.putServiceIntoFlowScope(context, service);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, confirmConsentAction.execute(context).getId());

        val consentProperties = casProperties.getConsent().getCore();
        val decision = consentEngine.findConsentDecision(service, servicesManager.findServiceBy(service), authentication);
        assertNotNull(decision);
        assertEquals(consentProperties.getReminder(), decision.getReminder().longValue());
        assertEquals(consentProperties.getReminderTimeUnit(), decision.getReminderTimeUnit());
        assertEquals(ConsentReminderOptions.ATTRIBUTE_NAME, decision.getOptions());
    }
}
