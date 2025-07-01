package org.apereo.cas.web.flow.actions;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationGenerateClientsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
class DelegatedAuthenticationGenerateClientsActionTests {

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.pac4j.core.discovery-selection.selection-type=MENU")
    @Nested
    @ExtendWith(CasTestExtension.class)
    class MenuSelectionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS)
        private Action delegatedAuthenticationCreateClientsAction;

        @Autowired
        private ConfigurableApplicationContext applicationContext;
        
        @Test
        void verifyAuthnFailureProduces() throws Throwable {
            val context2 = getMockRequestContext(applicationContext);
            WebUtils.getHttpServletResponseFromExternalWebflowContext(context2).setStatus(HttpStatus.UNAUTHORIZED.value());
            assertDoesNotThrow(() -> delegatedAuthenticationCreateClientsAction.execute(context2));
            assertFalse(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context2).isEmpty());
        }

        @Test
        void verifyOperation() throws Throwable {
            val context1 = getMockRequestContext(applicationContext);
            val result = delegatedAuthenticationCreateClientsAction.execute(context1);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertFalse(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context1).isEmpty());
            assertFalse(DelegationWebflowUtils.isDelegatedAuthenticationDynamicProviderSelection(context1));
            assertEquals(HttpStatus.FOUND.value(),
                WebUtils.getHttpServletResponseFromExternalWebflowContext(context1).getStatus());
        }
    }

    private static RequestContext getMockRequestContext(final ConfigurableApplicationContext applicationContext) throws Exception {
        return MockRequestContext.create(applicationContext).withUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64");
    }

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC")
    @Nested
    @ExtendWith(CasTestExtension.class)
    class DynamicSelectionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS)
        private Action delegatedAuthenticationCreateClientsAction;

        @Autowired
        private ConfigurableApplicationContext applicationContext;
        
        @Test
        void verifyOperation() throws Throwable {
            val context = getMockRequestContext(applicationContext);

            val result = delegatedAuthenticationCreateClientsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertTrue(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context).isEmpty());
            assertTrue(DelegationWebflowUtils.isDelegatedAuthenticationDynamicProviderSelection(context));
        }
    }
}
