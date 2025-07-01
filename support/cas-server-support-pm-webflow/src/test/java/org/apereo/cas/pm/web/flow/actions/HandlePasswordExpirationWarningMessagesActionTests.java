package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HandlePasswordExpirationWarningMessagesActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BasePasswordManagementActionTests.SharedTestConfiguration.class,
    properties = "cas.authn.pm.core.enabled=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class HandlePasswordExpirationWarningMessagesActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS)
    private Action action;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val exception = new PasswordExpiringWarningMessageDescriptor("About to expire", 10);
        val event = new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            new LocalAttributeMap<>(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, List.of(exception)));
        context.setCurrentEvent(event);
        action.execute(context);
        assertTrue(context.getFlowScope().get(HandlePasswordExpirationWarningMessagesAction.ATTRIBUTE_NAME_EXPIRATION_WARNING_FOUND, Boolean.class));
    }
}
