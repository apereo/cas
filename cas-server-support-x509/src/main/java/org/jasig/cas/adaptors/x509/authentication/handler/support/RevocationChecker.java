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
 * Strategy interface for checking revocation status of a certificate.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public interface RevocationChecker {
    /**
     * Checks the revocation status of the given certificate.
     *
     * @param certificate Certificate to examine.
     *
     * @throws GeneralSecurityException If certificate has been revoked or the revocation
     * check fails for some reason such as revocation data not available.
     */
    void check(X509Certificate certificate) throws GeneralSecurityException;
}
