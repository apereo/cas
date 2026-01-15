package org.apereo.cas.adaptors.trusted.authentication.principal;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRemoteRequestPrincipalAttributesExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Authentication")
class DefaultRemoteRequestPrincipalAttributesExtractorTests {

    @Test
    void verifyOperation() {
        val extractor = getExtractor(Map.of("AJP_(.+)", "^@.+_(.+);"));
        val request = new MockHttpServletRequest();
        request.addHeader("AJP_CAS", "@SSO_OSS;");
        request.addHeader("AJP_SYS", "@SYS_APEREO;");
        val results = extractor.getAttributes(request);
        assertEquals(2, results.size());
        assertTrue(results.containsKey("SYS"));
        assertTrue(results.containsKey("CAS"));
        assertEquals(results.get("SYS"), List.of("APEREO"));
        assertEquals(results.get("CAS"), List.of("OSS"));
    }

    private static RemoteRequestPrincipalAttributesExtractor getExtractor(final Map<String, String> patterns) {
        return new DefaultRemoteRequestPrincipalAttributesExtractor(patterns);
    }
}
