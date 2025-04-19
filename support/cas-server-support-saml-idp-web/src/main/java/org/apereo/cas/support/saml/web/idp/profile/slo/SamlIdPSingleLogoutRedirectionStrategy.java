package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.LogoutRedirectionResponse;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.xml.SerializeSupport;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

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
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val registeredService = WebUtils.getRegisteredService(request);
        if (registeredService instanceof final SamlRegisteredService samlRegisteredService) {
            val logout = configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
            val sloRequest = WebUtils.getSingleLogoutRequest(request);
            var async = false;
            if (StringUtils.isNotBlank(sloRequest)) {
                async = getLogoutRequest(request)
                    .map(RequestAbstractType::getExtensions)
                    .stream()
                    .filter(Objects::nonNull)
                    .anyMatch(extensions -> !extensions.getUnknownXMLObjects(Asynchronous.DEFAULT_ELEMENT_NAME).isEmpty());
            }
            return logout.isSendLogoutResponse()
                && samlRegisteredService.isLogoutResponseEnabled()
                && sloRequest != null
                && !async;
        }
        return false;
    }

    @Override
    public LogoutRedirectionResponse handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val samlRegisteredService = (SamlRegisteredService) WebUtils.getRegisteredService(request);
        val samlLogoutRequest = getLogoutRequest(request).orElseThrow();

        val logoutRequestIssuer = SamlIdPUtils.getIssuerFromSamlObject(samlLogoutRequest);
        val adapterResult = SamlRegisteredServiceMetadataAdaptor.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(),
            samlRegisteredService, logoutRequestIssuer);

        if (adapterResult.isEmpty()) {
            LOGGER.warn("Cannot find service provider metadata entity linked to [{}]", logoutRequestIssuer);
            return null;
        }
        
        val adaptor = adapterResult.get();
        val binding = determineLogoutResponseBindingType(adaptor, samlRegisteredService);
        LOGGER.debug("Logout response binding type is determined as [{}]", binding);

        if (SAMLConstants.SAML2_POST_BINDING_URI.equals(binding)) {
            return handleSingleLogoutForPostBinding(samlLogoutRequest, samlRegisteredService, adaptor, request, response);
        }
        return handleSingleLogoutForRedirectBinding(samlLogoutRequest, samlRegisteredService, adaptor, request, response);
    }

    protected String determineLogoutResponseBindingType(final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                        final SamlRegisteredService samlRegisteredService) {

        val logout = configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
        var binding = logout.getLogoutResponseBinding();
        if (StringUtils.isNotBlank(samlRegisteredService.getLogoutResponseBinding())) {
            binding = samlRegisteredService.getLogoutResponseBinding();
        } else if (!adaptor.getSingleLogoutServices().isEmpty()) {
            binding = adaptor.getSingleLogoutService().getBinding();
        }

        return StringUtils.defaultIfBlank(binding, SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    }

    protected LogoutRedirectionResponse handleSingleLogoutForRedirectBinding(final LogoutRequest samlLogoutRequest,
                                                                             final SamlRegisteredService samlRegisteredService,
                                                                             final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                                             final HttpServletRequest request,
                                                                             final HttpServletResponse response) throws Exception {
        val sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        return produceSamlLogoutResponseRedirect(adaptor, sloService, samlRegisteredService, samlLogoutRequest, request, response);
    }

    protected LogoutRedirectionResponse handleSingleLogoutForPostBinding(final LogoutRequest samlLogoutRequest,
                                                                         final SamlRegisteredService samlRegisteredService,
                                                                         final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                                         final HttpServletRequest request,
                                                                         final HttpServletResponse response) throws Exception {
        val sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_POST_BINDING_URI);
        return produceSamlLogoutResponsePost(adaptor, sloService, samlRegisteredService, samlLogoutRequest, request, response);
    }

    protected LogoutRedirectionResponse produceSamlLogoutResponseRedirect(final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                                          final SingleLogoutService sloService,
                                                                          final SamlRegisteredService registeredService,
                                                                          final LogoutRequest logoutRequest,
                                                                          final HttpServletRequest request,
                                                                          final HttpServletResponse response) throws Exception {
        val logoutResponse = buildSamlLogoutResponse(adaptor, sloService, registeredService, logoutRequest, request, response);
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.trace("Encoding logout response given endpoint [{}] for binding [{}]", location, sloService.getBinding());

        val encoder = new SamlIdPHttpRedirectDeflateEncoder(location, logoutResponse);
        encoder.setRelayState(fetchRelayState(request));
        encoder.doEncode();
        val redirectUrl = encoder.getRedirectUrl();
        LOGGER.debug("Final logout redirect URL is [{}]", redirectUrl);
        WebUtils.putLogoutRedirectUrl(request, redirectUrl);
        return null;
    }

    protected LogoutRedirectionResponse produceSamlLogoutResponsePost(final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                                      final SingleLogoutService sloService,
                                                                      final SamlRegisteredService registeredService,
                                                                      final LogoutRequest logoutRequest,
                                                                      final HttpServletRequest request,
                                                                      final HttpServletResponse response) throws Exception {
        val logoutResponse = buildSamlLogoutResponse(adaptor, sloService, registeredService, logoutRequest, request, response);
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.debug("Encoding logout response for endpoint [{}] and binding [{}]", location, sloService.getBinding());
        val payload = SerializeSupport.nodeToString(XMLObjectSupport.marshall(logoutResponse));
        LOGGER.debug("Logout response payload is [{}]", payload);

        val message = EncodingUtils.encodeBase64(payload);
        LOGGER.trace("Logout message encoded in base64 is [{}]", message);

        val data = CollectionUtils.<String, Object>wrap(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, message);
        val relayState = fetchRelayState(request);
        FunctionUtils.doIfNotNull(relayState, value -> data.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, value));
        return LogoutRedirectionResponse.builder().logoutPostUrl(Optional.ofNullable(location)).logoutPostData(data).build();
    }

    protected String fetchRelayState(final HttpServletRequest request) {
        val relayState = StringUtils.defaultIfBlank((String) request.getAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE),
            request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
        LOGGER.debug("Relay state is [{}]", relayState);
        return relayState;
    }

    protected LogoutResponse buildSamlLogoutResponse(final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                     final SingleLogoutService sloService,
                                                     final SamlRegisteredService registeredService,
                                                     final LogoutRequest logoutRequest,
                                                     final HttpServletRequest request,
                                                     final HttpServletResponse response) throws Exception {
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

        if (signSamlLogoutResponseFor(registeredService)) {
            LOGGER.trace("Signing logout request for service provider [{}]", adaptor.getEntityId());
            val logoutResponseSigned = configurationContext.getSamlObjectSigner()
                .encode(logoutResponse, registeredService, adaptor, response,
                    request, sloService.getBinding(), logoutRequest, new MessageContext());
            configurationContext.getOpenSamlConfigBean().logObject(logoutResponseSigned);
            return logoutResponseSigned;
        }
        return logoutResponse;
    }

    protected Optional<LogoutRequest> getLogoutRequest(final HttpServletRequest request) {
        val logoutRequest = WebUtils.getSingleLogoutRequest(request);
        return Optional.ofNullable(logoutRequest).map(req -> {
            val decodedRequest = EncodingUtils.decodeBase64(logoutRequest);
            return SamlUtils.transformSamlObject(configurationContext.getOpenSamlConfigBean(),
                decodedRequest, LogoutRequest.class);
        });
    }

    protected boolean signSamlLogoutResponseFor(final SamlRegisteredService registeredService) {
        if (registeredService.getSignLogoutResponse().isUndefined()) {
            return configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout().isSignLogoutResponse();
        }
        return registeredService.getSignLogoutResponse().isTrue();
    }
}
