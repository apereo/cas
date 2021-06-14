package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;

/**
 * This is {@link OidcJwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class OidcJwtBuilder extends OAuth20JwtBuilder {
    public OidcJwtBuilder(final OidcIssuerService issuerService,
                          final CipherExecutor defaultTokenCipherExecutor,
                          final ServicesManager servicesManager,
                          final RegisteredServiceCipherExecutor registeredServiceCipherExecutor) {
        super(defaultTokenCipherExecutor, servicesManager, registeredServiceCipherExecutor);
    }
}
