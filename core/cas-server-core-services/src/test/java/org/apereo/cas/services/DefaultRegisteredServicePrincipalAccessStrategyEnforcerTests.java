package org.apereo.cas.services;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
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
 * This is {@link DefaultRegisteredServicePrincipalAccessStrategyEnforcerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class DefaultRegisteredServicePrincipalAccessStrategyEnforcerTests {
    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val authentication = RegisteredServiceTestUtils.getAuthentication();
        assertThrows(PrincipalException.class, () -> principalAccessStrategyEnforcer.authorize(
            RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                .registeredService(service)
                .principalId(authentication.getPrincipal().getId())
                .principalAttributes(CollectionUtils.merge(authentication.getAttributes(), authentication.getPrincipal().getAttributes()))
                .service(RegisteredServiceTestUtils.getService())
                .applicationContext(applicationContext)
                .build()));
    }
}
