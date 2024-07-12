package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RequiredAttributesAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("AuthenticationPolicy")
class RequiredAttributesAuthenticationPolicyTests {
    @Test
    void verifyAuthnAttributes() throws Throwable {
        val input = new RequiredAttributesAuthenticationPolicy(Map.of(
            "givenName", "\\w+[0-9]", "name", "[0-9]-\\w+-cas"));
        val authn = CoreAuthenticationTestUtils.getAuthentication(
            Map.of("givenName", List.of("cas", "cas-88"), "name", List.of("1-hello-cas")));
        assertTrue(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }

    @Test
    void verifyPrincipalAttributes() throws Throwable {
        val input = new RequiredAttributesAuthenticationPolicy(Map.of(
            "givenName", "\\w+[0-9]", "name", "[0-9]-\\w+-cas"));
        val authn = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(Map.of("givenName", List.of("cas", "cas-88"), "name", List.of("1-hello-cas"))));
        assertTrue(input.isSatisfiedBy(authn, mock(ConfigurableApplicationContext.class)).isSuccess());
    }
}
