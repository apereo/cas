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
package org.jasig.cas.adaptors.x509.authentication.principal;

import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;

/**
 * This class is targeted at usage for mapping to an existing user record. It
 * can construct a highly-likely unique DN based on a certificate's serialnumber
 * and its issuerDN. example:
 * SERIALNUMBER=20267647332258882251479793556682961758, SERIALNUMBER=200301,
 * CN=Citizen CA, C=BE see RFC3280 The combination of a certificate serial
 * number and the issuerDN *should* be unique: - The certificate serialNumber is
 * by its nature unique for a certain issuer. - The issuerDN is RECOMMENDED to
 * be unique. Both the serial number and the issuerDN are REQUIRED in a
 * certificate. Note: comparison rules state the compare should be
 * case-insensitive. LDAP value description: EQUALITY distinguishedNameMatch
 * SYNTAX 1.3.6.1.4.1.1466.115.121.1.12 [=distinguishedName]
 *
 * @author Jan Van der Velpen
 * @since 3.1
 */
public final class X509SerialNumberAndIssuerDNPrincipalResolver extends AbstractX509PrincipalResolver {

    /** Prefix for Certificate Serial Number. */
    @NotNull
    private String serialNumberPrefix = "SERIALNUMBER=";

    /** Prefix for Value Delimiter. */
    @NotNull
    private String valueDelimiter = ", ";

    /**
     * Sets a prefix for the certificate serialnumber (default: "SERIALNUMBER=").
     *
     * @param serialNumberPrefix The serialNumberPrefix to set.
     */
    public void setSerialNumberPrefix(final String serialNumberPrefix) {
        this.serialNumberPrefix = serialNumberPrefix;
    }

    /**
     * Sets a delimiter to separate the two certificate properties in the string.
     * (default: ", ")
     *
     * @param valueDelimiter The valueDelimiter to set.
     */
    public void setValueDelimiter(final String valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        final StringBuilder builder = new StringBuilder(this.serialNumberPrefix);
        builder.append(certificate.getSerialNumber().toString());
        builder.append(this.valueDelimiter);
        builder.append(certificate.getIssuerDN().getName());
        return builder.toString();
    }
}
