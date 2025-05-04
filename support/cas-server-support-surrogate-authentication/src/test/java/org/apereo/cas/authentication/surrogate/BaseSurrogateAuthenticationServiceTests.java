package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseSurrogateAuthenticationServiceTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseSurrogateAuthenticationServiceTests {
    public static final String BANDERSON = "banderson";

    public static final String ADMIN = "casadmin";

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    public abstract SurrogateAuthenticationService getService();

    @Test
    void verifyUserAllowedToProxy() throws Throwable {
        assertFalse(getService().getImpersonationAccounts(getTestUser(), Optional.empty()).isEmpty());
    }

    @Test
    void verifyUserNotAllowedToProxy() throws Throwable {
        assertTrue(getService().getImpersonationAccounts("unknown-user", Optional.empty()).isEmpty());
    }

    @Test
    void verifyProxying() throws Throwable {
        val service = Optional.of(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.get().getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);
        
        val surrogateService = getService();
        assertTrue(surrogateService.canImpersonate(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(getTestUser()), service));
        assertTrue(surrogateService.canImpersonate(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(BANDERSON), service));
        assertFalse(surrogateService.canImpersonate("XXXX", CoreAuthenticationTestUtils.getPrincipal(getTestUser()), service));
        assertFalse(surrogateService.canImpersonate(getTestUser(), CoreAuthenticationTestUtils.getPrincipal(BANDERSON), service));
    }

    @Test
    void verifyWildcard() throws Throwable {
        val service = Optional.of(RegisteredServiceTestUtils.getService(UUID.randomUUID().toString()));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.get().getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);
        val admin = CoreAuthenticationTestUtils.getPrincipal(getAdminUser());
        assertTrue(getService().canImpersonate(BANDERSON, admin, service));
        assertTrue(getService().isWildcardedAccount(BANDERSON, admin, service));
    }

    public String getAdminUser() {
        return ADMIN;
    }

    public String getTestUser() {
        return "casuser";
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasSurrogateAuthenticationAutoConfiguration.class,
        CasCoreRestAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasPersonDirectoryTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
