package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@Tag("Simple")
public class WSFederationClaimsReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "WSFederationClaimsReleasePolicyTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAttributeReleaseNone() {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS", "givenName", "CAS User"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyAttributeReleaseInlineGroovy() {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.EMAIL_ADDRESS_2005.name(), "groovy { return attributes['cn'][0] + '@example.org' }"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", "casuser"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
        assertEquals(results.get(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()), List.of("casuser@example.org"));
    }

    @Test
    public void verifyAttributeReleaseScriptGroovy() throws Exception {
        val file = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        val script = IOUtils.toString(new ClassPathResource("wsfed-attr.groovy").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.write(file, script, StandardCharsets.UTF_8);
        
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.EMAIL_ADDRESS_2005.name(), "file:" + file.getCanonicalPath()));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", "casuser"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()));
        assertEquals(results.get(WSFederationClaims.EMAIL_ADDRESS_2005.getUri()), List.of("casuser@example.org"));
    }

    @Test
    public void verifyAttributeRelease() {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn", WSFederationClaims.EMAIL_ADDRESS.name(), "email"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", "casuser", "email", "cas@example.org"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertSame(2, results.size());
        assertTrue(results.containsKey(WSFederationClaims.COMMON_NAME.getUri()));
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS.getUri()));
        val commonNameValue = results.get(WSFederationClaims.COMMON_NAME.getUri());
        assertEquals(CollectionUtils.wrapArrayList("casuser"), commonNameValue);
        val emailAddressValue = results.get(WSFederationClaims.EMAIL_ADDRESS.getUri());
        assertEquals(CollectionUtils.wrapArrayList("cas@example.org"), emailAddressValue);
    }

    @Test
    public void verifySerializePolicyToJson() throws IOException {
        val policyWritten = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn", WSFederationClaims.EMAIL_ADDRESS.name(), "email"));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, WSFederationClaimsReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
