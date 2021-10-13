package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
public class DuoSecurityHealthIndicatorTests {

    @Test
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val props = new DuoSecurityMultifactorAuthenticationProperties()
            .setDuoApiHost("https://api.duosecurity.com");
        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.ping()).thenReturn(true);
        when(duoService.getProperties()).thenReturn(props);

        val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(bean.getDuoAuthenticationService()).thenReturn(duoService);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bean, "duoProvider");

        val indicator = new DuoSecurityHealthIndicator(applicationContext);
        val health = indicator.health();
        assertNotNull(health);
        assertEquals(health.getStatus(), Status.UP);
        assertTrue(health.getDetails().containsKey("duoApiHost"));
    }
}
