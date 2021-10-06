package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovySamlRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata4"
})
public class GroovySamlRegisteredServiceAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
        defaultSamlRegisteredServiceCachingMetadataResolver.invalidate();
    }

    @Test
    public void verifyUnknownScript() {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:unknown-123456.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(attributes.isEmpty());
    }


    @Test
    public void verifyScriptReleasesSamlAttributes() {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertFalse(attributes.isEmpty());
    }

    @Test
    public void verifyScriptReleasesSamlAttributesWithEntityId() {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        request.removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);
        
        val service = RegisteredServiceTestUtils.getService();
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(registeredService.getServiceId()));
        val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(), service, registeredService);
        assertFalse(attributes.isEmpty());
    }

    @Test
    public void verifyScriptReleasesSamlAttributesWithProviderId() {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        request.removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

        val service = RegisteredServiceTestUtils.getService();
        service.getAttributes().put(SamlIdPConstants.PROVIDER_ID, List.of(registeredService.getServiceId()));
        val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(), service, registeredService);
        assertFalse(attributes.isEmpty());
    }

    @Test
    public void verifyScriptReleasesSamlAttributesWithSamlRequest() throws Exception {
        val filter = new GroovySamlRegisteredServiceAttributeReleasePolicy();
        filter.setGroovyScript("classpath:saml-groovy-attrs.groovy");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        request.removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, registeredService);
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            val service = RegisteredServiceTestUtils.getService();
            service.getAttributes().put(SamlProtocolConstants.PARAMETER_SAML_REQUEST, List.of(samlRequest));
            val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal(), service, registeredService);
            assertFalse(attributes.isEmpty());
        }
    }
}
