package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import java.util.UUID;

/**
 * This is {@link BaseYubiKeyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public abstract class BaseYubiKeyActionTests {
    protected ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, new YubiKeyMultifactorAuthenticationProvider());
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
    }
}
