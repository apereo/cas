package org.apereo.cas.integration.pac4j;

import module java.base;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientNameExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Delegation")
class DelegatedClientNameExtractorTests {

    @Test
    void verifyOperation() {
        val extractor = DelegatedClientNameExtractor.fromHttpRequest();
        val request = new MockHttpServletRequest();
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        assertTrue(extractor.extract(webContext).isPresent());
    }

    @Test
    void verifyRelayState() {
        val extractor = DelegatedClientNameExtractor.fromHttpRequest();
        val request = new MockHttpServletRequest();
        request.addParameter("RelayState", "https://example.org?client_name=CasClient");
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        assertTrue(extractor.extract(webContext).isPresent());
    }

    @Test
    void verifyBadRelayState() {
        val extractor = DelegatedClientNameExtractor.fromHttpRequest();
        val request = new MockHttpServletRequest();
        request.addParameter("RelayState", "https://....");
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        assertTrue(extractor.extract(webContext).isEmpty());
    }
}
