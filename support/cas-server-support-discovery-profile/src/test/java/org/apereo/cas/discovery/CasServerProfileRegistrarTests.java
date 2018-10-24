package org.apereo.cas.discovery;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasDiscoveryProfileConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.support.pac4j.config.support.authentication.Pac4jAuthenticationEventExecutionPlanConfiguration;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link CasServerProfileRegistrarTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    Pac4jAuthenticationEventExecutionPlanConfiguration.class,
    CasDiscoveryProfileConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
})
@DirtiesContext
public class CasServerProfileRegistrarTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("casServerProfileRegistrar")
    private CasServerProfileRegistrar casServerProfileRegistrar;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyAction() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val profile = casServerProfileRegistrar.getProfile();
        assertNotNull(profile);
        assertNotNull(profile.getAvailableAttributes());
        assertNotNull(profile.getDelegatedClientTypes());
        assertNotNull(profile.getDelegatedClientTypesSupported());
        assertNotNull(profile.getMultifactorAuthenticationProviderTypes());
        assertNotNull(profile.getMultifactorAuthenticationProviderTypesSupported());
        assertNotNull(profile.getRegisteredServiceTypes());
        assertNotNull(profile.getRegisteredServiceTypesSupported());
    }
}
