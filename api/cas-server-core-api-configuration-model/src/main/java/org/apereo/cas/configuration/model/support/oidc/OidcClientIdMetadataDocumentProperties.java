package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcClientIdMetadataDocumentProperties}, which controls OAuth client ID metadata documents: clients
 * whose client id is an HTTPS URL that CAS fetches to learn the client's metadata, without prior registration.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcClientIdMetadataDocumentProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6024983134981652731L;

    /**
     * Whether clients may use client ID metadata documents. When enabled, CAS fetches the document at the client id URL
     * when it first sees the client, registers the client from it, advertises the support in its discovery document and
     * always asks people for consent, showing the host of the client id. Documents are only fetched from public
     * addresses; private, loopback and link-local addresses are always refused.
     */
    private boolean enabled;

    /**
     * The shortest time a fetched document is used before CAS fetches it again, also used when the response carries no
     * cache headers or forbids caching.
     */
    @DurationCapable
    private String minimumCacheDuration = "PT5M";

    /**
     * The longest time a fetched document is used before CAS fetches it again, whatever its cache headers allow.
     */
    @DurationCapable
    private String maximumCacheDuration = "PT24H";

    /**
     * How long a client stays registered after its document was last fetched successfully. Clients that are not used
     * for this long expire and are removed from the service registry.
     */
    @DurationCapable
    private String retention = "P30D";

    /**
     * The maximum number of clients registered from client ID metadata documents. New clients are refused once it is
     * reached, until existing ones expire.
     */
    private int maximumClients = 1000;

    /**
     * Maximum time to wait for the document.
     */
    @DurationCapable
    private String timeout = "PT5S";
}
