package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class RegisteredServiceAccessStrategyActivationCriteriaTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyAlways() throws Throwable {
        val criteria = RegisteredServiceAccessStrategyActivationCriteria.always();
        assertTrue(criteria.shouldActivate(RegisteredServiceAccessStrategyRequest.builder()
            .applicationContext(applicationContext)
            .principalId("casuser").build()));
        assertTrue(criteria.isAllowIfInactive());
        assertEquals(0, criteria.getOrder());
    }

    @Test
    void verifyNever() throws Throwable {
        val criteria = RegisteredServiceAccessStrategyActivationCriteria.never();
        assertFalse(criteria.shouldActivate(RegisteredServiceAccessStrategyRequest.builder()
            .applicationContext(applicationContext)
            .principalId("casuser").build()));
        assertTrue(criteria.isAllowIfInactive());
        assertEquals(0, criteria.getOrder());
    }
}
