package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class
})
@DirtiesContext
public class JpaServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("jpaServiceRegistry")
    private ServiceRegistry serviceRegistry;

    public JpaServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(
            RegexRegisteredService.class,
            OAuthRegisteredService.class,
            OidcRegisteredService.class,
            SamlRegisteredService.class,
            WSFederationRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
