package org.apereo.cas.web.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.soap.wsfed.RequestedSecurityToken;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This class represents an action in the webflow to retrieve WsFederation information on the callback url which is
 * the webflow url (/login).
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class WsFederationAction extends AbstractAction {

    private static final String WA = "wa";
    private static final String WRESULT = "wresult";
    private static final String WSIGNIN = "wsignin1.0";

    private final WsFederationHelper wsFederationHelper;
    private final Collection<WsFederationConfiguration> configurations;
    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;

    private final String themeParamName;
    private final String localParamName;

    private final WsFederationCookieManager wsFederationCookieManager;

    /**
     * Executes the webflow action.
     *
     * @param context the context
     * @return the event
     */
    @Override
    protected Event doExecute(final RequestContext context) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            final String wa = request.getParameter(WA);
            if (StringUtils.isNotBlank(wa) && wa.equalsIgnoreCase(WSIGNIN)) {
                return handleWsFederationAuthenticationRequest(context);
            }
            prepareLoginViewWithWsFederationClients(context);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, ex.getMessage());
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }

    private void prepareLoginViewWithWsFederationClients(final RequestContext context) {
        final List<WsFedClient> clients = new ArrayList<>();
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final Service service = (Service) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        this.configurations.forEach(cfg -> {
            final WsFedClient c = new WsFedClient();
            c.setName(cfg.getName());
            final String id = UUID.randomUUID().toString();
            final String rpId = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
            c.setAuthorizationUrl(cfg.getAuthorizationUrl(rpId, id));
            c.setReplyingPartyId(rpId);
            c.setId(id);
            c.setRedirectUrl(WsFederationNavigationController.getRelativeRedirectUrlFor(cfg, service, request));
            c.setAutoRedirect(cfg.isAutoRedirect());
            clients.add(c);
        });
        context.getFlowScope().put("wsfedUrls", clients);
    }


    private Event handleWsFederationAuthenticationRequest(final RequestContext context) {

        final Service service = wsFederationCookieManager.retrieve(context);
        LOGGER.debug("Retrieved service [{}] from the session cookie", service);

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final String wResult = request.getParameter(WRESULT);
        LOGGER.debug("Parameter [{}] received: [{}]", WRESULT, wResult);
        if (StringUtils.isBlank(wResult)) {
            LOGGER.error("No [{}] parameter is found", WRESULT);
            return error();
        }
        LOGGER.debug("Attempting to create an assertion from the token parameter");
        final RequestedSecurityToken rsToken = this.wsFederationHelper.getRequestSecurityTokenFromResult(wResult);
        final Pair<Assertion, WsFederationConfiguration> assertion = this.wsFederationHelper.buildAndVerifyAssertion(rsToken, configurations);
        if (assertion == null) {
            LOGGER.error("Could not validate assertion via parsing the token from [{}]", WRESULT);
            return error();
        }
        LOGGER.debug("Attempting to validate the signature on the assertion");
        if (!this.wsFederationHelper.validateSignature(assertion)) {
            final String msg = "WS Requested Security Token is blank or the signature is not valid.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return buildCredentialsFromAssertion(context, assertion, service);
    }

    private Event buildCredentialsFromAssertion(final RequestContext context,
                                                final Pair<Assertion, WsFederationConfiguration> assertion,
                                                final Service service) {
        try {
            LOGGER.debug("Creating credential based on the provided assertion");
            final WsFederationCredential credential = this.wsFederationHelper.createCredentialFromToken(assertion.getKey());
            final WsFederationConfiguration configuration = assertion.getValue();
            final String rpId = wsFederationHelper.getRelyingPartyIdentifier(service, configuration);

            if (credential == null) {
                LOGGER.error("SAML no credential could be extracted from [{}] based on RP identifier [{}] and IdP identifier [{}]",
                    assertion.getKey(), rpId, configuration.getIdentityProviderIdentifier());
                return error();
            }

            if (credential != null && credential.isValid(rpId, configuration.getIdentityProviderIdentifier(), configuration.getTolerance())) {
                LOGGER.debug("Validated assertion for the created credential successfully");
                if (configuration.getAttributeMutator() != null) {
                    LOGGER.debug("Modifying credential attributes based on [{}]", configuration.getAttributeMutator().getClass().getSimpleName());
                    configuration.getAttributeMutator().modifyAttributes(credential.getAttributes());
                }
            } else {
                LOGGER.error("SAML assertions are blank or no longer valid based on RP identifier [{}] and IdP identifier [{}]", rpId, configuration.getIdentityProviderIdentifier());
                return error();
            }
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            LOGGER.debug("Creating final authentication result based on the given credential");
            final AuthenticationResult authenticationResult = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
            LOGGER.debug("Attempting to create a ticket-granting ticket for the authentication result");
            WebUtils.putTicketGrantingTicketInScopes(context, this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult));
            LOGGER.info("Token validated and new [{}] created: [{}]", credential.getClass().getName(), credential);
            return success();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return error();
        }
    }
}
