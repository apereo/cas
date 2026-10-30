package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.crypto.CertUtils;
import com.nimbusds.jose.util.X509CertUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20X509AuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OAuth")
public class OAuth20X509AuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthX509CertificateAuthenticator")
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @Autowired
    @Qualifier("oauthX509CertificateAuthenticationProvider")
    private OAuth20ClientAuthenticationProvider authenticationProvider;

    @Test
    void verifyAcceptedCredentialsWithClientId() {
        val certificate = new CasX509Certificate(true);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "TlsWithClientId");
        val profile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificate));
        assertTrue(profile.isPresent());
        assertEquals(X509CertUtils.computeSHA256Thumbprint(certificate).toString(),
            profile.orElseThrow().getAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST));
    }

    @Test
    void verifyAcceptedCredentialsWitSpiffe() {
        val certificate = new CasX509Certificate(true)
            .setSubjectAltNames(List.of("spiffe://example.org/client"));
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "TlsWithClientId");
        val profile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificate));
        assertTrue(profile.isPresent());
    }

    @Test
    void verifyCertificateWithoutCommonName() throws Throwable {
        val spiffeCertificate = mockCertificate("O=SPIRE,C=US", List.of(List.of(6, "spiffe://example.org/client")));
        val spiffeProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(new MockHttpServletRequest(),
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, spiffeCertificate));
        assertEquals("spiffe://example.org/client", spiffeProfile.orElseThrow().getId());

        val registeredService = new OAuthRegisteredService();
        registeredService.setName("TlsSanOnly");
        registeredService.setServiceId("https://www.exampletlssanonly.org");
        registeredService.setClientId(UUID.randomUUID().toString());
        registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
        registeredService.setTlsClientAuthSanDns("client.example.org");
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val sanCertificate = mockCertificate(StringUtils.EMPTY, List.of(List.of(2, "client.example.org")));
        val sanProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, sanCertificate));
        assertEquals("client.example.org", sanProfile.orElseThrow().getId());

        val twoNamesCertificate = mockCertificate("CN=one,CN=two,O=Example", List.of(List.of(2, "client.example.org")));
        val twoNamesProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, twoNamesCertificate));
        assertEquals("CN=one,CN=two,O=Example", twoNamesProfile.orElseThrow().getId());
    }

    @Test
    void verifyRegisteredSubjectIsRequired() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName("TlsSan");
        registeredService.setServiceId("https://www.exampletlssan.org");
        registeredService.setClientId(UUID.randomUUID().toString());
        registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val certificate = new CasX509Certificate(true).setSubjectAltNames(List.of(2, "client.example.org"));
        assertThrows(BadCredentialsException.class, () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificate)));

        registeredService.setTlsClientAuthSanDns("client.example.org");
        servicesManager.save(registeredService);
        assertTrue(authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificate)).isPresent());

        val otherCertificate = new CasX509Certificate(true).setSubjectAltNames(List.of(2, "other.example.org"));
        assertThrows(BadCredentialsException.class, () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, otherCertificate)));

        val lookalikeCertificate = new CasX509Certificate(true).setSubjectAltNames(List.of(2, "clientXexample.org"));
        assertThrows(BadCredentialsException.class, () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, lookalikeCertificate)));

        registeredService.setTlsClientAuthSanDns("CLIENT.example.org");
        servicesManager.save(registeredService);
        assertTrue(authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificate)).isPresent());

        registeredService.setTlsClientAuthSanDns(null);
        registeredService.setTlsClientAuthSanIp("::1");
        servicesManager.save(registeredService);
        val addressCertificate = new CasX509Certificate(true).setSubjectAltNames(List.of(7, "0:0:0:0:0:0:0:1"));
        assertTrue(authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request,
            OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, addressCertificate)).isPresent());
    }

    @Test
    void verifyRegisteredSubjectDistinguishedName() throws Throwable {
        val x509Authenticator = new OAuth20X509Authenticator(servicesManager, oauthRequestParameterResolver);
        val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
        val profile = x509Authenticator.buildCertificateProfile(certificate, "cn=apereo.org, ou=CAS, o=Apereo, l=Gilbert, st=AZ, c=US");
        assertEquals("apereo.org", profile.getId());
        assertEquals("CN=apereo.org,OU=CAS,O=Apereo,L=Gilbert,ST=AZ,C=US", profile.getAttribute("x509-subjectDN"));
        assertThrows(BadCredentialsException.class,
            () -> x509Authenticator.buildCertificateProfile(certificate, "CN=apereo.org,OU=CAS,O=Apereo,L=Gilbert,ST=AZ,C=CA"));
    }

    @Test
    void verifyRegisteredSubjectDistinguishedNameWithParentheses() throws Throwable {
        val x509Authenticator = new OAuth20X509Authenticator(servicesManager, oauthRequestParameterResolver);
        val subject = new X500Principal("CN=client,O=Acme (Europe) Ltd");
        val certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(subject);
        when(certificate.getIssuerX500Principal()).thenReturn(subject);
        when(certificate.getEncoded()).thenReturn(new byte[]{1});
        val profile = x509Authenticator.buildCertificateProfile(certificate, "CN=client, O=Acme (Europe) Ltd");
        assertEquals("client", profile.getId());
    }

    @Test
    void verifyUnusableSubjectPattern() {
        val x509Authenticator = new OAuth20X509Authenticator(servicesManager, oauthRequestParameterResolver);
        val certificate = new CasX509Certificate(true);
        assertThrows(BadCredentialsException.class, () -> x509Authenticator.buildCertificateProfile(certificate, "CN=.+"));
        assertThrows(BadCredentialsException.class, () -> x509Authenticator.buildCertificateProfile(certificate, "CN=(.+"));
        assertThrows(BadCredentialsException.class, () -> x509Authenticator.buildCertificateProfile(certificate, "^()"));
    }

    @Test
    void verifyCertificateOfTheConnection() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "TlsWithClientId");
        assertNull(authenticationProvider.getAuthenticationConverter().convert(request));

        val certificate = new CasX509Certificate(true);
        request.setAttribute("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate});
        val credentials = authenticationProvider.getAuthenticationConverter().convert(request);
        assertInstanceOf(OAuth20ClientAuthenticationToken.class, credentials);
        assertEquals(OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, ((OAuth20ClientAuthenticationToken) credentials).getSource());
        assertSame(certificate, credentials.getCredentials());

        val result = authenticationProvider.getAuthenticationProvider().authenticate(credentials);
        assertNotNull(result);
        val profile = assertInstanceOf(OAuth20UserProfile.class, result.getPrincipal());
        assertEquals(X509CertUtils.computeSHA256Thumbprint(certificate).toString(),
            profile.getAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST));
    }

    private static X509Certificate mockCertificate(final String subject, final Collection<List<?>> subjectAlternativeNames) throws Exception {
        val principal = new X500Principal(subject);
        val certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(principal);
        when(certificate.getIssuerX500Principal()).thenReturn(new X500Principal("CN=issuer"));
        when(certificate.getEncoded()).thenReturn(subject.getBytes(StandardCharsets.UTF_8));
        when(certificate.getSubjectAlternativeNames()).thenReturn(subjectAlternativeNames);
        return certificate;
    }
}
