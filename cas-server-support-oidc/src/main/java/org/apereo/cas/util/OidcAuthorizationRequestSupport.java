package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * This is {@link OidcAuthorizationRequestSupport}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("oidcAuthorizationRequestSupport")
public class OidcAuthorizationRequestSupport {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;


    public Optional<Long> getOidcMaxAgeFromAuthorizationRequest(final WebContext context) {
        final URIBuilder builderContext = new URIBuilder(context.getFullRequestURL());
        final Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                .stream().filter(p -> p.getName().equals(OidcConstants.MAX_AGE))
                .findFirst();

        if (parameter.isPresent()) {
            final long maxAge = NumberUtils.toLong(parameter.get().getValue(), -1);
            return Optional.of(maxAge);
        }
        return Optional.empty();
    }

    public Optional<UserProfile> isAuthenticationProfileAvailable(final WebContext context) {
        final ProfileManager manager = new ProfileManager(context);
        final UserProfile profile = manager.get(true);
        if (profile != null) {
            return Optional.of(profile);
        }
        return Optional.empty();
    }

    public Optional<Authentication> isCasAuthenticationAvailable(final WebContext context) {
        final J2EContext j2EContext = (J2EContext) context;
        if (j2EContext != null) {
            final String tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(j2EContext.getRequest());

            if (StringUtils.isNotBlank(tgtId)) {
                final Authentication authentication = ticketRegistrySupport.getAuthenticationFrom(tgtId);
                if (authentication != null) {
                    return Optional.of(authentication);
                }
            }
        }
        return Optional.empty();
    }

    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final ZonedDateTime authenticationDate) {
        final Optional<Long> maxAge = getOidcMaxAgeFromAuthorizationRequest(context);
        if (maxAge.isPresent() && maxAge.get() > 0) {
            final long now = ZonedDateTime.now().toEpochSecond();
            final long authTime = authenticationDate.toEpochSecond();
            final long diffInSeconds = now - authTime;
            if (diffInSeconds > maxAge.get()) {
                logger.debug("Authentication is too old: {} and was created {} seconds ago.", 
                        authTime, diffInSeconds);
                return true;
            }
        }
        return false;
    }

    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final Authentication authentication) {
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authentication.getAuthenticationDate());
    }

    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final UserProfile profile) {

        final Object authTime =
                profile.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        if (authTime == null) {
            return false;
        }
        final ZonedDateTime dt = ZonedDateTime.parse(authTime.toString());
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, dt);
    }

    public void configureClientForMaxAgeAuthorizationRequest(final CasClient casClient, final WebContext context,
                                                             final Authentication authentication) {
        if (isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authentication)) {
            casClient.setRenew(true);
        }
    }

}
