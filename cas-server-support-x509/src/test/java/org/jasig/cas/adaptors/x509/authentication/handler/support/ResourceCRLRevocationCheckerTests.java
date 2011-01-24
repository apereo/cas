/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import edu.vt.middleware.crypt.util.CryptReader;


/**
 * Unit tests for {@link ResourceCRLRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.7
 *
 */
@RunWith(Parameterized.class)
public class ResourceCRLRevocationCheckerTests {
    /** Instance under test */
    private ResourceCRLRevocationChecker checker;
    
    /** Certificate to be tested */
    private X509Certificate[] certificates;

    /** Expected result of check; null for success */
    private GeneralSecurityException expected;

    /**
     * Creates a new test instance with given parameters.
     *
     * @param checker Revocation checker instance.
     * @param expiredCRLPolicy Policy instance for handling expired CRL data.
     * @param certFiles File names of certificates to check.
     * @param expected Expected result of check; null to indicate expected success.
     */
    public ResourceCRLRevocationCheckerTests(
        final ResourceCRLRevocationChecker checker,
        final RevocationPolicy<X509CRL> expiredCRLPolicy,
        final String[] certFiles,
        final GeneralSecurityException expected) {

        this.checker = checker;
        this.expected = expected;
       
        this.certificates = new X509Certificate[certFiles.length];
        int i = 0;
        for (String file : certFiles) {
            this.certificates[i++] = readCertificate(file);
        }

        this.checker.setExpiredCRLPolicy(expiredCRLPolicy);
        try {
            this.checker.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("ResourceCRLRevocationChecker initialization failed", e);
        }
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters()
    {
      final Collection<Object[]> params = new ArrayList<Object[]>();
      
      final ThresholdExpiredCRLRevocationPolicy zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy();
      zeroThresholdPolicy.setThreshold(0);
      
      // Test case #1
      // Valid certificate on valid CRL data
      params.add(new Object[] {
          new ResourceCRLRevocationChecker(new ClassPathResource[] {
              new ClassPathResource("userCA-valid.crl"),
          }),
          zeroThresholdPolicy,
          new String[] {"user-valid.crt"},
          null,
      });
      
      // Test case #2
      // Revoked certificate on valid CRL data
      params.add(new Object[] {
          new ResourceCRLRevocationChecker(new ClassPathResource[] {
              new ClassPathResource("userCA-valid.crl"),
              new ClassPathResource("intermediateCA-valid.crl"),
              new ClassPathResource("rootCA-valid.crl"),
          }),
          zeroThresholdPolicy,
          new String[] {"user-revoked.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt" },
          new RevokedCertificateException(new Date(), new BigInteger("1")),
      });
      
      // Test case #3
      // Valid certificate on expired CRL data for head cert
      params.add(new Object[] {
          new ResourceCRLRevocationChecker(new ClassPathResource[] {
              new ClassPathResource("userCA-expired.crl"),
              new ClassPathResource("intermediateCA-valid.crl"),
              new ClassPathResource("rootCA-valid.crl"),
          }),
          zeroThresholdPolicy,
          new String[] {"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt" },
          new ExpiredCRLException("test", new Date()),
      });
      
      // Test case #4
      // Valid certificate on expired CRL data for intermediate cert
      params.add(new Object[] {
          new ResourceCRLRevocationChecker(new ClassPathResource[] {
              new ClassPathResource("userCA-valid.crl"),
              new ClassPathResource("intermediateCA-expired.crl"),
              new ClassPathResource("rootCA-valid.crl"),
          }),
          zeroThresholdPolicy,
          new String[] {"user-valid.crt", "userCA.crt", "intermediateCA.crt", "rootCA.crt" },
          new ExpiredCRLException("test", new Date()),
      });

      return params;
    }

    /**
     * Test method for {@link org.jasig.cas.adaptors.x509.authentication.handler.support.AbstractCRLRevocationChecker#check(java.security.cert.X509Certificate)}.
     */
    @Test
    public void testCheck() {
        try {
            for (X509Certificate cert : this.certificates) {
	            this.checker.check(cert);
            }
            if (this.expected != null) {
                Assert.fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (GeneralSecurityException e) {
            if (this.expected == null) {
                e.printStackTrace();
                Assert.fail("Revocation check failed unexpectedly with exception: " + e);
            } else {
                final Class<?> expectedClass = this.expected.getClass();
                final Class<?> actualClass = e.getClass();
                Assert.assertTrue(
                    String.format("Expected exception of type %s but got %s", expectedClass, actualClass),
                    expectedClass.isAssignableFrom(actualClass));
            }
        }
    }

    private X509Certificate readCertificate(final String file) {
        InputStream in = null;
        try {
            in = new ClassPathResource(file).getInputStream();
            return (X509Certificate) CryptReader.readCertificate(in);
        } catch (Exception e) {
            throw new RuntimeException("Error reading certificate " + file, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
