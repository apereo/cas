package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.pac4j.core.credentials.TokenCredentials;

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

    /**
     * PrincipalNameTransformer to be used by subclasses to transform the principal name.
     */
    private PrincipalNameTransformer principalNameTransformer = formUserId -> formUserId;

    /**
     * Default constructor.
     */
    public AbstractTokenWrapperAuthenticationHandler() {

    }

    @Override
    protected TokenCredentials convertToPac4jCredentials(final BasicIdentifiableCredential casCredential)
            throws GeneralSecurityException, PreventedException {
        logger.debug("CAS credentials: {}", casCredential);

        final String id = this.principalNameTransformer.transform(casCredential.getId());
        if (id == null) {
            throw new AccountNotFoundException("Id is null.");
        }
        final TokenCredentials credentials = new TokenCredentials(id, getClass().getSimpleName());
        logger.debug("pac4j credentials: {}", credentials);
        return credentials;
    }

    @Override
    protected Class<BasicIdentifiableCredential> getCasCredentialsType() {
        return BasicIdentifiableCredential.class;
    }

    public PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }
}
