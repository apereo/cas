package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SingleSignOnParticipationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
class SingleSignOnParticipationRequestTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .attributes(new HashMap<>(Map.of("hello", "world")))
            .build()
            .attribute("testkey", List.of("testvalue"));
        assertTrue(ssoRequest.containsAttribute("hello"));
        assertNotNull(ssoRequest.getAttributeValue("hello", String.class));
        assertTrue(ssoRequest.getRequestContext().isPresent());
        assertTrue(ssoRequest.getHttpServletRequest().isPresent());
        assertTrue(ssoRequest.isRequestingRenewAuthentication());
    }
}
