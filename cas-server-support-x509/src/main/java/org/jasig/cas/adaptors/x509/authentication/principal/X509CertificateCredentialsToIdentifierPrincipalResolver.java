/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.inspektr.common.ioc.annotation.NotNull;

/**
 * @author Anders Svensson
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class X509CertificateCredentialsToIdentifierPrincipalResolver extends
    AbstractX509CertificateCredentialsToPrincipalResolver {

    private static final String DEFAULT_IDENTIFIER = "$OU $CN";

    private static final String ENTRIES_DELIMITER = ",";

    private static final String NAME_VALUE_PAIR_DELIMITER = "=";

    /** The identifier meta data */
    @NotNull
    private String identifier = DEFAULT_IDENTIFIER;

    protected String resolvePrincipalInternal(
        final X509Certificate certificate) {
        String username = this.identifier;
        
        if (log.isInfoEnabled()) {
            log.info("Creating principal for: " + certificate.getSubjectDN().getName());
        }

        final String[] entries = certificate.getSubjectDN().getName().split(
            ENTRIES_DELIMITER);
        
        for (final String val : entries) {
            final String[] nameValuePair = val
                .split(NAME_VALUE_PAIR_DELIMITER);
            final String name = nameValuePair[0].trim();
            final String value = nameValuePair[1];

            if (log.isDebugEnabled()) {
                log.debug("Parsed " + name + " - " + value);
            }

            username = username.replaceAll("\\$" + name, value);
        }
        
        if (this.identifier.equals(username)) {
            return null;
        }

        return username;
    }
    
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }
}
