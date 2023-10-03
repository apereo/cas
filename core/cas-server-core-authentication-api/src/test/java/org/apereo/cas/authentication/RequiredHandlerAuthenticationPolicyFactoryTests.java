package org.apereo.cas.authentication;

import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RequiredHandlerAuthenticationPolicyFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
class RequiredHandlerAuthenticationPolicyFactoryTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new RequiredHandlerAuthenticationPolicyFactory();
        val policy = input.createPolicy(CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(policy.getContext());
    }

}
