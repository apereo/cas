package org.apereo.cas.digest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link DigestAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DigestAuthenticationHandler extends AbstractAuthenticationHandler {

    public DigestAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final DigestCredential c = (DigestCredential) credential;
        if (StringUtils.isNotBlank(c.getId()) && StringUtils.isNotBlank(c.getHash())) {
            return new DefaultHandlerResult(this, c, this.principalFactory.createPrincipal(c.getId()));
        }
        throw new FailedLoginException("Could not authenticate " + c.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof DigestCredential;
    }


}
