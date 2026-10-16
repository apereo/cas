package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OidcDefaultTokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OidcDefaultTokenGenerator extends OAuth20DefaultTokenGenerator {
    public OidcDefaultTokenGenerator(final TicketFactory ticketFactory,
                                     final TicketRegistry ticketRegistry,
                                     final PrincipalResolver principalResolver,
                                     final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
                                     final ConfigurableApplicationContext applicationContext,
                                     final CasConfigurationProperties casProperties) {
        super(ticketFactory, ticketRegistry, principalResolver,
            profileScopeToAttributesFilter, applicationContext, casProperties);
    }

    @Override
    public OAuth20TokenGeneratedResult generate(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        consumeCibaRequest(tokenRequestContext);
        return super.generate(tokenRequestContext);
    }

    /*
     * CIBA Core 1.0, section 10.1.1: once redeemed, the auth_req_id is no longer valid. The request is removed before
     * tokens are issued, and only the token request whose delete removed it gets them; the others fail with invalid_grant.
     */
    private void consumeCibaRequest(final AccessTokenRequestContext tokenRequestContext) throws Exception {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.CIBA
            && StringUtils.isNotBlank(tokenRequestContext.getCibaRequestId())
            && tokenRequestContext.getRegisteredService() instanceof final OidcRegisteredService registeredService) {
            val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(
                registeredService.getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
            if (deliveryMode == OidcBackchannelTokenDeliveryModes.POLL
                || deliveryMode == OidcBackchannelTokenDeliveryModes.PING) {
                val cibaFactory = (OidcCibaRequestFactory) ticketFactory.get(OidcCibaRequest.class);
                val cibaRequestId = cibaFactory.decodeId(tokenRequestContext.getCibaRequestId());
                if (ticketRegistry.deleteTicket(cibaRequestId) == 0) {
                    LOGGER.warn("CIBA request [{}] was already redeemed by another token request", cibaRequestId);
                    throw new InvalidTicketException(cibaRequestId);
                }
            }
        }
    }

    @Override
    protected Authentication finalizeAuthentication(final AccessTokenRequestContext tokenRequestContext,
                                                    final AuthenticationBuilder authenticationBuilder) {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.CIBA) {
            FunctionUtils.doIfNotNull(tokenRequestContext.getCibaRequestId(),
                _ -> authenticationBuilder.addAttribute(OidcConstants.AUTH_REQ_ID, tokenRequestContext.getCibaRequestId()));
        }

        return super.finalizeAuthentication(tokenRequestContext, authenticationBuilder);
    }
}
