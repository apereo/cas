package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.jose4j.jwt.ReservedClaimNames;
import org.pac4j.core.util.CommonHelper;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The message handler for the OIDC protocol.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Slf4j
public class OidcSingleLogoutServiceMessageHandler extends BaseSingleLogoutServiceMessageHandler {

    private final String issuer;

    public OidcSingleLogoutServiceMessageHandler(final HttpClient httpClient,
                                                 final SingleLogoutMessageCreator logoutMessageBuilder,
                                                 final ServicesManager servicesManager,
                                                 final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                 final boolean asynchronous,
                                                 final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                 final String issuer) {
        super(httpClient, logoutMessageBuilder, servicesManager, singleLogoutServiceLogoutUrlBuilder,
                asynchronous, authenticationRequestServiceSelectionStrategies);
        this.issuer = issuer;
    }

    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService) {
        return registeredService instanceof OidcRegisteredService;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Compute the logout requests.
     * For a front channel logout, the logout URL is supplemented with the issuer and session identifier.
     *
     * @param ticketId             the ticket id
     * @param selectedService      the selected service
     * @param registeredService    the registered service
     * @param logoutUrls           the logout urls
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the logout requests
     */
    @Override
    protected Collection<SingleLogoutRequest> createLogoutRequests(final String ticketId,
                                                                   final WebApplicationService selectedService,
                                                                   final RegisteredService registeredService,
                                                                   final Collection<SingleLogoutUrl> logoutUrls,
                                                                   final TicketGrantingTicket ticketGrantingTicket) {
        return logoutUrls
                .stream()
                .map(url -> {
                    var newSloUrl = url;
                    val logoutType = url.getLogoutType();
                    if (logoutType == RegisteredServiceLogoutType.FRONT_CHANNEL) {
                        var newUrl = CommonHelper.addParameter(url.getUrl(), ReservedClaimNames.ISSUER, issuer);
                        newUrl = CommonHelper.addParameter(newUrl, OidcConstants.CLAIM_SESSIOND_ID, DigestUtils.sha(ticketGrantingTicket.getId()));
                        newSloUrl = new SingleLogoutUrl(newUrl, logoutType);
                    }
                    return createLogoutRequest(ticketId, selectedService, registeredService, newSloUrl, ticketGrantingTicket);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean sendMessageToEndpoint(final LogoutHttpMessage msg, final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {

        val payload = logoutMessage.getPayload();

        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(msg.getUrl().toExternalForm(), CollectionUtils.wrap("logout_token", payload),
                    CollectionUtils.wrap("Content-Type", msg.getContentType()));

            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                LOGGER.trace("Received OK logout response");
                return true;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.warn("No (successful) logout response received from the url [{}]", msg.getUrl().toExternalForm());
        return false;
    }
}
