package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EduPersonTargetedIdAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata55"
})
class EduPersonTargetedIdAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "EduPersonTargetedIdAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyEduPersonTargetedId() throws Throwable {
        val filter = new EduPersonTargetedIdAttributeReleasePolicy();
        filter.setSalt("OqmG80fEKBQt");
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        val attributes = filter.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID));
        assertEquals(List.of("bhb1if0QzFdkKSS5xkcNCALXtGE="),
            attributes.get(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID));
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new EduPersonTargetedIdAttributeReleasePolicy();
        filter.setSalt("OqmG80fEKBQt");
        filter.setAttribute("something");
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, EduPersonTargetedIdAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }

    @Test
    void verifyEduPersonTargetedIdViaInCommon() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val filter = new InCommonRSAttributeReleasePolicy();
        filter.setOrder(1);
        val filter2 = new EduPersonTargetedIdAttributeReleasePolicy();
        filter2.setSalt("OqmG80fEKBQt");
        filter2.setOrder(0);
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(filter);
        chain.addPolicies(filter2);
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        registeredService.setAttributeReleasePolicy(chain);
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        val attributes = chain.getAttributes(releasePolicyContext);
        assertEquals(List.of("bhb1if0QzFdkKSS5xkcNCALXtGE="),
            attributes.get(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID));
    }

    @Test
    void verifyEduPersonTargetedIdDefinitions() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        policy.setSalt("OqmG80fEKBQt");
        policy.setUseUniformResourceName(true);

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        var definitions = policy.determineRequestedAttributeDefinitions(context);
        assertTrue(definitions.contains(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_URN_EDU_PERSON_TARGETED_ID));
        policy.setUseUniformResourceName(false);
        definitions = policy.determineRequestedAttributeDefinitions(context);
        assertTrue(definitions.contains(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID));
    }
}
