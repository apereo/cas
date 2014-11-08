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


/**
 * NO-OP implementation certificate revocation checker.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public final class NoOpRevocationChecker implements RevocationChecker {

    /**
     * NO-OP check implementation.
     *
     * @param certificate Certificate to check.
     *
     * @throws GeneralSecurityException Never thrown.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationChecker#check(java.security.cert.X509Certificate)
     */
    @Override
    public void check(final X509Certificate certificate) throws GeneralSecurityException {
        // NO-OP
    }

}
