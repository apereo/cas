package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import javax.security.auth.login.FailedLoginException;

/**
 * This is {@link BaseCasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseCasSimpleMultifactorAuthenticationService implements CasSimpleMultifactorAuthenticationService {
    protected final TicketRegistry ticketRegistry;

    @Override
    public CasSimpleMultifactorAuthenticationTicket getMultifactorAuthenticationTicket(final CasSimpleMultifactorTokenCredential credential) {
        val tokenId = normalize(credential.getId());
        return ticketRegistry.getTicket(tokenId, CasSimpleMultifactorAuthenticationTicket.class);
    }

    protected Principal validateTokenForPrincipal(final Principal resolvedPrincipal, final CasSimpleMultifactorAuthenticationTicket acct)
        throws FailedLoginException {
        if (!acct.getProperties().containsKey(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL)) {
            LOGGER.warn("Unable to locate principal for token [{}]", acct.getId());
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + acct.getId());
        }
        val principal = (Principal) acct.getProperties().get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL);
        if (!principal.equals(resolvedPrincipal)) {
            LOGGER.warn("Principal assigned to token [{}] is unauthorized for token [{}]", principal.getId(), acct.getId());
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + acct.getId());
        }
        return principal;
    }

    protected static String normalize(final String tokenId) {
        if (!tokenId.startsWith(CasSimpleMultifactorAuthenticationTicket.PREFIX)) {
            return CasSimpleMultifactorAuthenticationTicket.PREFIX + UniqueTicketIdGenerator.SEPARATOR + tokenId;
        }
        return tokenId;
    }

    protected void deleteToken(final CasSimpleMultifactorAuthenticationTicket acct) {
        FunctionUtils.doUnchecked(__ -> ticketRegistry.deleteTicket(acct.getId()));
    }

}
