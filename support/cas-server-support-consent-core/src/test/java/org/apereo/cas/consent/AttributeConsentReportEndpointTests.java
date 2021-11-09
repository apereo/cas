package org.apereo.cas.consent;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeConsentReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.attributeConsent.enabled=true")
@Tag("ActuatorEndpoint")
@Import(CasConsentCoreConfiguration.class)
public class AttributeConsentReportEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("attributeConsentReportEndpoint")
    private AttributeConsentReportEndpoint attributeConsentReportEndpoint;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier("consentDecisionBuilder")
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    public void verifyOperation() {
        val uid = UUID.randomUUID().toString();
        val desc = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), uid,
            CoreAuthenticationTestUtils.getAttributes());
        consentRepository.storeConsentDecision(desc);

        var results = attributeConsentReportEndpoint.consentDecisions(uid);
        assertFalse(results.isEmpty());

        val entity = attributeConsentReportEndpoint.export();
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        assertTrue(attributeConsentReportEndpoint.revokeConsents(desc.getPrincipal(), desc.getId()));
        results = attributeConsentReportEndpoint.consentDecisions(uid);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyImportOperation() throws Exception {
        val uid = UUID.randomUUID().toString();
        val toSave = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), uid,
            CoreAuthenticationTestUtils.getAttributes());
        val request = new MockHttpServletRequest();
        val content = MAPPER.writeValueAsString(toSave);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, attributeConsentReportEndpoint.importAccount(request));
    }
}
