package org.apereo.cas.logout.slo;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.HttpMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link BaseSingleLogoutServiceMessageHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseSingleLogoutServiceMessageHandler implements SingleLogoutServiceMessageHandler {
    private final HttpClient httpClient;

    private final SingleLogoutMessageCreator logoutMessageBuilder;

    private final ServicesManager servicesManager;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final boolean asynchronous;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Override
    public Collection<SingleLogoutRequestContext> handle(final WebApplicationService singleLogoutService,
                                                         final String ticketId,
                                                         final SingleLogoutExecutionRequest context) {
        if (singleLogoutService.isLoggedOutAlready()) {
            LOGGER.debug("Service [{}] is already logged out.", singleLogoutService);
            return new ArrayList<>();
        }
        val selectedService = FunctionUtils.doUnchecked(() ->
            (WebApplicationService) authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService));

        LOGGER.trace("Processing logout request for service [{}]...", selectedService);
        val registeredService = servicesManager.findServiceBy(selectedService);

        LOGGER.debug("Service [{}] supports single logout and is found in the registry as [{}]. Proceeding...",
            selectedService.getId(), registeredService.getName());

        val logoutUrls = singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, selectedService);
        LOGGER.debug("Prepared logout url [{}] for service [{}]", logoutUrls, selectedService);
        if (logoutUrls == null || logoutUrls.isEmpty()) {
            LOGGER.debug("Service [{}] does not support logout operations given no logout url could be determined.", selectedService);
            return new ArrayList<>();
        }

        LOGGER.trace("Creating logout request for [{}] and ticket id [{}]", selectedService, ticketId);
        return createLogoutRequests(ticketId, selectedService, registeredService, logoutUrls, context);
    }

    @Override
    public boolean supports(final SingleLogoutExecutionRequest context, final WebApplicationService singleLogoutService) {
        val selectedService = FunctionUtils.doUnchecked(() ->
            (WebApplicationService) authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService));
        val registeredService = (WebBasedRegisteredService) this.servicesManager.findServiceBy(selectedService);

        return registeredService != null
            && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, selectedService)
            && registeredService.getLogoutType() != RegisteredServiceLogoutType.NONE
            && supportsInternal(singleLogoutService, registeredService, context);
    }

    @Override
    public boolean performBackChannelLogout(final SingleLogoutRequestContext request) {
        try {
            LOGGER.trace("Creating back-channel logout request based on [{}]", request);
            val logoutRequest = createSingleLogoutMessage(request);
            return sendSingleLogoutMessage(request, logoutRequest);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public SingleLogoutMessage createSingleLogoutMessage(final SingleLogoutRequestContext logoutRequest) throws Throwable {
        return this.logoutMessageBuilder.create(logoutRequest);
    }

    protected boolean supportsInternal(final WebApplicationService singleLogoutService,
                                       final RegisteredService registeredService,
                                       final SingleLogoutExecutionRequest context) {
        return true;
    }

    protected Collection<SingleLogoutRequestContext> createLogoutRequests(final String ticketId,
                                                                          final WebApplicationService selectedService,
                                                                          final RegisteredService registeredService,
                                                                          final Collection<SingleLogoutUrl> logoutUrls,
                                                                          final SingleLogoutExecutionRequest context) {
        return logoutUrls
            .stream()
            .map(url -> createLogoutRequest(ticketId, selectedService, registeredService, url, context))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected SingleLogoutRequestContext createLogoutRequest(final String ticketId,
                                                             final WebApplicationService selectedService,
                                                             final RegisteredService registeredService,
                                                             final SingleLogoutUrl logoutUrl,
                                                             final SingleLogoutExecutionRequest context) {
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .ticketId(ticketId)
            .service(selectedService)
            .logoutUrl(FunctionUtils.doUnchecked(() -> new URI(logoutUrl.getUrl()).toURL()))
            .logoutType(logoutUrl.getLogoutType())
            .registeredService(registeredService)
            .executionRequest(context)
            .properties(logoutUrl.getProperties())
            .build();

        LOGGER.trace("Logout request [{}] created for [{}] and ticket id [{}]", logoutRequest, selectedService, ticketId);
        if (logoutRequest.getLogoutType() == RegisteredServiceLogoutType.BACK_CHANNEL) {
            if (performBackChannelLogout(logoutRequest)) {
                logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
            } else {
                logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                LOGGER.warn("Logout message is not sent to [{}]; Continuing processing...", selectedService);
            }
        } else {
            LOGGER.trace("Logout operation is not yet attempted for [{}] given logout type is set to [{}]",
                selectedService, logoutRequest.getLogoutType());
            logoutRequest.setStatus(LogoutRequestStatus.NOT_ATTEMPTED);
        }
        return logoutRequest;
    }

    protected boolean sendSingleLogoutMessage(final SingleLogoutRequestContext request, final SingleLogoutMessage logoutMessage) {
        val logoutService = request.getService();
        LOGGER.trace("Preparing logout request for [{}] to [{}]", logoutService.getId(), request.getLogoutUrl());
        val msg = prepareLogoutHttpMessageToSend(request, logoutMessage);
        LOGGER.debug("Prepared logout message to send is [{}]. Sending...", msg);
        val result = sendMessageToEndpoint(msg, request, logoutMessage);
        logoutService.setLoggedOutAlready(result);
        return result;
    }

    protected boolean sendMessageToEndpoint(final HttpMessage msg,
                                            final SingleLogoutRequestContext request,
                                            final SingleLogoutMessage logoutMessage) {
        return this.httpClient.sendMessageToEndPoint(msg);
    }
    
    @Override
    public HttpMessage prepareLogoutHttpMessageToSend(final SingleLogoutRequestContext request, final SingleLogoutMessage logoutMessage) {
        return new LogoutHttpMessage(CasProtocolConstants.PARAMETER_LOGOUT_REQUEST, request.getLogoutUrl(), logoutMessage.getPayload(), this.asynchronous);
    }
}
