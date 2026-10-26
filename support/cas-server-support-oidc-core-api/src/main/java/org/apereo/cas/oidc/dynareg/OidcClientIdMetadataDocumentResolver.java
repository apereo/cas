package org.apereo.cas.oidc.dynareg;

import module java.base;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcClientIdMetadataDocumentResolver}. It makes clients that identify themselves with an OAuth client ID
 * metadata document known to CAS: the first time such a client id is seen, and whenever the document's cache lifetime
 * has passed, the document is fetched and the client is registered or updated from it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OidcClientIdMetadataDocumentResolver {
    /**
     * Bean name.
     */
    String BEAN_NAME = "oidcClientIdMetadataDocumentResolver";

    /**
     * Hosts of redirect URIs on the person's own computer, which may use {@code http}.
     */
    Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");

    /**
     * Make sure the client with this id is registered from its document, when the client id is a document URL.
     *
     * @param clientId the client id, as presented by the client
     * @return the registered client, or empty when the client id is not a document URL or its document cannot be used
     */
    Optional<OidcRegisteredService> resolve(@Nullable String clientId);

    /**
     * Whether a client id has the shape of a client ID metadata document URL.
     *
     * @param clientId the client id
     * @return true for {@code https://} client ids
     */
    static boolean isClientIdMetadataDocumentUrl(@Nullable final String clientId) {
        return Strings.CS.startsWith(clientId, "https://");
    }

    /**
     * Whether a service was registered from a client ID metadata document.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    static boolean isClientIdMetadataDocumentClient(final OidcRegisteredService registeredService) {
        val property = registeredService.getProperties()
            .get(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT.getPropertyName());
        return property != null && property.getBooleanValue();
    }
}
