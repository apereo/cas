package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Token Status List settings of the verifiable credential issuer, with which issued credentials can be revoked or
 * suspended.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialStatusListProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5310871642910367205L;

    /**
     * Whether issued SD-JWT VC credentials carry a {@code status} claim that points into a status list published by
     * CAS. Entries are kept in the ticket registry, so the status of a credential survives restarts and is shared
     * between nodes; a stateless ticket registry cannot keep them, and credentials are then issued without status.
     */
    private boolean enabled;

    /**
     * Number of entries in each status list. Credentials get random, unpredictable indexes within a list, and a new list
     * is started once a list has little room left. Each entry takes 2 bits, so a multiple of 4 fills whole bytes.
     */
    private int size = 131_072;

    /**
     * How long a verifier may cache a status list token before fetching it again, advertised as its {@code ttl} claim.
     * CAS also caches the token it builds for this long.
     */
    @DurationCapable
    private String timeToLive = "PT10M";

    /**
     * How long a status list token is valid after it is issued, advertised as its {@code exp} claim.
     */
    @DurationCapable
    private String expiration = "PT24H";
}
