package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FinalizeInterruptFlowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseInterruptFlowActionTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class FinalizeInterruptFlowActionTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FINALIZE_INTERRUPT)
    private Action action;

    @Test
    void verifyFinalizedInterruptBlocked() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val interrupt = InterruptResponse.interrupt();
        interrupt.setBlock(true);

        InterruptUtils.putInterruptIn(context, interrupt);
        WebUtils.putRegisteredService(context, CoreAuthenticationTestUtils.getRegisteredService());

        assertThrows(UnauthorizedServiceException.class, () -> action.execute(context));
    }

    @Test
    void verifyFinalizedInterruptBlockedUnauthzUrl() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val interrupt = InterruptResponse.interrupt();
        interrupt.setBlock(true);

        InterruptUtils.putInterruptIn(context, interrupt);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new DefaultRegisteredServiceAccessStrategy(true, true);
        strategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);

        val event = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
        assertTrue(context.getMockExternalContext().isResponseComplete());
        assertNotNull(context.getMockExternalContext().getExternalRedirectUrl());
    }

    @Test
    void verifyFinalizedInterruptNonBlocked() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent();
        val interrupt = InterruptResponse.interrupt();

        InterruptUtils.putInterruptIn(context, interrupt);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, CoreAuthenticationTestUtils.getRegisteredService());

        val event = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.containsAttribute(InterruptTrackingEngine.AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT));
    }
}
