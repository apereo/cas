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

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.jasig.cas.adaptors.x509.util.MockX509CRL;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * Unit test for {@link ThresholdExpiredCRLRevocationPolicy} class.
 *
 * @author Marvin S. Addison
 * @since 3.4.7
 *
 */
@RunWith(Parameterized.class)
public class ThresholdExpiredCRLRevocationPolicyTests {
    /** Policy instance under test. */
    private final ThresholdExpiredCRLRevocationPolicy policy;

    /** CRL to test. */
    private final X509CRL crl;

    /** Expected result of check; null for success */
    private final GeneralSecurityException expected;


    /**
     * Creates a new test instance with given parameters.
     *
     * @param policy Policy to test.
     * @param crl CRL instance to apply policy to.
     * @param expected Expected result of policy application; null to indicate expected success.
     */
    public ThresholdExpiredCRLRevocationPolicyTests(
            final ThresholdExpiredCRLRevocationPolicy policy,
            final X509CRL crl,
            final GeneralSecurityException expected) {

        this.policy = policy;
        this.expected = expected;
        this.crl = crl;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     * @throws Exception if there is an exception getting the test parameters.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        final Collection<Object[]> params = new ArrayList<>();

        final Date now = new Date();
        final Date twoHoursAgo = new Date(now.getTime() - 7200000);
        final Date oneHourAgo = new Date(now.getTime() - 3600000);
        final Date halfHourAgo = new Date(now.getTime() - 1800000);
        final X500Principal issuer = new X500Principal("CN=CAS");

        // Test case #1
        // Expect expired for zero leniency on CRL expiring 1ms ago
        final ThresholdExpiredCRLRevocationPolicy zeroThreshold = new ThresholdExpiredCRLRevocationPolicy();
        zeroThreshold.setThreshold(0);
        params.add(new Object[] {
                zeroThreshold,
                new MockX509CRL(issuer, oneHourAgo, new Date(now.getTime() - 1)),
                new ExpiredCRLException("CN=CAS", new Date()),
        });

        // Test case #2
        // Expect expired for 1h leniency on CRL expired 1 hour 1ms ago
        final ThresholdExpiredCRLRevocationPolicy oneHourThreshold = new ThresholdExpiredCRLRevocationPolicy();
        oneHourThreshold.setThreshold(3600);
        params.add(new Object[] {
                oneHourThreshold,
                new MockX509CRL(issuer, twoHoursAgo, new Date(oneHourAgo.getTime() - 1)),
                new ExpiredCRLException("CN=CAS", new Date()),
        });

        // Test case #3
        // Expect valid for 1h leniency on CRL expired 30m ago
        params.add(new Object[] {
                oneHourThreshold,
                new MockX509CRL(issuer, twoHoursAgo, halfHourAgo),
                null,
        });

        return params;
    }

    /**
     * Test method for {@link ThresholdExpiredCRLRevocationPolicy#apply(java.security.cert.X509CRL)}.
     */
    @Test
    public void verifyApply() {
        try {
            this.policy.apply(this.crl);
            if (this.expected != null) {
                Assert.fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (final GeneralSecurityException e) {
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
}
