/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import org.opensaml.artifact.SAMLArtifact;
import org.opensaml.artifact.SAMLArtifactType0002;
import org.opensaml.artifact.URI;
import org.springframework.util.Assert;

/**
 * Unique Ticket Id Generator compliant with the SAML 1.1 specification for
 * artifacts.  This should also be compliant with the SAML 2 specification.
 * 
 * @author Scott
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SamlCompliantUniqueTicketIdGenerator implements
    UniqueTicketIdGenerator {

    /** SAML defines the source id as the server name. */
  /**  private final byte[] sourceIdDigest; **/
    
    private final String sourceLocation;

    /** Random generator to construct the AssertionHandle. */
    private final RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        20);

    public SamlCompliantUniqueTicketIdGenerator(final String sourceId) {
        Assert.notNull(sourceId, "sourceId cannot be null.");
        this.sourceLocation = sourceId;
    }

    /**
     * We ignore prefixes for SAML compliance.
     */
    public String getNewTicketId(final String prefix) {
        final SAMLArtifact samlArtifact = new SAMLArtifactType0002(this.randomStringGenerator
            .getNewStringAsBytes(), new URI(this.sourceLocation));

        return samlArtifact.encode();
    }
}
