package org.apereo.cas.support.saml.web.idp.profile.slo;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AbstractSamlSLOProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractSamlSLOProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSamlSLOProfileHandlerController.class);

    /**
     * Instantiates a new Abstract saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
     * @param authenticationSystemSupport                  the authentication system support
     * @param servicesManager                              the services manager
     * @param webApplicationServiceFactory                 the web application service factory
     * @param samlRegisteredServiceCachingMetadataResolver the saml registered service caching metadata resolver
     * @param configBean                                   the config bean
     * @param responseBuilder                              the response builder
     * @param casProperties                                the cas properties
     * @param samlObjectSignatureValidator                 the saml object signature validator
     */
    public AbstractSamlSLOProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner, final ParserPool parserPool,
                                                   final AuthenticationSystemSupport authenticationSystemSupport,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final OpenSamlConfigBean configBean,
                                                   final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                   final CasConfigurationProperties casProperties,
                                                   final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport, servicesManager, webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver, configBean, responseBuilder, casProperties, samlObjectSignatureValidator);
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
        if (casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled()) {
            LOGGER.info("Processing SAML IdP SLO requests is disabled");
            return;
        }

        final Pair<? extends SignableSAMLObject, MessageContext> pair = decodeSamlContextFromHttpRequest(request, decoder, LogoutRequest.class);
        final LogoutRequest logoutRequest = LogoutRequest.class.cast(pair.getKey());
        final MessageContext ctx = pair.getValue();

        if (casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests() && !SAMLBindingSupport.isMessageSigned(ctx)) {
            throw new SAMLException("Logout request is not signed but should be.");
        }

        if (SAMLBindingSupport.isMessageSigned(ctx)) {
            final String entityId = SamlIdPUtils.getIssuerFromSamlRequest(logoutRequest);
            final SamlRegisteredService registeredService = this.servicesManager.findServiceBy(entityId, SamlRegisteredService.class);
            final SamlRegisteredServiceServiceProviderMetadataFacade facade = SamlRegisteredServiceServiceProviderMetadataFacade
                    .get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, entityId).get();
            this.samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(logoutRequest, facade, request, ctx);
        }
        SamlUtils.logSamlObject(this.configBean, logoutRequest);
        response.sendRedirect(casProperties.getServer().getLogoutUrl());
    }
}
