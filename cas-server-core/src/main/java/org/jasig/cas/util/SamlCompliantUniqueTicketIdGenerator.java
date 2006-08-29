/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import java.security.MessageDigest;

import org.opensaml.artifact.SAMLArtifact;
import org.opensaml.artifact.SAMLArtifactType0001;
import org.springframework.util.Assert;

/**
 * Unique Ticket Id Generator compliant with the SAML 1.1 specification for
 * artifacts.
 * 
 * @author Scott
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class SamlCompliantUniqueTicketIdGenerator implements
    UniqueTicketIdGenerator {

    /** SAML defines the source id as the server name. */
    private final byte[] sourceIdDigest;

    /** Random generator to construct the AssertionHandle. */
    private final RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        20);

    public SamlCompliantUniqueTicketIdGenerator(final String sourceId) {
        Assert.notNull(sourceId, "sourceId cannot be null.");
        try {
            final MessageDigest messageDigest = MessageDigest
                .getInstance("SHA");
            messageDigest.update(sourceId.getBytes("8859_1"));
            this.sourceIdDigest = messageDigest.digest();
        } catch (final Exception e) {
            throw new IllegalStateException(
                "Exception generating digest which should not happen...EVER", e);
        }
    }

    /**
     * We ignore prefixes for SAML compliance.
     */
    public String getNewTicketId(String prefix) {
        final SAMLArtifact samlArtifactType = new SAMLArtifactType0001(
            this.sourceIdDigest, this.randomStringGenerator
                .getNewStringAsBytes());

        return samlArtifactType.encode();
    }
}
