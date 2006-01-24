/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.security.cert.X509Certificate;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * @author Anders Svensson
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class X509CertificateCredentialsToIdentifierPrincipalResolver extends
    AbstractX509CertificateCredentialsToPrincipalResolver implements
    InitializingBean {

    private static final String DEFAULT_IDENTIFIER = "$OU $CN";

    private static final String ENTRIES_DELIMITER = ",";

    private static final String NAME_VALUE_PAIR_DELIMITER = "=";

    /** The identifier meta data */
    private String identifier;

    protected Principal resolvePrincipalInternal(
        final X509Certificate certificate) {
        String username = this.identifier;

        final String[] entries = certificate.getSubjectDN().getName().split(
            ENTRIES_DELIMITER);

        for (int i = 0; i < entries.length; i++) {
            final String[] nameValuePair = entries[i]
                .split(NAME_VALUE_PAIR_DELIMITER);
            final String name = nameValuePair[0];
            final String value = nameValuePair[1];

            if (log.isDebugEnabled()) {
                log.debug("Parsed " + name + " - " + value);
            }

            username = username.replaceAll("\\$" + name, value);
        }

        return new SimplePrincipal(username);
    }

    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(this.identifier)) {
            log
                .info("No identifier set.  Using default: "
                    + DEFAULT_IDENTIFIER);
            this.identifier = DEFAULT_IDENTIFIER;
        }
    }
}
