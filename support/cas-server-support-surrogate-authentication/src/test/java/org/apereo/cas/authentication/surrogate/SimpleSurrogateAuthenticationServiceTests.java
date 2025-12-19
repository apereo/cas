package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceSurrogatePolicy;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SimpleSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
class SimpleSurrogateAuthenticationServiceTests {

    @Nested
    @SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class DefaultTests extends BaseSurrogateAuthenticationServiceTests {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

        @Autowired
        private ConfigurableApplicationContext applicationContext;
        
        @Override
        public SurrogateAuthenticationService getService() {
            return new SimpleSurrogateAuthenticationService(
                CollectionUtils.wrap(
                    "casuser", CollectionUtils.wrapList("banderson"),
                    "casadmin", CollectionUtils.wrapList(SurrogateAuthenticationService.WILDCARD_ACCOUNT)
                ), servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        }
    }
    
    @Nested
    @SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.surrogate.core.principal-attribute-names=membership",
            "cas.authn.surrogate.core.principal-attribute-values=(ad|st|su).*"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class AttributeTests {

        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        protected ServicesManager servicesManager;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

        @Autowired
        private ConfigurableApplicationContext applicationContext;
        
        @Test
        void verifyOperation() throws Throwable {
            val surrogateService = new SimpleSurrogateAuthenticationService(Map.of(), servicesManager,
                casProperties, principalAccessStrategyEnforcer, applicationContext);
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
                Map.of("membership", List.of("faculty", "superadmin"),
                    SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true)));
            assertTrue(surrogateService.canImpersonate(UUID.randomUUID().toString(), principal, Optional.empty()));

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            val accessStrategy = new SurrogateRegisteredServiceAccessStrategy();
            accessStrategy.setSurrogateRequiredAttributes(Map.of("impersonation", Set.of("yes")));
            registeredService.setAccessStrategy(accessStrategy);
            servicesManager.save(registeredService);
            assertThrows(PrincipalException.class, () -> surrogateService.canImpersonate(UUID.randomUUID().toString(), principal,
                Optional.of(RegisteredServiceTestUtils.getService(registeredService.getServiceId()))));
            
            val registeredService2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
            registeredService2.setSurrogatePolicy(new DefaultRegisteredServiceSurrogatePolicy().setEnabled(false));
            servicesManager.save(registeredService2);
            assertFalse(surrogateService.canImpersonate(UUID.randomUUID().toString(), principal,
                Optional.of(RegisteredServiceTestUtils.getService(registeredService2.getServiceId()))));

        }
    }
}
