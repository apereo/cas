package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Attributes")
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = "cas.authn.attribute-repository.default-attributes-to-release=cn,mail")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ReturnAllowedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllowedAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifySerializeAReturnAllowedAttributeReleasePolicyToJson() throws IOException {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("attributeOne");
        val policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        policyWritten.setPrincipalIdAttribute("principalId");
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyConsentable() {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("uid");
        allowedAttributes.add("cn");
        allowedAttributes.add("givenName");
        val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        val consentPolicy = new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"));
        policy.setConsentPolicy(consentPolicy);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val results = policy.getConsentableAttributes(principal,
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("givenName"));
    }

    @Test
    public void verifyDefaultAttributes() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setPrincipalIdAttribute("principalId");
        policy.postLoad();

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getUsernameAttributeProvider()).thenReturn(new RegisteredServiceUsernameAttributeProvider() {
            private static final long serialVersionUID = 6935950848419028873L;

            @Override
            public String resolveUsername(final Principal principal, final Service service, final RegisteredService registeredService) {
                return principal.getId();
            }
        });
        val attributes = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(3, attributes.size());
        assertTrue(attributes.containsKey("principalId"));
        assertTrue(attributes.containsKey("cn"));
        assertTrue(attributes.containsKey("mail"));
    }
}
