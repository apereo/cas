/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.cryptacular.util.CertUtil;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for {@link X509CredentialsAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0.4
 *
 */
@RunWith(Parameterized.class)
public class X509CredentialsAuthenticationHandlerTests {

    /** Subject of test. */
    private final X509CredentialsAuthenticationHandler handler;

    /** Test authentication credential. */
    private final Credential credential;

    /** Expected result of supports test. */
    private final boolean expectedSupports;

    /** Expected authentication result. */
    private final Object expectedResult;


    /**
     * Creates a new test class instance with the given parameters.
     *
     * @param handler Test authentication handler.
     * @param credential Test credential.
     * @param supports Expected result of supports test.
     * @param result Expected result of authentication test.
     */
    public X509CredentialsAuthenticationHandlerTests(
            final X509CredentialsAuthenticationHandler handler,
            final Credential credential,
            final boolean supports,
            final Object result) {

        this.handler = handler;
        this.credential = credential;
        this.expectedSupports = supports;
        this.expectedResult = result;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     *
     * @throws Exception  On test data setup errors.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        final Collection<Object[]> params = new ArrayList<>();

        X509CredentialsAuthenticationHandler handler;
        X509CertificateCredential credential;

        // Test case #1: Unsupported credential type
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        params.add(new Object[] {handler, new UsernamePasswordCredential(), false, null});

        // Test case #2:Valid certificate
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        credential = new X509CertificateCredential(createCertificates("user-valid.crt"));
        params.add(new Object[] {handler, credential, true, new DefaultHandlerResult(handler, credential,
                new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #3: Expired certificate
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-expired.crt")),
                true,
                new CertificateExpiredException(),
        });

        // Test case #4: Untrusted issuer
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US");
        handler.setMaxPathLengthAllowUnspecified(true);
        params.add(new Object[] {handler, new X509CertificateCredential(createCertificates("snake-oil.crt")),
                true, new FailedLoginException(),
        });

        // Test case #5: Disallowed subject
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        handler.setSubjectDnPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US");
        handler.setMaxPathLengthAllowUnspecified(true);
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("snake-oil.crt")),
                true,
                new FailedLoginException(),
        });

        // Test case #6: Check key usage on a cert without keyUsage extension
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        handler.setCheckKeyUsage(true);
        credential = new X509CertificateCredential(createCertificates("user-valid.crt"));
        params.add(new Object[] {
                handler,
                credential,
                true,
                new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #7: Require key usage on a cert without keyUsage extension
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        handler.setCheckKeyUsage(true);
        handler.setRequireKeyUsage(true);
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-valid.crt")),
                true, new FailedLoginException(),
        });

        // Test case #8: Require key usage on a cert with acceptable keyUsage extension values
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        handler.setCheckKeyUsage(true);
        handler.setRequireKeyUsage(true);
        credential = new X509CertificateCredential(createCertificates("user-valid-keyUsage.crt"));
        params.add(new Object[] {
                handler,
                credential,
                true,
                new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #9: Require key usage on a cert with unacceptable keyUsage extension values
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        handler.setCheckKeyUsage(true);
        handler.setRequireKeyUsage(true);
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-invalid-keyUsage.crt")),
                true,
                new FailedLoginException(),
        });

        //===================================
        // Revocation tests
        //===================================
        ResourceCRLRevocationChecker checker;

        // Test case #10: Valid certificate with CRL checking
        handler = new X509CredentialsAuthenticationHandler();
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.afterPropertiesSet();
        handler.setRevocationChecker(checker);
        handler.setTrustedIssuerDnPattern(".*");
        credential = new X509CertificateCredential(createCertificates("user-valid.crt"));
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-valid.crt")),
                true,
                new DefaultHandlerResult(handler, credential, new DefaultPrincipalFactory().createPrincipal(credential.getId())),
        });

        // Test case #11: Revoked end user certificate
        handler = new X509CredentialsAuthenticationHandler();
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.afterPropertiesSet();
        handler.setRevocationChecker(checker);
        handler.setTrustedIssuerDnPattern(".*");
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-revoked.crt")),
                true,
                new RevokedCertificateException(null, null),
        });

        // Test case #12: Valid certificate on expired CRL data
        final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy();
        zeroThresholdPolicy.setThreshold(0);
        handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern(".*");
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-expired.crl"));
        checker.setExpiredCRLPolicy(zeroThresholdPolicy);
        checker.afterPropertiesSet();
        handler.setRevocationChecker(checker);
        params.add(new Object[] {
                handler,
                new X509CertificateCredential(createCertificates("user-valid.crt")),
                true,
                new ExpiredCRLException(null, null),
        });

        return params;
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#authenticate(org.jasig.cas.authentication.Credential)} method.
     */
    @Test
    public void verifyAuthenticate() {
        try {
            if (this.handler.supports(this.credential)) {
                final HandlerResult result = this.handler.authenticate(this.credential);
                if (this.expectedResult instanceof DefaultHandlerResult) {
                    assertEquals(this.expectedResult, result);
                } else {
                    fail("Authentication succeeded when it should have failed with " + this.expectedResult);
                }
            }
        } catch (final Exception e) {
            if (this.expectedResult instanceof Exception) {
                assertEquals(this.expectedResult.getClass(), e.getClass());
            } else {
                fail("Authentication failed when it should have succeeded.");
            }
        }
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#supports(org.jasig.cas.authentication.Credential)} method.
     */
    @Test
    public void verifySupports() {
        assertEquals(this.expectedSupports, this.handler.supports(this.credential));
    }

    protected static X509Certificate[] createCertificates(final String ... files) {
        final X509Certificate[] certs = new X509Certificate[files.length];

        int i = 0;
        for (final String file : files) {
            try {
                certs[i++] = (X509Certificate) CertUtil.readCertificate(
                        new ClassPathResource(file).getInputStream());
            } catch (final Exception e) {
                throw new RuntimeException("Error creating certificate at " + file, e);
            }
        }
        return certs;
    }
}

