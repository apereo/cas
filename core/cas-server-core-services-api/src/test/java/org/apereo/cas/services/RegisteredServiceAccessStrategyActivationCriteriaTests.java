package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class RegisteredServiceAccessStrategyActivationCriteriaTests {
    @Test
    public void verifyAlways() throws Exception {
        val criteria = RegisteredServiceAccessStrategyActivationCriteria.always();
        assertTrue(criteria.shouldActivate(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser").build()));
        assertTrue(criteria.isAllowIfInactive());
        assertEquals(0, criteria.getOrder());
    }

    @Test
    public void verifyNever() throws Exception {
        val criteria = RegisteredServiceAccessStrategyActivationCriteria.never();
        assertFalse(criteria.shouldActivate(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser").build()));
        assertTrue(criteria.isAllowIfInactive());
        assertEquals(0, criteria.getOrder());
    }
}
