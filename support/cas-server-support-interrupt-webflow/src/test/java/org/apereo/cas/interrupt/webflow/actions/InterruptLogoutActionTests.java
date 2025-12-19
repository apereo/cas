package org.apereo.cas.interrupt.webflow.actions;

import module java.base;
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
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseInterruptFlowActionTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class InterruptLogoutActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INTERRUPT_LOGOUT)
    private Action action;

    @Test
    void verifyLogoutInterrupt() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertNull(action.execute(context));
        val cookie = context.getHttpServletResponse().getCookie(casProperties.getInterrupt().getCookie().getName());
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }
}
