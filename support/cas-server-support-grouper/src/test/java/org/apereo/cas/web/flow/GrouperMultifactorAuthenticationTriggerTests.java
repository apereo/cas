package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GrouperMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Grouper")
class GrouperMultifactorAuthenticationTriggerTests {

    @Test
    void verifyOperationFails() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val casProperties = new CasConfigurationProperties();
        val trigger = new GrouperMultifactorAuthenticationTrigger(casProperties,
            mock(MultifactorAuthenticationProviderResolver.class), mock(GrouperFacade.class),
            applicationContext);

        assertTrue(trigger.isActivated(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getRegisteredService(),
            new MockHttpServletRequest(), new MockHttpServletResponse(), RegisteredServiceTestUtils.getService()).isEmpty());

        casProperties.getAuthn().getMfa().getTriggers().getGrouper().setGrouperGroupField("name");
        assertTrue(trigger.isActivated(RegisteredServiceTestUtils.getAuthentication(),
            null,
            new MockHttpServletRequest(), new MockHttpServletResponse(), RegisteredServiceTestUtils.getService()).isEmpty());

        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(RegisteredServiceTestUtils.getAuthentication(),
                RegisteredServiceTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), RegisteredServiceTestUtils.getService()));
    }
}
