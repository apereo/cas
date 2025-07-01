package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InitPasswordChangeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
class InitPasswordChangeActionTests extends BasePasswordManagementActionTests {
    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val attributes = new LocalAttributeMap<Object>(CasWebflowConstants.TRANSITION_ID_ERROR, new FailedLoginException());
        attributes.put(Credential.class.getName(), RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        context.setCurrentEvent(new Event(this, "eventId", attributes));
        assertNull(initPasswordChangeAction.execute(context));
        assertNotNull(WebUtils.getPasswordPolicyPattern(context));
        assertNotNull(WebUtils.getCredential(context));
    }
}
