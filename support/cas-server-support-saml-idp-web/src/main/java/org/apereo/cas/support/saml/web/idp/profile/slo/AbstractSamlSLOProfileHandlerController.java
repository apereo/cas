package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
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
public abstract class AbstractSamlSLOProfileHandlerController extends AbstractSamlProfileHandlerController {
    private final SSOSamlHttpRequestExtractor samlHttpRequestExtractor;

    public AbstractSamlSLOProfileHandlerController(final SamlIdPObjectSigner samlObjectSigner,
                                                   final AuthenticationSystemSupport authenticationSystemSupport,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final OpenSamlConfigBean configBean,
                                                   final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                   final CasConfigurationProperties casProperties,
                                                   final SamlObjectSignatureValidator samlObjectSignatureValidator,
                                                   final SSOSamlHttpRequestExtractor samlHttpRequestExtractor,
                                                   final Service callbackService) {
        super(samlObjectSigner, authenticationSystemSupport, servicesManager, webApplicationServiceFactory,
            samlRegisteredServiceCachingMetadataResolver, configBean, responseBuilder,
            casProperties, samlObjectSignatureValidator, callbackService);
        this.samlHttpRequestExtractor = samlHttpRequestExtractor;
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
        val logout = casProperties.getAuthn().getSamlIdp().getLogout();
        if (logout.isSingleLogoutCallbacksDisabled()) {
            LOGGER.info("Processing SAML IdP SLO requests is disabled");
            return;
        }

        final Pair<? extends SignableSAMLObject, MessageContext> pair =
            this.samlHttpRequestExtractor.extract(request, decoder, LogoutRequest.class);
        val logoutRequest = (LogoutRequest) pair.getKey();
        val ctx = pair.getValue();

        if (logout.isForceSignedLogoutRequests() && !SAMLBindingSupport.isMessageSigned(ctx)) {
            throw new SAMLException("Logout request is not signed but should be.");
        }

        if (SAMLBindingSupport.isMessageSigned(ctx)) {
            val entityId = SamlIdPUtils.getIssuerFromSamlRequest(logoutRequest);
            val registeredService = this.servicesManager.findServiceBy(entityId, SamlRegisteredService.class);
            val facade = SamlRegisteredServiceServiceProviderMetadataFacade
                .get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, entityId).get();
            this.samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(logoutRequest, facade, request, ctx);
        }
        SamlUtils.logSamlObject(this.configBean, logoutRequest);
        response.sendRedirect(casProperties.getServer().getLogoutUrl());
    }
}
