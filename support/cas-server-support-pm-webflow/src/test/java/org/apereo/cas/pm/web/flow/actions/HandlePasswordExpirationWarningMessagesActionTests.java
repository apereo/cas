package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link HandlePasswordExpirationWarningMessagesActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HandlePasswordExpirationWarningMessagesActionTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val exception = new PasswordExpiringWarningMessageDescriptor("About to expire", 10);
        val event = new Event(this, "success",
            new LocalAttributeMap<>(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, CollectionUtils.wrapList(exception)));
        context.setCurrentEvent(event);

        val action = new HandlePasswordExpirationWarningMessagesAction();
        action.execute(context);
        assertTrue(context.getFlowScope().get(HandlePasswordExpirationWarningMessagesAction.ATTRIBUTE_NAME_EXPIRATION_WARNING_FOUND, Boolean.class));
    }
}
