package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorProviderSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
class MultifactorProviderSelectionActionTests {

    @Nested
    class ProceedTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_COMPOSITE_SELECTION)
        private Action action;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setCurrentEvent(new Event(this, "eventId"));
            val result = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, result.getId());
        }
    }

    @Nested
    @Import(SelectTests.MultifactorTestConfiguration.class)
    class SelectTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_COMPOSITE_SELECTION)
        private Action action;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setCurrentEvent(new Event(this, "eventId"));
            val result = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, result.getId());
        }

        @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
        public static class MultifactorTestConfiguration {
            @Bean
            public MultifactorProviderSelectionCriteria multifactorProviderSelectionCriteria() {
                return MultifactorProviderSelectionCriteria.select();
            }
        }
    }
}
