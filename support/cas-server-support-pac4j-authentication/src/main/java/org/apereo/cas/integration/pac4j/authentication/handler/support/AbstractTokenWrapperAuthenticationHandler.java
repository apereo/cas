package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.credentials.TokenCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which works on a CAS identifiable credential
 * and uses a pac4j authenticator and profile creator to play authentication.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public abstract class AbstractTokenWrapperAuthenticationHandler extends
        AbstractWrapperAuthenticationHandler<BasicIdentifiableCredential, TokenCredentials> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTokenWrapperAuthenticationHandler.class);
    
    /**
     * PrincipalNameTransformer to be used by subclasses to transform the principal name.
     */
    private PrincipalNameTransformer principalNameTransformer = formUserId -> formUserId;

    public AbstractTokenWrapperAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                     final Integer order, final PrincipalNameTransformer principalNameTransformer) {
        super(name, servicesManager, principalFactory, order);
        if (principalNameTransformer == null) {
            this.principalNameTransformer = formUserId -> formUserId;
        } else {
            this.principalNameTransformer = principalNameTransformer;
        }
    }

    @Override
    protected TokenCredentials convertToPac4jCredentials(final BasicIdentifiableCredential casCredential)
            throws GeneralSecurityException {
        LOGGER.debug("CAS credentials: [{}]", casCredential);

        final String id = this.principalNameTransformer.transform(casCredential.getId());
        if (id == null) {
            throw new AccountNotFoundException("Id is null.");
        }
        final TokenCredentials credentials = new TokenCredentials(id, getClass().getSimpleName());
        LOGGER.debug("pac4j credentials: [{}]", credentials);
        return credentials;
    }

    @Override
    protected Class<BasicIdentifiableCredential> getCasCredentialsType() {
        return BasicIdentifiableCredential.class;
    }

    public PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }
}
