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

import java.security.cert.X509Certificate;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryPrincipalResolver;
import org.jasig.cas.authentication.Credential;

/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractX509CertificatePrincipalResolver
    extends AbstractPersonDirectoryPrincipalResolver {

    protected String extractPrincipalId(final Credential credential) {
        return resolvePrincipalInternal(((X509CertificateCredential) credential).getCertificate());
    }

    public boolean supports(final Credential credential) {
        return credential != null
            && X509CertificateCredential.class.isAssignableFrom(credential
                .getClass());
    }

    protected abstract String resolvePrincipalInternal(
        final X509Certificate certificate);
}
