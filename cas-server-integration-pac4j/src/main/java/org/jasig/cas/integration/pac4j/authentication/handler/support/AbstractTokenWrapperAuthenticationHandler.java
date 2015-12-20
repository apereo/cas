package org.jasig.cas.integration.pac4j.authentication.handler.support;

import org.jasig.cas.authentication.BasicIdentifiableCredential;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.pac4j.http.credentials.TokenCredentials;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /**
     * Default constructor.
     */
    public AbstractTokenWrapperAuthenticationHandler() {
        setTypedIdUsed(false);
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
        return principalNameTransformer;
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }
}
