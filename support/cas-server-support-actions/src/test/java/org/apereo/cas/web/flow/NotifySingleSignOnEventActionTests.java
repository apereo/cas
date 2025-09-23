package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NotifySingleSignOnEventActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("WebflowActions")
@EnabledIfListeningOnPort(port = 25000)
@ExtendWith(CasTestExtension.class)
class NotifySingleSignOnEventActionTests {

    abstract static class BaseTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_NOTIFY_SINGLE_SIGNON_EVENT)
        protected Action action;
    }

    @Nested
    class UndefinedProviders extends BaseTests {
        @Test
        void verifyUndefinedSettings() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val result = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, result.getId());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.sso.sms.text=User ${#principal.id} verified",
        "cas.sso.sms.from=3477563421",

        "cas.sso.mail.from=admin@example.org",
        "cas.sso.mail.subject=Sample Subject",
        "cas.sso.mail.text=User ${#principal.attributes['name'].first} with id as ${principalId} verified"
    })
    class WithProviders extends BaseTests {
        @Test
        void verifyNoTicketInContext() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val result = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, result.getId());
        }

        @Test
        void verifyWithTicketInContext() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val tgt = new MockTicketGrantingTicket("casuser",
                Map.of(
                    "email", List.of("casuser@example.org"),
                    "mail", List.of("casuser@apereo.org"),
                    "phone", List.of("3477563444"),
                    "phoneNumber", List.of("6477563444"),
                    "name", List.of("ApereoCAS"))
            );
            getTicketRegistry().addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            val result = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        }
    }
}
