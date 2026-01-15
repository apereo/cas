package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PatternMatchingEntityIdAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLAttributes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml3382")
class PatternMatchingEntityIdAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    @BeforeEach
    void setup() {
        servicesManager.deleteAll();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
    }

    @Test
    @Order(1)
    void verifyPatternDoesNotMatch() throws Throwable {
        val filter = new PatternMatchingEntityIdAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = filter.getAttributes(context);
        assertTrue(attributes.isEmpty());
    }

    @Test
    @Order(2)
    void verifyPatternDoesNotMatchAndReversed() throws Throwable {
        val filter = new PatternMatchingEntityIdAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("cn"));
        filter.setEntityIds("helloworld");
        filter.setReverseMatch(true);
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = filter.getAttributes(context);
        assertFalse(attributes.isEmpty());
    }

    @Test
    @Order(3)
    void verifyPatternDoesMatch() throws Throwable {
        val filter = new PatternMatchingEntityIdAttributeReleasePolicy();
        filter.setEntityIds("https://sp.+");
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "givenName", "displayName"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = filter.getAttributes(context);
        assertFalse(attributes.isEmpty());
    }
}
