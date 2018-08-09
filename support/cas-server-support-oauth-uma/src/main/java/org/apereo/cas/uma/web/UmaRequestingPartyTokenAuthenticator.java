package org.apereo.cas.uma.web;

import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

import java.util.LinkedHashMap;

/**
 * This is {@link UmaRequestingPartyTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class UmaRequestingPartyTokenAuthenticator implements Authenticator<TokenCredentials> {
    private final TicketRegistry ticketRegistry;

    @Override
    public void validate(final TokenCredentials credentials, final WebContext webContext) {
        val rpt = credentials.getToken();
        val at = this.ticketRegistry.getTicket(rpt, AccessToken.class);
        if (at == null || at.isExpired()) {
            throw new CredentialsException("Unable to locate requesting party access token " + rpt);
        }
        val profile = new CommonProfile();
        val authentication = at.getAuthentication();
        val principal = authentication.getPrincipal();
        profile.setId(principal.getId());
        val attributes = new LinkedHashMap<>(authentication.getAttributes());
        attributes.putAll(principal.getAttributes());
        profile.addAttributes(attributes);
        LOGGER.debug("Authenticated requesting party access token [{}]", profile);
        credentials.setUserProfile(profile);
    }
}
