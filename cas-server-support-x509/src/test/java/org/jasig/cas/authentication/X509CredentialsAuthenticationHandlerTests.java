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
package org.jasig.cas.authentication;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import edu.vt.middleware.crypt.util.CryptReader;
import org.jasig.cas.authentication.X509CertificateCredential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.X509CredentialsAuthenticationHandler;
import org.jasig.cas.authentication.support.ResourceCRLRevocationChecker;
import org.jasig.cas.authentication.support.ThresholdExpiredCRLRevocationPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit test for {@link org.jasig.cas.authentication.X509CredentialsAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.0.4
 *
 */
@RunWith(Parameterized.class)
public class X509CredentialsAuthenticationHandlerTests {
    /** Subject of test. */
    private X509CredentialsAuthenticationHandler handler;

    /** Test credential. */
    private X509CertificateCredential credential;

    /** Expected result of authentication attempt. */
    private boolean expected;
    
    
    /**
     * Creates a new test class instance with the given parameters.
     *
     * @param handler Test authentication handler.
     * @param credential Test credential.
     * @param authenticationSuccess Expected result of authentication test.
     */
    public X509CredentialsAuthenticationHandlerTests(
        final X509CredentialsAuthenticationHandler handler,
        final X509CertificateCredential credential,
        final boolean authenticationSuccess) {
       
        this.handler = handler;
        this.credential = credential;
        this.expected = authenticationSuccess;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     * 
     * @throws Exception  On test data setup errors.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception
    {
      final Collection<Object[]> params = new ArrayList<Object[]>();
      
      X509CredentialsAuthenticationHandler handler;

      // Test case #1
      // Valid certificate
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-valid.crt")),
          true,
      });
      
      // Test case #2
      // Expired certificate
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-expired.crt")),
          false,
      });
      
      // Test case #3
      // Untrusted issuer
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("snake-oil.crt")),
          false,
      });
      
      // Test case #4
      // Disallowed subject
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      handler.setSubjectDnPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("snake-oil.crt")),
          false,
      });
      
      // Test case #5
      // Check key usage on a cert without keyUsage extension
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      handler.setCheckKeyUsage(true);
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-valid.crt")),
          true,
      });
      
      // Test case #6
      // Require key usage on a cert without keyUsage extension
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      handler.setCheckKeyUsage(true);
      handler.setRequireKeyUsage(true);
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-valid.crt")),
          false,
      });
      
      // Test case #7
      // Require key usage on a cert with acceptable keyUsage extension values
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      handler.setCheckKeyUsage(true);
      handler.setRequireKeyUsage(true);
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-valid-keyUsage.crt")),
          true,
      });
      
      // Test case #8
      // Require key usage on a cert with unacceptable keyUsage extension values
      handler = new X509CredentialsAuthenticationHandler();
      handler.setTrustedIssuerDnPattern(".*");
      handler.setCheckKeyUsage(true);
      handler.setRequireKeyUsage(true);
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-invalid-keyUsage.crt")),
          false,
      });
      
      //===================================
      // Revocation tests
      //===================================
      ResourceCRLRevocationChecker checker;

      // Test case #9
      // Valid certificate with CRL checking
      handler = new X509CredentialsAuthenticationHandler();
      checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
      checker.afterPropertiesSet();
      handler.setRevocationChecker(checker);
      handler.setTrustedIssuerDnPattern(".*");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-valid.crt")),
          true,
      });

      // Test case #10
      // Revoked end user certificate
      handler = new X509CredentialsAuthenticationHandler();
      checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
      checker.afterPropertiesSet();
      handler.setRevocationChecker(checker);
      handler.setTrustedIssuerDnPattern(".*");
      params.add(new Object[] {
          handler,
          new X509CertificateCredential(createCertificates("user-revoked.crt")),
          false,
      });
      
      // Test case #11
      // Valid certificate on expired CRL data
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
          false,
      });
      
      return params;
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#authenticate(org.jasig.cas.authentication.Credential)} method.
     */
    @Test
    public void testAuthenticate() throws Exception {
        try {
            final HandlerResult result = this.handler.authenticate(this.credential);
            if (this.expected) {
                Assert.assertEquals(
                        this.credential.getCertificate().getSubjectDN().getName(),
                        result.getPrincipal().getId());
            } else {
                Assert.fail("Should have thrown GeneralSecurityException");
            }
        } catch (GeneralSecurityException e) {
            if (this.expected) {
                Assert.fail("Authentication failed where it should have succeeded.");
            }
        }
    }

    protected static X509Certificate[] createCertificates(final String ... files) {
        final X509Certificate[] certs = new X509Certificate[files.length];
        
        int i = 0;
        for (final String file : files) {
            try {
                certs[i++] = (X509Certificate) CryptReader.readCertificate(
                    new ClassPathResource(file).getInputStream());
            } catch (Exception e) {
                throw new RuntimeException("Error creating certificate at " + file, e);
            }
        }
        return certs;
    }
}