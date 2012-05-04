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

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public final class X509CertificateCredentials implements Credentials {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = 7579713688326827121L;

    /** The collection of certificates sent with the request. */
    private X509Certificate[] certificates;
    
    /** The certificate that we actually use. */
    private X509Certificate certificate;

    public X509CertificateCredentials(final X509Certificate[] certificates) {
        this.certificates = certificates;
    }

    public X509Certificate[] getCertificates() {
        return this.certificates;
    }
    
    public void setCertificate(final X509Certificate certificate) {
        this.certificate = certificate;
    }
    
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    public String toString() {
        X509Certificate cert = null;
        if (getCertificate() != null) {
            cert = getCertificate();
        } else if (getCertificates() != null && getCertificates().length > 1) {
            cert = getCertificates()[0];
        }
        
        if (cert != null) {
            return CertUtils.toString(cert);
        }
        return super.toString();
    }
}
