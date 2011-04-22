/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The following class is deprecated in favor of
 * {@link X509CertificateCredentialsToSubjectPrinciplalResolver}.
 *
 * @author Anders Svensson
 * @author Scott Battaglia
 * @author Barry Silk
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
@Deprecated
public final class X509CertificateCredentialsToIdentifierPrincipalResolver extends AbstractX509CertificateCredentialsToPrincipalResolver {

    private static final String DEFAULT_IDENTIFIER = "$OU $CN";

    private final Pattern subjectRegex = Pattern.compile("([A-Z]+)=(?:\"(.+)\"|([\\w ]+))", 74);

    /** The identifier meta data */
    @NotNull
    private String identifier = DEFAULT_IDENTIFIER;

    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        String username = this.identifier;
        
        if (log.isInfoEnabled()) {
            log.info("Creating principal for: " + certificate.getSubjectDN().getName());
        }

        for (final Matcher regexMatcher = this.subjectRegex.matcher(certificate.getSubjectDN().getName()); regexMatcher.find();) {
            final String name = regexMatcher.group(1).trim();
            final String value;

            if(regexMatcher.group(2) != null) {
                value = regexMatcher.group(2);
            } else {
                value = regexMatcher.group(3);
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Parsed: %s - %s", name, value));
	        }

            username = username.replaceAll((new StringBuilder("\\$")).append(name).toString(), value);
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
