package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2aslo.Asynchronous;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is {@link SamlIdPSingleLogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class SamlIdPSingleLogoutRedirectionStrategy implements LogoutRedirectionStrategy {
    private final SamlProfileHandlerConfigurationContext configurationContext;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean supports(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val registeredService = WebUtils.getRegisteredService(request);
        if (registeredService instanceof SamlRegisteredService) {
            val logout = configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
            val samlRegisteredService = (SamlRegisteredService) registeredService;
            val sloRequest = WebUtils.getSingleLogoutRequest(request);
            val async = new AtomicBoolean(false);

            if (StringUtils.isNotBlank(sloRequest)) {
                async.set(getLogoutRequest(request)
                    .map(RequestAbstractType::getExtensions)
                    .stream()
                    .filter(Objects::nonNull)
                    .anyMatch(extensions -> !extensions.getUnknownXMLObjects(Asynchronous.DEFAULT_ELEMENT_NAME).isEmpty()));
            }
            return logout.isSendLogoutResponse()
                && samlRegisteredService != null
                && samlRegisteredService.isLogoutResponseEnabled()
                && sloRequest != null
                && !async.get();
        }
        return false;
    }

    @Override
    public void handle(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val samlRegisteredService = (SamlRegisteredService) WebUtils.getRegisteredService(request);
        val samlLogoutRequest = getLogoutRequest(request).get();

        val logoutRequestIssuer = SamlIdPUtils.getIssuerFromSamlObject(samlLogoutRequest);
        val adaptorRes = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(),
            samlRegisteredService, logoutRequestIssuer);

        if (adaptorRes.isEmpty()) {
            LOGGER.warn("Cannot find service provider metadata entity linked to [{}]", logoutRequestIssuer);
        } else {
            val adaptor = adaptorRes.get();
            val binding = determineLogoutResponseBindingType(samlRegisteredService);
            LOGGER.debug("Logout response binding type is determined as [{}]", binding);

            switch (StringUtils.defaultString(binding)) {
                case SAMLConstants.SAML2_POST_BINDING_URI:
                    handleSingleLogoutForPostBinding(context, samlLogoutRequest, samlRegisteredService, adaptor);
                    break;
                case SAMLConstants.SAML2_REDIRECT_BINDING_URI:
                default:
                    handleSingleLogoutForRedirectBinding(context, samlLogoutRequest, samlRegisteredService, adaptor);
                    break;
            }
        }
    }

    /**
     * Determine logout response binding type string.
     *
     * @param samlRegisteredService the saml registered service
     * @return the string
     */
    protected String determineLogoutResponseBindingType(final SamlRegisteredService samlRegisteredService) {
        val logout = configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
        return StringUtils.defaultIfBlank(samlRegisteredService.getLogoutResponseBinding(), logout.getLogoutResponseBinding());
    }

    /**
     * Handle single logout for redirect binding.
     *
     * @param context               the context
     * @param samlLogoutRequest     the saml logout request
     * @param samlRegisteredService the saml registered service
     * @param adaptor               the adaptor
     */
    protected void handleSingleLogoutForRedirectBinding(final RequestContext context, final LogoutRequest samlLogoutRequest,
                                                        final SamlRegisteredService samlRegisteredService,
                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        if (sloService != null) {
            produceSamlLogoutResponseRedirect(adaptor, sloService, context, samlRegisteredService, samlLogoutRequest);
        }
    }

    /**
     * Handle single logout for post binding.
     *
     * @param context               the context
     * @param samlLogoutRequest     the saml logout request
     * @param samlRegisteredService the saml registered service
     * @param adaptor               the adaptor
     */
    protected void handleSingleLogoutForPostBinding(final RequestContext context, final LogoutRequest samlLogoutRequest,
                                                    final SamlRegisteredService samlRegisteredService,
                                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        var sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_POST_BINDING_URI);
        if (sloService != null) {
            produceSamlLogoutResponsePost(adaptor, sloService, context, samlRegisteredService, samlLogoutRequest);
        }
    }

    /**
     * Produce saml logout response redirect.
     *
     * @param adaptor           the adaptor
     * @param sloService        the slo service
     * @param context           the context
     * @param registeredService the registered service
     * @param logoutRequest     the logout request
     */
    @SneakyThrows
    protected void produceSamlLogoutResponseRedirect(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final SingleLogoutService sloService,
                                                     final RequestContext context,
                                                     final SamlRegisteredService registeredService,
                                                     final LogoutRequest logoutRequest) {
        val logoutResponse = buildSamlLogoutResponse(adaptor, sloService, context, registeredService, logoutRequest);
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.trace("Encoding logout response given endpoint [{}] for binding [{}]", location, sloService.getBinding());

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val encoder = new SamlIdPHttpRedirectDeflateEncoder(location, logoutResponse);
        encoder.setRelayState(request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
        encoder.doEncode();
        val redirectUrl = encoder.getRedirectUrl();
        LOGGER.debug("Final logout redirect URL is [{}]", redirectUrl);

        WebUtils.putLogoutRedirectUrl(request, redirectUrl);
    }

    /**
     * Produce saml logout response post.
     *
     * @param adaptor           the adaptor
     * @param sloService        the slo service
     * @param context           the context
     * @param registeredService the registered service
     * @param logoutRequest     the logout request
     */
    @SneakyThrows
    protected void produceSamlLogoutResponsePost(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                 final SingleLogoutService sloService,
                                                 final RequestContext context,
                                                 final SamlRegisteredService registeredService,
                                                 final LogoutRequest logoutRequest) {
        val logoutResponse = buildSamlLogoutResponse(adaptor, sloService, context, registeredService, logoutRequest);
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.trace("Encoding logout response given endpoint [{}] for binding [{}]", location, sloService.getBinding());

        val payload = SerializeSupport.nodeToString(XMLObjectSupport.marshall(logoutResponse));
        LOGGER.trace("Logout request payload is [{}]", payload);

        val message = EncodingUtils.encodeBase64(payload);
        LOGGER.trace("Logout message encoded in base64 is [{}]", message);

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val data = CollectionUtils.<String, Object>wrap(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, message);
        val relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);
        FunctionUtils.doIfNotNull(relayState, value -> data.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, value));

        WebUtils.putLogoutPostUrl(context, location);
        WebUtils.putLogoutPostData(context, data);
    }

    /**
     * Build saml logout response.
     *
     * @param adaptor           the adaptor
     * @param sloService        the slo service
     * @param requestContext    the context
     * @param registeredService the registered service
     * @param logoutRequest     the logout request
     * @return the logout response
     * @throws Exception the exception
     */
    protected LogoutResponse buildSamlLogoutResponse(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final SingleLogoutService sloService,
                                                     final RequestContext requestContext,
                                                     final SamlRegisteredService registeredService,
                                                     final LogoutRequest logoutRequest) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val builder = configurationContext.getLogoutResponseBuilder();
        val status = builder.newStatus(StatusCode.SUCCESS, "Success");
        val issuer = builder.newIssuer(configurationContext.getCasProperties().getAuthn().getSamlIdp().getCore().getEntityId());

        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.trace("Creating logout response for binding [{}] with issuer [{}], location [{}] and service provider [{}]",
            sloService.getBinding(), issuer, location, adaptor.getEntityId());
        val logoutResponse = builder.newLogoutResponse(id, location, issuer, status, logoutRequest.getID());

        if (configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout().isSignLogoutResponse()) {
            LOGGER.trace("Signing logout request for service provider [{}]", adaptor.getEntityId());
            val logoutResponseSigned = configurationContext.getSamlObjectSigner()
                .encode(logoutResponse, registeredService, adaptor, response,
                    request, sloService.getBinding(), logoutRequest, new MessageContext());
            SamlUtils.logSamlObject(configurationContext.getOpenSamlConfigBean(), logoutResponseSigned);
            return logoutResponseSigned;
        }
        return logoutResponse;
    }

    private Optional<LogoutRequest> getLogoutRequest(final HttpServletRequest request) {
        val logoutRequest = WebUtils.getSingleLogoutRequest(request);
        return Optional.ofNullable(logoutRequest).map(req -> {
            val decodedRequest = EncodingUtils.decodeBase64(logoutRequest);
            return SamlUtils.transformSamlObject(configurationContext.getOpenSamlConfigBean(),
                decodedRequest, LogoutRequest.class);
        });
    }
}
