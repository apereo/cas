package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.jose4j.jwt.ReservedClaimNames;
import org.pac4j.core.util.CommonHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The message handler for the OIDC protocol.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Slf4j
public class OidcSingleLogoutServiceMessageHandler extends BaseSingleLogoutServiceMessageHandler {

    private final OidcIssuerService issuerService;

    public OidcSingleLogoutServiceMessageHandler(final HttpClient httpClient,
                                                 final SingleLogoutMessageCreator logoutMessageBuilder,
                                                 final ServicesManager servicesManager,
                                                 final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                 final boolean asynchronous,
                                                 final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                 final OidcIssuerService issuerService) {
        super(httpClient, logoutMessageBuilder, servicesManager, singleLogoutServiceLogoutUrlBuilder,
            asynchronous, authenticationRequestServiceSelectionStrategies);
        this.issuerService = issuerService;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService,
                                       final SingleLogoutExecutionRequest context) {
        return registeredService instanceof OidcRegisteredService;
    }

    @Override
    protected Collection<SingleLogoutRequestContext> createLogoutRequests(final String ticketId,
                                                                          final WebApplicationService selectedService,
                                                                          final RegisteredService registeredService,
                                                                          final Collection<SingleLogoutUrl> logoutUrls,
                                                                          final SingleLogoutExecutionRequest context) {
        return logoutUrls
            .stream()
            .map(url -> {
                var newSloUrl = url;
                val logoutType = url.getLogoutType();
                if (logoutType == RegisteredServiceLogoutType.FRONT_CHANNEL) {
                    var newUrl = CommonHelper.addParameter(url.getUrl(), ReservedClaimNames.ISSUER,
                        issuerService.determineIssuer(Optional.empty()));
                    newUrl = CommonHelper.addParameter(newUrl, OidcConstants.CLAIM_SESSION_ID,
                        DigestUtils.sha(context.getTicketGrantingTicket().getId()));
                    newSloUrl = new SingleLogoutUrl(newUrl, logoutType);
                }
                return createLogoutRequest(ticketId, selectedService, registeredService, newSloUrl, context);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    protected boolean sendMessageToEndpoint(final LogoutHttpMessage msg, final SingleLogoutRequestContext request, final SingleLogoutMessage logoutMessage) {

        val payload = logoutMessage.getPayload();
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(msg.getUrl().toExternalForm())
                .parameters(CollectionUtils.wrap("logout_token", payload))
                .headers(CollectionUtils.wrap("Content-Type", msg.getContentType()))
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && !Objects.requireNonNull(HttpStatus.resolve(response.getStatusLine().getStatusCode())).isError()) {
                LOGGER.trace("Received OK logout response");
                return true;
            }
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.warn("No (successful) logout response received from the url [{}]", msg.getUrl().toExternalForm());
        return false;
    }
}
