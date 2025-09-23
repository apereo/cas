package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.ws.idp.WSFederationClaims;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationClaimsReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("WSFederation")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
class WSFederationClaimsReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "WSFederationClaimsReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAttributeReleaseNone() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS", "givenName", "CAS User"));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyAttributeReleaseInlineGroovy() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.EMAIL_ADDRESS_2005.name(), "groovy { return attributes['cn'][0] + '@example.org' }"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", "casuser"));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
        assertEquals(List.of("casuser@example.org"), results.get(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
    }

    @Test
    void verifyAttributeReleaseScriptGroovy() throws Throwable {
        val file = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        val script = IOUtils.toString(new ClassPathResource("wsfed-attr.groovy").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.write(file, script, StandardCharsets.UTF_8);

        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.EMAIL_ADDRESS_2005.name(), "file:" + file.getCanonicalPath()));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", "casuser"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
        assertEquals(List.of("casuser@example.org"), results.get(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
    }

    @Test
    void verifyAttributeRelease() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn",
                WSFederationClaims.EMAIL_ADDRESS.name(), "email",
                WSFederationClaims.GROUP.name(), "unknown",
                WSFederationClaims.EMAIL_ADDRESS_2005.name(), "unknown"));
        assertFalse(policy.getAllowedAttributes().isEmpty());
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", "casuser", "email", "cas@example.org",
                WSFederationClaims.EMAIL_ADDRESS_2005.getUri(), "cas2005@example.org"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertSame(3, results.size());
        assertTrue(results.containsKey(WSFederationClaims.COMMON_NAME.getUri()));
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS.getUri()));
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
        
        val commonNameValue = results.get(WSFederationClaims.COMMON_NAME.getUri());
        assertEquals(CollectionUtils.wrapArrayList("casuser"), commonNameValue);
        val emailAddressValue = results.get(WSFederationClaims.EMAIL_ADDRESS.getUri());
        assertEquals(CollectionUtils.wrapArrayList("cas@example.org"), emailAddressValue);
    }
    
    @Test
    void verifySerializePolicyToJson() throws IOException {
        val policyWritten = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn", WSFederationClaims.EMAIL_ADDRESS.name(), "email"));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, WSFederationClaimsReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
