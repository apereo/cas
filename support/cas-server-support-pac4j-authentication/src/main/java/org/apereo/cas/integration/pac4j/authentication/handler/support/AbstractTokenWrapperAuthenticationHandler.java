package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.credentials.TokenCredentials;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Pac4j authentication handler which works on a CAS identifiable credential
 * and uses a pac4j authenticator and profile creator to play authentication.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public abstract class AbstractTokenWrapperAuthenticationHandler extends
    AbstractWrapperAuthenticationHandler<BasicIdentifiableCredential, TokenCredentials> {

    /**
     * PrincipalNameTransformer to be used by subclasses to transform the principal name.
     */
    private final PrincipalNameTransformer principalNameTransformer;

    public AbstractTokenWrapperAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory,
                                                     final Integer order, final PrincipalNameTransformer principalNameTransformer) {
        super(name, servicesManager, principalFactory, order);
        this.principalNameTransformer = Objects.requireNonNullElseGet(principalNameTransformer, () -> formUserId -> formUserId);
    }

    @Override
    protected TokenCredentials convertToPac4jCredentials(final BasicIdentifiableCredential casCredential)
        throws GeneralSecurityException {
        LOGGER.debug("CAS credentials: [{}]", casCredential);

        val id = this.principalNameTransformer.transform(casCredential.getId());
        if (id == null) {
            throw new AccountNotFoundException("Id is null.");
        }
        val credentials = new TokenCredentials(id);
        LOGGER.debug("pac4j credentials: [{}]", credentials);
        return credentials;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return BasicIdentifiableCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected Class<BasicIdentifiableCredential> getCasCredentialsType() {
        return BasicIdentifiableCredential.class;
    }
}
