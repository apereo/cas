package org.apereo.cas.authentication;

import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.services.ServiceContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptAnyAuthenticationPolicyFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationPolicy")
public class AcceptAnyAuthenticationPolicyFactoryTests {
    @Test
    public void verifyOperation() {
        val input = new AcceptAnyAuthenticationPolicyFactory();
        val policy = input.createPolicy(new ServiceContext(CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService()));
        assertNotNull(policy.getContext());
    }

}
