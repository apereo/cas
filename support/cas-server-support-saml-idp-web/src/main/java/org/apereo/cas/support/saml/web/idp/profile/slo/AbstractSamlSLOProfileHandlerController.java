package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link AbstractSamlSLOProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class AbstractSamlSLOProfileHandlerController extends AbstractSamlIdPProfileHandlerController {

    protected AbstractSamlSLOProfileHandlerController(final SamlProfileHandlerConfigurationContext context) {
        super(context);
    }

    private void handleLogoutResponse(final Pair<? extends SignableSAMLObject, MessageContext> pair) {
        val logoutResponse = (LogoutResponse) pair.getKey();
        LOGGER.debug("Received logout response from [{}]", SamlIdPUtils.getIssuerFromSamlObject(logoutResponse.getIssuer()));
        SamlUtils.logSamlObject(getConfigurationContext().getOpenSamlConfigBean(), logoutResponse);
    }

    private void handleLogoutRequest(final HttpServletResponse response, final HttpServletRequest request,
                                     final Pair<? extends SignableSAMLObject, MessageContext> pair) throws Exception {
        val configContext = getConfigurationContext();
        val logout = configContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
        val logoutRequest = (LogoutRequest) pair.getKey();
        val ctx = pair.getValue();

        if (logout.isForceSignedLogoutRequests() && !SAMLBindingSupport.isMessageSigned(ctx)) {
            throw new SAMLException("Logout request is not signed but should be.");
        }

        val entityId = SamlIdPUtils.getIssuerFromSamlObject(logoutRequest);
        LOGGER.trace("SAML logout request from entity id [{}] is signed", entityId);

        val service = configContext.getWebApplicationServiceFactory().createService(entityId);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(entityId));
        val registeredService = configContext.getServicesManager().findServiceBy(service, SamlRegisteredService.class);
        LOGGER.trace("SAML registered service tied to [{}] is [{}]", entityId, registeredService);
        val facade = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId).get();
        if (SAMLBindingSupport.isMessageSigned(ctx)) {
            LOGGER.trace("Verifying signature on the SAML logout request for [{}]", entityId);
            configContext.getSamlObjectSignatureValidator()
                .verifySamlProfileRequestIfNeeded(logoutRequest, facade, request, ctx);
        }
        SamlUtils.logSamlObject(configContext.getOpenSamlConfigBean(), logoutRequest);

        val logoutUrls = SingleLogoutUrl.from(registeredService);
        if (!logoutUrls.isEmpty()) {
            val destination = logoutUrls.iterator().next().getUrl();
            WebUtils.putLogoutRedirectUrl(request, destination);
        }

        WebUtils.putRegisteredService(request, registeredService);
        try (val writer = SamlUtils.transformSamlObject(configurationContext.getOpenSamlConfigBean(), logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }

        request.getServletContext().getRequestDispatcher(CasProtocolConstants.ENDPOINT_LOGOUT).forward(request, response);
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleSloProfileRequest(final HttpServletResponse response,
                                           final HttpServletRequest request,
                                           final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        val logout = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getLogout();
        if (logout.isSingleLogoutCallbacksDisabled()) {
            LOGGER.info("Processing SAML2 IdP SLO requests is disabled");
            return;
        }

        val extractor = getConfigurationContext().getSamlHttpRequestExtractor();
        val result = extractor.extract(request, decoder, SignableSAMLObject.class);
        if (result.isPresent()) {
            val pair = result.get();
            if (pair.getKey() instanceof LogoutResponse) {
                handleLogoutResponse(pair);
            } else if (pair.getKey() instanceof LogoutRequest) {
                handleLogoutRequest(response, request, pair);
            }
        } else {
            LOGGER.trace("Unable to process logout request/response");
        }

    }
}
