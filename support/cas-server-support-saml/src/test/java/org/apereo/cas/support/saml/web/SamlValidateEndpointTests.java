package org.apereo.cas.support.saml.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasSamlAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlValidateEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    CasCoreSamlAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasSamlAutoConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.samlValidate.access=UNRESTRICTED"
    })
@Tag("SAML1")
@ExtendWith(CasTestExtension.class)
class SamlValidateEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("samlValidateEndpoint")
    private SamlValidateEndpoint samlValidateEndpoint;

    @Autowired
    @Qualifier("samlProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<Void> samlProtocolEndpointConfigurer;

    @Test
    void verifyEndpoints() {
        assertFalse(samlProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }

    @Test
    void verifyOperation() throws Throwable {
        val service = CoreAuthenticationTestUtils.getService();
        assertNotNull(samlValidateEndpoint);
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE);
        val results = samlValidateEndpoint.handle(request, "sample", "sample", service.getId());
        assertNotNull(results);
    }

    @Test
    void verifyWithoutPassword() throws Throwable {
        val service = CoreAuthenticationTestUtils.getService();
        assertNotNull(samlValidateEndpoint);
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        val results = samlValidateEndpoint.handle(request, "sample", null, service.getId());
        assertNotNull(results);
    }
}
