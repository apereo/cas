package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.logout.LogoutConfirmationResolver;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DelegatedSaml2ClientFinishLogoutAction}.
 * <p>
 * The action takes into account the currently used PAC4J client which is stored
 * in the user profile. If the client is found, its logout action is executed.
 * <p>
 * Assumption: The PAC4J user profile should be in the user session during
 * logout, accessible with PAC4J Profile Manager. The Logout web flow should
 * make sure the user profile is present.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedSaml2ClientFinishLogoutAction extends BaseCasWebflowAction {
    private final DelegatedIdentityProviders identityProviders;

    private final SessionStore sessionStore;

    private final OpenSamlConfigBean openSamlConfigBean;

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    private final ServiceFactory serviceFactory;

    private final LogoutConfirmationResolver logoutConfirmationResolver;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (logoutConfirmationResolver.isLogoutRequestConfirmed(requestContext)) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val context = new JEEContext(request, response);

            var clientName = DelegationWebflowUtils.getDelegatedAuthenticationClientName(requestContext);
            if (clientName == null) {
                clientName = requestContext.getRequestParameters().get(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);
                if (StringUtils.isNotBlank(clientName)) {
                    identityProviders.findClient(clientName, context)
                        .filter(SAML2Client.class::isInstance)
                        .map(SAML2Client.class::cast)
                        .ifPresent(client -> FunctionUtils.doAndHandle(__ -> {
                            client.init();
                            LOGGER.debug("Located client from relay-state [{}]", client);
                            val callContext = new CallContext(context, sessionStore);
                            client.getCredentialsExtractor().extract(callContext).ifPresent(logoutCredentials -> {
                                val result = client.getLogoutProcessor().processLogout(callContext, logoutCredentials);
                                JEEHttpActionAdapter.INSTANCE.adapt(result, context);
                            });
                        }));
                }
            } else {
                val logoutRedirect = WebUtils.getLogoutRedirectUrl(requestContext, String.class);
                val delegatedClientLogoutRequest = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(requestContext,
                    DelegatedAuthenticationClientLogoutRequest.class);
                identityProviders.findClient(clientName, context)
                    .filter(SAML2Client.class::isInstance)
                    .map(SAML2Client.class::cast)
                    .ifPresent(client -> {
                        client.init();
                        Optional.ofNullable(delegatedClientLogoutRequest)
                            .filter(__ -> StringUtils.isNotBlank(logoutRedirect))
                            .filter(__ -> StringUtils.isNotBlank(delegatedClientLogoutRequest.getLocation()))
                            .ifPresent(Unchecked.consumer(__ -> {
                                try {
                                    val urlBuilder = new URIBuilder(delegatedClientLogoutRequest.getLocation());
                                    val samlRequestParam = Optional.ofNullable(urlBuilder.getFirstQueryParam(SamlProtocolConstants.PARAMETER_SAML_REQUEST));
                                    samlRequestParam.ifPresent(Unchecked.consumer(samlRequest -> {
                                        val logoutRequest = SamlUtils.convertToSamlObject(openSamlConfigBean, samlRequest.getValue(), LogoutRequest.class);
                                        val service = serviceFactory.createService(logoutRequest.getIssuer().getValue());
                                        service.setTenant(ClientInfoHolder.getClientInfo().getTenant());

                                        val logoutRequestTicketId = TransientSessionTicketFactory.normalizeTicketId(logoutRequest.getID());
                                        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                                        val transientSessionTicket = transientFactory.create(logoutRequestTicketId,
                                            service,
                                            Map.of(DelegatedAuthenticationClientLogoutRequest.class.getName(), delegatedClientLogoutRequest)
                                        );
                                        val storedTicket = ticketRegistry.addTicket(transientSessionTicket);
                                        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequestTicket(requestContext, storedTicket);
                                    }));
                                } finally {
                                    LOGGER.debug("Captured post logout url: [{}]", logoutRedirect);
                                    WebUtils.putLogoutRedirectUrl(requestContext, null);
                                }
                            }));
                    });
            }
        }
        return null;
    }

}
