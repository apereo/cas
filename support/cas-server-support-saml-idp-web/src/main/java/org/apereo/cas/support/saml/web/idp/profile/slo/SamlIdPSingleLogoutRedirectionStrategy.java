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
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.springframework.http.MediaType;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;

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
        return WebUtils.getRegisteredService(request) != null && WebUtils.getSingleLogoutRequest(request) != null;
    }

    @Override
    public void handle(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val decodedRequest = EncodingUtils.decodeBase64(WebUtils.getSingleLogoutRequest(request));
        val samlLogoutRequest = SamlUtils.transformSamlObject(configurationContext.getOpenSamlConfigBean(),
            decodedRequest, LogoutRequest.class);

        val logoutRequestIssuer = SamlIdPUtils.getIssuerFromSamlObject(samlLogoutRequest);
        val samlRegisteredService = (SamlRegisteredService) WebUtils.getRegisteredService(request);
        val adaptorRes = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(),
            samlRegisteredService, logoutRequestIssuer);

        if (adaptorRes.isEmpty()) {
            LOGGER.warn("Cannot find metadata linked to [{}]", logoutRequestIssuer);
        } else {
            val adaptor = adaptorRes.get();

            var sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_POST_BINDING_URI);
            if (sloService != null) {
                val logoutResponse = buildSamlLogoutResponse(logoutRequestIssuer, adaptor, sloService, context);
                postSamlLogoutResponse(sloService, logoutResponse, context);
                return;
            }

            sloService = adaptor.getSingleLogoutService(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
            if (sloService != null) {
                produceSamlLogoutResponseRedirect(logoutRequestIssuer, adaptor, sloService, context);
            }
        }
    }

    /**
     * Produce saml logout response redirect.
     *
     * @param logoutRequestIssuer the logout request issuer
     * @param adaptor             the adaptor
     * @param sloService          the slo service
     * @param context             the context
     */
    @SneakyThrows
    protected void produceSamlLogoutResponseRedirect(final String logoutRequestIssuer,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final SingleLogoutService sloService,
                                                     final RequestContext context) {
        val logoutResponse = buildSamlLogoutResponse(logoutRequestIssuer, adaptor, sloService, context);
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        val encoder = new SamlIdPHttpRedirectDeflateEncoder(location, logoutResponse);
        encoder.doEncode();
        val redirectUrl = encoder.getRedirectUrl();
        LOGGER.trace("Final logout redirect URL is [{}]", redirectUrl);

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        WebUtils.putLogoutRedirectUrl(request, redirectUrl);
    }

    /**
     * Post saml logout response.
     *
     * @param sloService     the slo service
     * @param logoutResponse the logout response
     * @param context        the context
     */
    @SneakyThrows
    protected void postSamlLogoutResponse(final SingleLogoutService sloService, final LogoutResponse logoutResponse,
                                          final RequestContext context) {
        val payload = SerializeSupport.nodeToString(XMLObjectSupport.marshall(logoutResponse));
        LOGGER.trace("Logout request payload is [{}]", payload);

        val message = EncodingUtils.encodeBase64(payload.getBytes(StandardCharsets.UTF_8), false);
        LOGGER.trace("Logout message encoded in base64 is [{}]", message);

        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        LOGGER.debug("Sending logout response using [{}} binding to [{}]", sloService.getBinding(), location);
        HttpUtils.executePost(location,
            CollectionUtils.wrap(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, message),
            CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_XML_VALUE));
    }

    /**
     * Build saml logout response.
     *
     * @param logoutRequestIssuer the logout request issuer
     * @param adaptor             the adaptor
     * @param sloService          the slo service
     * @param context             the context
     * @return the logout response
     */
    protected LogoutResponse buildSamlLogoutResponse(final String logoutRequestIssuer,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final SingleLogoutService sloService,
                                                     final RequestContext context) {
        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val builder = configurationContext.getLogoutResponseBuilder();
        val status = builder.newStatus(StatusCode.SUCCESS, "Success");
        val issuer = builder.newIssuer(configurationContext.getCasProperties().getAuthn().getSamlIdp().getEntityId());

        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        return builder.newLogoutResponse(id, location, issuer, status, logoutRequestIssuer);
    }
}
