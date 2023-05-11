package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;

/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultCasSimpleMultifactorAuthenticationService implements CasSimpleMultifactorAuthenticationService {

    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    @Override
    public CasSimpleMultifactorAuthenticationTicket generate(final Principal principal, final Service service) throws Exception {
        val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val token = mfaFactory.create(service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
        return token;
    }

    @Override
    public void store(final CasSimpleMultifactorAuthenticationTicket token) {
        token.update();
        val trackingToken = ticketRegistry.getTicket(token.getId());
        FunctionUtils.doUnchecked(us -> {
            if (trackingToken != null) {
                LOGGER.debug("Updating existing token [{}] to registry", token.getId());
                ticketRegistry.updateTicket(trackingToken);
            } else {
                LOGGER.debug("Adding token [{}] to registry", token.getId());
                ticketRegistry.addTicket(token);
            }
        });
    }

    @Override
    public Principal validate(final Principal resolvedPrincipal,
                              final CasSimpleMultifactorTokenCredential credential) throws Exception {
        val acct = getMultitfactorAuthenticationTicketFor(resolvedPrincipal, credential);
        val principal = validateTokenForPrincipal(resolvedPrincipal, acct);
        deleteToken(acct);
        LOGGER.debug("Validated token [{}] successfully for [{}].", credential.getId(), resolvedPrincipal.getId());
        return principal;
    }

    protected CasSimpleMultifactorAuthenticationTicket getMultitfactorAuthenticationTicketFor(final Principal resolvedPrincipal,
                                                                                              final CasSimpleMultifactorTokenCredential credential) {
        val tokenId = normalize(credential.getId());
        LOGGER.debug("Received token [{}] and pricipal id [{}]", tokenId, resolvedPrincipal.getId());
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
