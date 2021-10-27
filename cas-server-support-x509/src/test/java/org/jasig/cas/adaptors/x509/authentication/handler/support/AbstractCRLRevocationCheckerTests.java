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
import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Base class for {@link RevocationChecker} unit tests.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public abstract class AbstractCRLRevocationCheckerTests {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Certificate to be tested. */
    private final X509Certificate[] certificates;

    /** Expected result of check; null for success. */
    private final GeneralSecurityException expected;

    /**
     * Creates a new test instance with given parameters.
     *
     * @param certFiles File names of certificates to check.
     * @param expected Expected result of check; null to indicate expected success.
     */
    public AbstractCRLRevocationCheckerTests(
            final String[] certFiles,
            final GeneralSecurityException expected) {

        this.expected = expected;
        this.certificates = new X509Certificate[certFiles.length];
        int i = 0;
        for (final String file : certFiles) {
            this.certificates[i++] = CertUtils.readCertificate(new ClassPathResource(file));
        }
    }

    /**
     * Test method for {@link AbstractCRLRevocationChecker#check(X509Certificate)}.
     */
    @Test
    public void checkCertificate() {
        try {
            for (final X509Certificate cert : this.certificates) {
                getChecker().check(cert);
            }
            if (this.expected != null) {
                Assert.fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (final GeneralSecurityException e) {
            if (this.expected == null) {
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

    protected abstract RevocationChecker getChecker();
}
