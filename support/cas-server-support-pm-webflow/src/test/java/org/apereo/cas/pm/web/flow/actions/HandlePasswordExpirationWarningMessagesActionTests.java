package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HandlePasswordExpirationWarningMessagesActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
class HandlePasswordExpirationWarningMessagesActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val exception = new PasswordExpiringWarningMessageDescriptor("About to expire", 10);
        val event = new Event(this, "success",
            new LocalAttributeMap<>(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, CollectionUtils.wrapList(exception)));
        context.setCurrentEvent(event);

        val action = new HandlePasswordExpirationWarningMessagesAction();
        action.execute(context);
        assertTrue(context.getFlowScope().get(HandlePasswordExpirationWarningMessagesAction.ATTRIBUTE_NAME_EXPIRATION_WARNING_FOUND, Boolean.class));
    }
}
