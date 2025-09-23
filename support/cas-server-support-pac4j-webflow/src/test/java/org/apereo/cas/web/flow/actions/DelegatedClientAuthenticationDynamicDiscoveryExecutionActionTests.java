package org.apereo.cas.web.flow.actions;

import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC",
        "cas.authn.pac4j.core.discovery-selection.json.location=classpath:delegated-discovery.json"
    })
@Tag("Delegation")
class DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
    private Action delegatedAuthenticationDiscoveryAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperationWithClient() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        context.setParameter("username", "cas@example.org");

        val result = delegatedAuthenticationDiscoveryAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
        assertTrue(context.getRequestScope()
            .contains(DelegatedAuthenticationDynamicDiscoveryProviderLocator.REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL, String.class));
    }

    @Test
    void verifyOperationWithoutClient() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("username", "cas@test.org");
        context.withUserAgent();
        val result = delegatedAuthenticationDiscoveryAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }
}
