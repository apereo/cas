package org.apereo.cas.oidc.dynareg;

import module java.base;

/**
 * This is {@link OidcClientIdMetadataDocument}, an OAuth client ID metadata document as fetched from its client id URL.
 *
 * @author Misagh Moayyed
 * @param clientId      the client id, which is the URL the document was fetched from
 * @param json          the document
 * @param cacheLifetime how long the response allows the document to be used, before the CAS bounds are applied
 * @since 8.1.0
 */
public record OidcClientIdMetadataDocument(String clientId, String json, Duration cacheLifetime) {
}
