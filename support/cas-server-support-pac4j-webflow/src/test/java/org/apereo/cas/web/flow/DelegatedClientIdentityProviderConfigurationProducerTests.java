package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DelegatedClientIdentityProviderConfigurationProducerTests {
    @Autowired
    @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
    private DelegatedClientIdentityProviderConfigurationProducer producer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperationAutoRedirect() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(List.of("CasClient"));
        policy.setPermitUndefined(false);

        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated2.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated2.example.org"));
        assertNotNull(producer.produce(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(context));
    }

    @Test
    void verifyOperationPrimaryProvider() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(List.of("CasClient"));
        policy.setPermitUndefined(false);
        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated.example.org"));
        assertNotNull(producer.produce(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(context));
    }
}
