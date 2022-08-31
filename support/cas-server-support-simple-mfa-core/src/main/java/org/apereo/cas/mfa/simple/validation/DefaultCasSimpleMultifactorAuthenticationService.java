package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.Authentication;
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

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

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
    public Principal validate(final Authentication authentication,
                              final CasSimpleMultifactorTokenCredential credential) throws Exception {
        val uid = authentication.getPrincipal().getId();
        val tokenId = normalize(credential.getId());
        LOGGER.debug("Received token [{}] and pricipal id [{}]", tokenId, uid);
        val acct = ticketRegistry.getTicket(tokenId, CasSimpleMultifactorAuthenticationTicket.class);
        val properties = acct.getProperties();
        if (!properties.containsKey(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL)) {
            LOGGER.warn("Unable to locate principal for token [{}]", tokenId);
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + tokenId);
        }
        val principal = (Principal) properties.get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL);
        if (!principal.equals(authentication.getPrincipal())) {
            LOGGER.warn("Principal assigned to token [{}] is unauthorized for token [{}]", principal.getId(), tokenId);
            deleteToken(acct);
            throw new FailedLoginException("Failed to authenticate code " + tokenId);
        }
        deleteToken(acct);
        LOGGER.debug("Validated token [{}] successfully for [{}].", tokenId, uid);
        return principal;
    }

    /**
     * Normalize ticket.
     *
     * @param tokenId the token id
     * @return the string
     */
    private static String normalize(final String tokenId) {
        if (!tokenId.startsWith(CasSimpleMultifactorAuthenticationTicket.PREFIX)) {
            return CasSimpleMultifactorAuthenticationTicket.PREFIX + UniqueTicketIdGenerator.SEPARATOR + tokenId;
        }
        return tokenId;
    }

    /**
     * Delete token.
     *
     * @param acct the acct
     */
    protected void deleteToken(final CasSimpleMultifactorAuthenticationTicket acct) {
        FunctionUtils.doUnchecked(s -> ticketRegistry.deleteTicket(acct.getId()));
    }
}
