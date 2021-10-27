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

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;


/**
 * Unit tests for {@link ResourceCRLRevocationChecker} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
@RunWith(Parameterized.class)
public class ResourceCRLRevocationCheckerTests extends AbstractCRLRevocationCheckerTests {
    /** Instance under test. */
    private final ResourceCRLRevocationChecker checker;

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

        super(certFiles, expected);

        this.checker = checker;
        this.checker.setExpiredCRLPolicy(expiredCRLPolicy);
        try {
            this.checker.afterPropertiesSet();
        } catch (final Exception e) {
            throw new RuntimeException("ResourceCRLRevocationChecker initialization failed", e);
        }
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        final Collection<Object[]> params = new ArrayList<>();

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

        // Test case #5
        // Valid certificate on expired CRL data with custom expiration
        // policy to always allow expired CRL data
        params.add(new Object[] {
                new ResourceCRLRevocationChecker(new ClassPathResource[] {
                        new ClassPathResource("userCA-expired.crl"),
                }),
                new RevocationPolicy<X509CRL>() {
                    @Override
                    public void apply(final X509CRL crl) {/* Do nothing to allow unconditionally */}
                },
                new String[] {"user-valid.crt"},
                null,
        });

        return params;
    }

    @Override
    protected RevocationChecker getChecker() {
        return this.checker;
    }
}
