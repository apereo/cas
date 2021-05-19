package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SingleSignOnParticipationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
public class SingleSignOnParticipationRequestTests {
    @Test
    public void verifyOperation() {
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(new MockRequestContext())
            .attributes(Map.of("hello", "world"))
            .build();
        assertTrue(ssoRequest.containsAttribute("hello"));
        assertNotNull(ssoRequest.getAttributeValue("hello", String.class));
        assertTrue(ssoRequest.getRequestContext().isPresent());
        assertTrue(ssoRequest.getHttpServletRequest().isPresent());
    }
}
