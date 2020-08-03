package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EduPersonTargetedIdAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.entityId=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.location=${#systemProperties['java.io.tmpdir']}/idp-metadata"
})
public class EduPersonTargetedIdAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "EduPersonTargetedIdAttributeReleasePolicyTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyEduPersonTargetedId() {
        val filter = new EduPersonTargetedIdAttributeReleasePolicy();
        filter.setSalt("OqmG80fEKBQt");
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val attributes = filter.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"), registeredService);
        assertTrue(attributes.containsKey(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID));
        assertTrue(attributes.get(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID).equals(List.of("bhb1if0QzFdkKSS5xkcNCALXtGE=")));
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        val filter = new EduPersonTargetedIdAttributeReleasePolicy();
        filter.setSalt("OqmG80fEKBQt");
        filter.setAttribute("something");
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, EduPersonTargetedIdAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }

    @Test
    public void verifyEduPersonTargetedIdViaInCommon() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();

        val filter = new InCommonRSAttributeReleasePolicy();
        filter.setOrder(1);

        val filter2 = new EduPersonTargetedIdAttributeReleasePolicy();
        filter2.setSalt("OqmG80fEKBQt");
        filter2.setOrder(0);

        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(filter);
        chain.addPolicies(filter2);
        registeredService.setAttributeReleasePolicy(chain);

        val attributes = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"), registeredService);

        assertTrue(attributes.get(EduPersonTargetedIdAttributeReleasePolicy.ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID)
            .equals(List.of("bhb1if0QzFdkKSS5xkcNCALXtGE=")));
    }
}
