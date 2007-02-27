/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Abstract implementation of a WebApplicationService.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public abstract class AbstractWebApplicationService implements WebApplicationService {

    /** The id of the service. */
    private final String id;
    
    /** The original url provided, used to reconstruct the redirect url. */
    private final String originalUrl;

    private final String artifactId;
    
    protected AbstractWebApplicationService(final String id, final String originalUrl, final String artifactId) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.artifactId = artifactId;
    }
    
    public final String getId() {
        return this.id;
    }
    
    public final String getArtifactId() {
        return this.artifactId;
    }
    
    protected static final String cleanupUrl(final String url) {
        final int jsessionPosition = url.indexOf(";jsession");

        if (jsessionPosition == -1) {
            return url;
        }

        final int questionMarkPosition = url.indexOf("?");

        if (questionMarkPosition < jsessionPosition) {
            return url.substring(0, url.indexOf(";jsession"));
        }

        return url.substring(0, jsessionPosition)
            + url.substring(questionMarkPosition);
    }
    
    protected final String getOriginalUrl() {
        return this.originalUrl;
    }

    public final boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Service) {
            final Service service = (Service) object;

            return getId().equals(service.getId());
        }

        return false;
    }
}
