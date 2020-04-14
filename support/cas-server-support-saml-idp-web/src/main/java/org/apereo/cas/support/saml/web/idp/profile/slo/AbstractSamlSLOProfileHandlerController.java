package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.LogoutRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AbstractSamlSLOProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class AbstractSamlSLOProfileHandlerController extends AbstractSamlIdPProfileHandlerController {

    public AbstractSamlSLOProfileHandlerController(final SamlProfileHandlerConfigurationContext context) {
        super(context);
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
        val logout = getSamlProfileHandlerConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getLogout();
        if (logout.isSingleLogoutCallbacksDisabled()) {
            LOGGER.info("Processing SAML2 IdP SLO requests is disabled");
            return;
        }

        val pair = getSamlProfileHandlerConfigurationContext().getSamlHttpRequestExtractor()
            .extract(request, decoder, LogoutRequest.class);
        val logoutRequest = (LogoutRequest) pair.getKey();
        val ctx = pair.getValue();

        if (logout.isForceSignedLogoutRequests() && !SAMLBindingSupport.isMessageSigned(ctx)) {
            throw new SAMLException("Logout request is not signed but should be.");
        }
        
        if (SAMLBindingSupport.isMessageSigned(ctx)) {
            val entityId = SamlIdPUtils.getIssuerFromSamlObject(logoutRequest);
            LOGGER.trace("SAML logout request from entity id [{}] is signed", entityId);
            val registeredService = getSamlProfileHandlerConfigurationContext().getServicesManager().findServiceBy(entityId, SamlRegisteredService.class);
            LOGGER.trace("SAML registered service tied to [{}] is [{}]", entityId, registeredService);
            val facade = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                getSamlProfileHandlerConfigurationContext().getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId).get();
            LOGGER.trace("Verifying signature on the SAML logout request for [{}]", entityId);
            getSamlProfileHandlerConfigurationContext().getSamlObjectSignatureValidator()
                .verifySamlProfileRequestIfNeeded(logoutRequest, facade, request, ctx);
        }
        SamlUtils.logSamlObject(getSamlProfileHandlerConfigurationContext().getOpenSamlConfigBean(), logoutRequest);
        response.sendRedirect(getSamlProfileHandlerConfigurationContext().getCasProperties().getServer().getLogoutUrl());
    }
}
