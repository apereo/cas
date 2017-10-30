package org.apereo.cas.support.saml.web.idp.profile.artifact;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link Saml1ArtifactResolutionProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class Saml1ArtifactResolutionProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(Saml1ArtifactResolutionProfileHandlerController.class);
    private final TicketRegistry ticketRegistry;
    private final SamlArtifactTicketFactory artifactTicketFactory;
    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;

    public Saml1ArtifactResolutionProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                                           final ParserPool parserPool,
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
                                                           final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport, servicesManager,
                webApplicationServiceFactory, samlRegisteredServiceCachingMetadataResolver, configBean,
                responseBuilder, casProperties, samlObjectSignatureValidator);
        this.ticketRegistry = ticketRegistry;
        this.casProperties = casProperties;
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
        final MessageContext ctx = decodeSoapRequest(request);
        final ArtifactResolve artifactMsg = (ArtifactResolve) ctx.getMessage();
        try {
            final String issuer = artifactMsg.getIssuer().getValue();
            final SamlRegisteredService service = verifySamlRegisteredService(issuer);
            final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> adaptor = getSamlMetadataFacadeFor(service, artifactMsg);
            if (!adaptor.isPresent()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }
            final SamlRegisteredServiceServiceProviderMetadataFacade facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, artifactMsg, facade);
            final String artifactId = artifactMsg.getArtifact().getArtifact();
            final String ticketId = artifactTicketFactory.createTicketIdFor(artifactId);
            final SamlArtifactTicket ticket = this.ticketRegistry.getTicket(ticketId, SamlArtifactTicket.class);

            final Service issuerService = webApplicationServiceFactory.createService(issuer);
            final Assertion casAssertion = buildCasAssertion(ticket.getGrantingTicket().getAuthentication(),
                    issuerService, service,
                    CollectionUtils.wrap("artifact", ticket));
            this.responseBuilder.build(artifactMsg, request, response, casAssertion,
                    service, facade, SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, e.getMessage());
            samlFaultResponseBuilder.build(artifactMsg, request, response,
                    null, null, null, SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        }
    }
}
