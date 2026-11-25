package org.apereo.cas.support.wsfederation;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.wsfederation.authentication.crypto.WsFederationCertificateProvider;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import lombok.Setter;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.xmlsec.signature.Signature;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link WsFederationHelper}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Setter
@Tag("WSFederation")
class WsFederationHelperTests extends AbstractWsFederationTests {

    @Test
    void verifyEncryptedToken() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val wresult = IOUtils.toString(new ClassPathResource("encryptedToken.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val result = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), service);
        assertNotNull(result.getKey());
    }

    @Test
    void verifyParseTokenString() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val result = wsFederationHelper.buildAndVerifyAssertion(token, wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        assertNotNull(result, "testParseTokenString() - Not null");
    }

    @Test
    void verifyCreateCredentialFromToken() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token, wsFederationConfigurations.toList(),
            RegisteredServiceTestUtils.getService());
        val expResult = new WsFederationCredential();
        expResult.setIssuedOn(ZonedDateTime.parse("2014-02-26T22:51:16.504Z"));
        expResult.setNotBefore(ZonedDateTime.parse("2014-02-26T22:51:16.474Z"));
        expResult.setNotOnOrAfter(ZonedDateTime.parse("2014-02-26T23:51:16.474Z"));
        expResult.setIssuer("http://adfs.example.com/adfs/services/trust");
        expResult.setAudience("urn:federation:cas");
        expResult.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");

        val result = wsFederationHelper.createCredentialFromToken(assertion.getKey());
        assertNotNull(result);
        assertEquals(expResult.getIssuedOn(), result.getIssuedOn());
        assertEquals(expResult.getNotBefore(), result.getNotBefore());
        assertEquals(expResult.getNotOnOrAfter(), result.getNotOnOrAfter());
        assertEquals(expResult.getIssuer(), result.getIssuer());
        assertEquals(expResult.getAudience(), result.getAudience());
        assertEquals(expResult.getId(), result.getId());
    }

    @Test
    void verifyWsTrustTokenResponse() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("wsTrustTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        assertNotNull(token);
        val result = wsFederationHelper.buildAndVerifyAssertion(token, wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        assertEquals("_8B4C0D2E6A0F4E11B9F6A2C3D4E5F601", result.getKey().getID());
        assertEquals(wsFederationConfigurations.toList().getFirst().getId(), result.getValue().getId());
        val attributeValue = result.getKey().getAttributeStatements().getFirst().getAttributes().getFirst().getAttributeValues().getFirst();
        assertInstanceOf(XSString.class, attributeValue);

        val credential = wsFederationHelper.createCredentialFromToken(result.getKey());
        assertEquals(ISSUER, credential.getIssuer());
        assertEquals(AUDIENCE, credential.getAudience());
        assertEquals(List.of("casuser"), credential.getAttributes().get("upn"));
        assertEquals(List.of("casuser@example.org"), credential.getAttributes().get("emailaddress"));
        assertFalse(wsFederationHelper.validateSignature(result));
    }

    @Test
    void verifyWsTrustTokenResponseWithoutToken() {
        val wresult = "<RequestSecurityTokenResponseCollection xmlns=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">"
            + "<RequestSecurityTokenResponse><RequestedSecurityToken/></RequestSecurityTokenResponse>"
            + "</RequestSecurityTokenResponseCollection>";
        assertNull(wsFederationHelper.getRequestSecurityTokenFromResult(wresult));
    }

    @Test
    void verifyCreateCredentialFromTypedAttributeValues() throws Throwable {
        val xml = """
            <saml1:Assertion xmlns:saml1="urn:oasis:names:tc:SAML:1.0:assertion"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                AssertionID="_5D0E6F7A8B9C4D2E8F1A2B3C4D5E6F70" IssueInstant="2026-11-18T10:15:30.000Z"
                Issuer="http://adfs.example.com/adfs/services/trust" MajorVersion="1" MinorVersion="1">
                <saml1:AttributeStatement>
                    <saml1:Subject>
                        <saml1:NameIdentifier>casuser</saml1:NameIdentifier>
                    </saml1:Subject>
                    <saml1:Attribute AttributeName="upn" AttributeNamespace="http://schemas.xmlsoap.org/ws/2005/05/identity/claims">
                        <saml1:AttributeValue xsi:type="xs:string">casuser</saml1:AttributeValue>
                    </saml1:Attribute>
                    <saml1:Attribute AttributeName="website" AttributeNamespace="http://schemas.xmlsoap.org/ws/2005/05/identity/claims">
                        <saml1:AttributeValue xsi:type="xs:anyURI">https://apereo.github.io</saml1:AttributeValue>
                    </saml1:Attribute>
                    <saml1:Attribute AttributeName="photo" AttributeNamespace="http://schemas.xmlsoap.org/ws/2005/05/identity/claims">
                        <saml1:AttributeValue xsi:type="xs:base64Binary">Y2FzdXNlcg==</saml1:AttributeValue>
                    </saml1:Attribute>
                    <saml1:Attribute AttributeName="Group" AttributeNamespace="http://schemas.xmlsoap.org/claims">
                        <saml1:AttributeValue>admins</saml1:AttributeValue>
                        <saml1:AttributeValue xsi:type="xs:integer">42</saml1:AttributeValue>
                    </saml1:Attribute>
                </saml1:AttributeStatement>
            </saml1:Assertion>
            """;
        try (val in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            val element = parserPool.parse(in).getDocumentElement();
            val assertion = (Assertion) unmarshallerFactory.getUnmarshaller(element).unmarshall(element);
            val attributes = assertion.getAttributeStatements().getFirst().getAttributes();
            assertInstanceOf(XSString.class, attributes.getFirst().getAttributeValues().getFirst());
            assertInstanceOf(XSURI.class, attributes.get(1).getAttributeValues().getFirst());
            assertInstanceOf(XSBase64Binary.class, attributes.get(2).getAttributeValues().getFirst());
            assertInstanceOf(XSAny.class, attributes.get(3).getAttributeValues().getFirst());

            val credential = wsFederationHelper.createCredentialFromToken(assertion);
            assertEquals(List.of("casuser"), credential.getAttributes().get("upn"));
            assertEquals(List.of("https://apereo.github.io"), credential.getAttributes().get("website"));
            assertEquals(List.of("Y2FzdXNlcg=="), credential.getAttributes().get("photo"));
            assertEquals(List.of("admins", "42"), credential.getAttributes().get("Group"));
        }
    }

    @Test
    void verifyGetSigningCredential() throws Throwable {
        val provider = WsFederationCertificateProvider.getProvider(wsFederationConfigurations.toList().getFirst(), configBean);
        assertFalse(provider.getSigningCredentials().isEmpty());
        assertNotNull(provider);
    }

    @Test
    void verifyValidateSignatureGoodToken() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertTrue(result);
    }

    @Test
    void verifyValidateSignatureBadInput() {
        assertFalse(wsFederationHelper.validateSignature(null));
        assertFalse(wsFederationHelper.validateSignature(Pair.of(null, null)));
        val config = wsFederationConfigurations.toList().getFirst();
        val assertion = mock(Assertion.class);
        assertFalse(wsFederationHelper.validateSignature(Pair.of(assertion, config)));
        when(assertion.getSignature()).thenReturn(mock(Signature.class));
        assertFalse(wsFederationHelper.validateSignature(Pair.of(assertion, config)));
    }

    @Test
    void verifyValidateSignatureModifiedAttribute() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("badTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertFalse(result);
    }

    @Test
    void verifyValidateSignatureModifiedSignature() throws Throwable {
        val wresult = IOUtils.toString(new ClassPathResource("badTokenSignature.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertFalse(result);
    }
}
