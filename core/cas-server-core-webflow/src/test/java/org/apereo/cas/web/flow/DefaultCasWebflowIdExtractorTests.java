package org.apereo.cas.web.flow;

import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.UnknownTenantException;
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
@TestPropertySource(properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
class DefaultCasWebflowIdExtractorTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("casDefaultFlowIdExtractor")
    private CasWebflowIdExtractor casDefaultFlowIdExtractor;

    @Test
    void verifyFlowIdExtraction() {
        val request = new MockHttpServletRequest();
        request.setContextPath("/tenants/unknown/abc");
        assertThrows(UnknownTenantException.class, () -> casDefaultFlowIdExtractor.extract(request, request.getContextPath()));
        request.setContextPath("/tenants/shire/abc");
        val flowId = casDefaultFlowIdExtractor.extract(request, request.getContextPath());
        assertEquals("abc", flowId);
        assertNotNull(request.getAttribute(TenantDefinition.class.getName()));
    }
}
