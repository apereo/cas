package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasWebflowIdExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.multitenancy.json.location=classpath:/tenants.json")
class DefaultCasWebflowIdExtractorTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("casDefaultFlowIdExtractor")
    private CasWebflowIdExtractor casDefaultFlowIdExtractor;

    @Test
    void verifyFlowIdExtraction() throws Exception {
        val request = new MockHttpServletRequest();
        var flowId = casDefaultFlowIdExtractor.extract(request, "tenants/unknown/abc");
        assertEquals("tenants/unknown/abc", flowId);
        flowId = casDefaultFlowIdExtractor.extract(request, "tenants/shire/abc");
        assertEquals("abc", flowId);
    }
}
