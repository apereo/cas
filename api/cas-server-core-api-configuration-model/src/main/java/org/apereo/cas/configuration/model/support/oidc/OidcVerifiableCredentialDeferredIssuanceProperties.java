package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Deferred issuance settings of the verifiable credential issuer. Credential configurations that turn on deferred issuance
 * answer credential requests with a transaction the wallet collects later, once the transaction is approved.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialDeferredIssuanceProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4720193865102347761L;

    /**
     * How long a wallet should wait before asking for the credentials of a deferred transaction again, advertised as the
     * {@code interval} of the credential response.
     */
    @DurationCapable
    private String interval = "PT5M";

    /**
     * How long a deferred transaction is kept. A transaction that is neither approved and collected nor denied within this
     * time expires, and the wallet is told its {@code transaction_id} is invalid.
     */
    @DurationCapable
    private String timeToLive = "P7D";
}
