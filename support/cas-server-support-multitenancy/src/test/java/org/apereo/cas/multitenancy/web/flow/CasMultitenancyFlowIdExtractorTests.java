package org.apereo.cas.multitenancy.web.flow;

import org.apereo.cas.config.CasMultitenancyAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.CasFlowIdExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMultitenancyFlowIdExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasMultitenancyAutoConfiguration.class,
    properties = "cas.multitenancy.json.location=classpath:/tenants.json")
class CasMultitenancyFlowIdExtractorTests {
    @Autowired
    @Qualifier("casMultitenancyFlowIdExtractor")
    private CasFlowIdExtractor casMultitenancyFlowIdExtractor;

    @Test
    void verifyFlowIdExtraction() throws Exception {
        val request = new MockHttpServletRequest();
        var flowId = casMultitenancyFlowIdExtractor.extract(request, "tenants/unknown/abc");
        assertEquals("tenants/unknown/abc", flowId);
        flowId = casMultitenancyFlowIdExtractor.extract(request, "tenants/b9584c42/abc");
        assertEquals("abc", flowId);
    }
}
