package org.apereo.cas.oidc.dynareg;

import module java.base;

/**
 * This is {@link OidcClientIdMetadataDocumentFetcher}, which fetches the OAuth client ID metadata document of a client.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OidcClientIdMetadataDocumentFetcher {

    /**
     * Fetch the document published at the client id URL.
     *
     * @param clientId the client id URL
     * @return the document
     * @throws Exception when the document cannot be fetched or may not be fetched
     */
    OidcClientIdMetadataDocument fetch(URI clientId) throws Exception;
}
