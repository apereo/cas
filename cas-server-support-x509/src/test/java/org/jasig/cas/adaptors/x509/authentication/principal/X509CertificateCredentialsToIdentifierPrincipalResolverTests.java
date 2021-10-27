/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentialsToIdentifierPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;


/**
 * @author Markus Härnvi, Altcom
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class X509CertificateCredentialsToIdentifierPrincipalResolverTests
    extends AbstractX509CertificateTests {

    X509CertificateCredentialsToIdentifierPrincipalResolver resolver = new X509CertificateCredentialsToIdentifierPrincipalResolver();
    
    public void testResolvePrincipalInternal() throws Exception {
        final X509CertificateCredentials credentials = new X509CertificateCredentials(
            new X509Certificate[0]);
        credentials.setCertificate(getTestCertificate());

        this.resolver.setIdentifier("$C, $CN");
        assertEquals("The principals should match", this.resolver.resolvePrincipal(
            credentials).getId(), "SE, test testsson");
        assertFalse("The principals should not match", this.resolver
            .resolvePrincipal(credentials).getId().equals("SE, Altcom Test"));
    }

    public void testSupport() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }
    
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }

    private X509Certificate getTestCertificate() {

        final String validCert = "-----BEGIN CERTIFICATE-----\n"
            + "MIID3DCCAsSgAwIBAgIBATANBgkqhkiG9w0BAQUFADCBgTEUMBIGA1UEAxMLQWx0\n"
            + "Y29tIFRlc3QxCzAJBgNVBAYTAlNFMRAwDgYDVQQIEwdVcHBsYW5kMRIwEAYDVQQH\n"
            + "EwlTdG9ja2hvbG0xEjAQBgNVBAoTCWFsdGNvbS5zZTEiMCAGCSqGSIb3DQEJARYT\n"
            + "anVzdGFmYWtlQGFsdGNvbS5zZTAeFw0wNTEyMTUxNTA5NTlaFw0wODA5MTAxNTA5\n"
            + "NTlaMIGEMQswCQYDVQQGEwJTRTEQMA4GA1UECBMHVXBwbGFuZDESMBAGA1UEChMJ\n"
            + "YWx0Y29tLnNlMRQwEgYDVQQLEwtBbHRjb20gVGVzdDEWMBQGA1UEAxMNdGVzdCB0\n"
            + "ZXN0c3NvbjEhMB8GCSqGSIb3DQEJARYSdGVzdHRlc3RAYWx0Y29tLnNlMFwwDQYJ\n"
            + "KoZIhvcNAQEBBQADSwAwSAJBAMcbMTo6t2x1L+4wlVj4gCEGyyUO/f99mqrbGYsl\n"
            + "1GFr9a4PL0gYzuX5XYOGIT4+wlij0xtN2NLrzME0UWQYzgMCAwEAAaOCASAwggEc\n"
            + "MAkGA1UdEwQCMAAwHQYDVR0OBBYEFKYtB3SrYJW2ZJLiE7hM2Cyj6oIWMIG2BgNV\n"
            + "HSMEga4wgauAFK9OFsXxDnEzx3VvZcf7jpTPliUpoYGHpIGEMIGBMRQwEgYDVQQD\n"
            + "EwtBbHRjb20gVGVzdDELMAkGA1UEBhMCU0UxEDAOBgNVBAgTB1VwcGxhbmQxEjAQ\n"
            + "BgNVBAcTCVN0b2NraG9sbTESMBAGA1UEChMJYWx0Y29tLnNlMSIwIAYJKoZIhvcN\n"
            + "AQkBFhNqdXN0YWZha2VAYWx0Y29tLnNlggkA+W5ygmZwb/gwNwYJYIZIAYb4QgEE\n"
            + "BCoWKGh0dHBzOi8vbm9uZXhpc3RpbmcuYWx0Y29tLnNlL2NhLWNybC5wZW0wDQYJ\n"
            + "KoZIhvcNAQEFBQADggEBAJF5YOcmyFqmZWAid3KcYwdvP3aP2I5RPKUEaH2yLGPm\n"
            + "cHJcOXq2gUp6FIO5zkYS06O+2BtHhclDeL9RVUBkAJ3oSQ7boQXTT1b9AzxbOi16\n"
            + "weV7tQPgzV/5t8pme9HkyB7fbaFBYrxgwQIMBuAovlPuZbwosfuk8bksX8FtCdvf\n"
            + "hkupCvhsIvz3whYhW/P5b8Pcj12rDITNA4pIz8edBqEQxwA7XpXGIL/fHz7SC8xi\n"
            + "4NxhtxFKDirFOMEtarMCXC5oNYfo7sqrTPfOFWLB2LOCW32+FvIL7w1ci0ND9X96\n"
            + "oH4b/VDw/nP8CWPJdMHFnRtqhIIyNJmweRO9rI9GBrk=\n"
            + "-----END CERTIFICATE-----";

        final ByteArrayInputStream in = new ByteArrayInputStream(validCert
            .getBytes());
        CertificateFactory cf;
        X509Certificate cert = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
            fail("Error creating test certificate");
        }
        return cert;

    }

}
