package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityUserAccountStatusEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ActuatorEndpoint")
public class DuoSecurityUserAccountStatusEndpointTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyStatusOperation() {
        assertEquals(DuoSecurityUserAccountStatus.AUTH, DuoSecurityUserAccountStatus.from("active"));
        assertEquals(DuoSecurityUserAccountStatus.ALLOW, DuoSecurityUserAccountStatus.from("bypass"));
        assertEquals(DuoSecurityUserAccountStatus.DENY, DuoSecurityUserAccountStatus.from("disabled"));
        assertEquals(DuoSecurityUserAccountStatus.DENY, DuoSecurityUserAccountStatus.from("locked"));
    }

    @Test
    public void verifyOperation() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val account = new DuoSecurityUserAccount("casuser");
        account.setMessage("User is valid");
        account.setStatus(DuoSecurityUserAccountStatus.AUTH);

        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.ping()).thenReturn(true);
        val props = new DuoSecurityMultifactorAuthenticationProperties()
            .setDuoApiHost("https://api.duosecurity.com");
        when(duoService.getProperties()).thenReturn(props);
        when(duoService.getUserAccount(eq("casuser"))).thenReturn(account);

        val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(bean.getDuoAuthenticationService()).thenReturn(duoService);
        when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bean, "duoProvider");

        val indicator = new DuoSecurityUserAccountStatusEndpoint(casProperties, this.applicationContext);
        val result = indicator.fetchAccountStatus("casuser", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertNotNull(result);
        assertTrue(result.containsKey(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER));
    }
}
