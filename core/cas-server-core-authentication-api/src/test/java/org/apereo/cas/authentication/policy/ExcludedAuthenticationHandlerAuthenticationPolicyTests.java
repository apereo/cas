package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExcludedAuthenticationHandlerAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
class ExcludedAuthenticationHandlerAuthenticationPolicyTests {

    @Test
    void verifyOperation() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val input = new ExcludedAuthenticationHandlerAuthenticationPolicy(Set.of("Hello"), true);
        assertTrue(input.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication(), Set.of(), applicationContext).isSuccess());

    }

}
