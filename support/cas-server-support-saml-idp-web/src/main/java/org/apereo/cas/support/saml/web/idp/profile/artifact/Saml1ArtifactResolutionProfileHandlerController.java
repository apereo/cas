package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link Saml1ArtifactResolutionProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class Saml1ArtifactResolutionProfileHandlerController extends AbstractSamlProfileHandlerController {

    private final TicketRegistry ticketRegistry;
    private final SamlArtifactTicketFactory artifactTicketFactory;
    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;

    public Saml1ArtifactResolutionProfileHandlerController(final SamlIdPObjectSigner samlObjectSigner,
                                                           final AuthenticationSystemSupport authenticationSystemSupport,
                                                           final ServicesManager servicesManager,
                                                           final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                           final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                           final OpenSamlConfigBean configBean,
                                                           final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                           final CasConfigurationProperties casProperties,
                                                           final SamlObjectSignatureValidator samlObjectSignatureValidator,
                                                           final TicketRegistry ticketRegistry,
                                                           final SamlArtifactTicketFactory artifactTicketFactory,
                                                           final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder,
                                                           final Service callbackService) {
        super(samlObjectSigner, authenticationSystemSupport, servicesManager,
            webApplicationServiceFactory, samlRegisteredServiceCachingMetadataResolver, configBean,
            responseBuilder, casProperties, samlObjectSignatureValidator, callbackService);
        this.ticketRegistry = ticketRegistry;
        this.artifactTicketFactory = artifactTicketFactory;
        this.samlFaultResponseBuilder = samlFaultResponseBuilder;
    }

    /**
     * Handle post request.
     *
     * @param response the response
     * @param request  the request
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION)
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) {
        val ctx = decodeSoapRequest(request);
        val artifactMsg = (ArtifactResolve) ctx.getMessage();
        try {
            val issuer = artifactMsg.getIssuer().getValue();
            val service = verifySamlRegisteredService(issuer);
            val adaptor = getSamlMetadataFacadeFor(service, artifactMsg);
            if (!adaptor.isPresent()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }
            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, artifactMsg, facade);
            val artifactId = artifactMsg.getArtifact().getArtifact();
            val ticketId = artifactTicketFactory.createTicketIdFor(artifactId);
            val ticket = this.ticketRegistry.getTicket(ticketId, SamlArtifactTicket.class);

            val issuerService = webApplicationServiceFactory.createService(issuer);
            val casAssertion = buildCasAssertion(ticket.getTicketGrantingTicket().getAuthentication(),
                issuerService, service,
                CollectionUtils.wrap("artifact", ticket));
            this.responseBuilder.build(artifactMsg, request, response, casAssertion,
                service, facade, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, e.getMessage());
            samlFaultResponseBuilder.build(artifactMsg, request, response,
                null, null, null, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        }
    }
}
