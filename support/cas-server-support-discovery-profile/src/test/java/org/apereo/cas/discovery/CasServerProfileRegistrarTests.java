package org.apereo.cas.discovery;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasDiscoveryProfileAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServerProfileRegistrarTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasDiscoveryProfileAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.ldap[0].principal-attribute-list=sn,cn"
    })
@Tag("Simple")
class CasServerProfileRegistrarTests {
    @Autowired
    @Qualifier(CasServerProfileRegistrar.BEAN_NAME)
    private CasServerProfileRegistrar casServerProfileRegistrar;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val profile = casServerProfileRegistrar.getProfile();
        assertNotNull(profile);
        assertNotNull(profile.getAvailableAttributes());
        assertNotNull(profile.getMultifactorAuthenticationProviderTypesSupported());
        assertNotNull(profile.getRegisteredServiceTypesSupported());
        assertNotNull(profile.getAvailableAuthenticationHandlers());
        assertNotNull(profile.getTicketTypesSupported());
    }
}
