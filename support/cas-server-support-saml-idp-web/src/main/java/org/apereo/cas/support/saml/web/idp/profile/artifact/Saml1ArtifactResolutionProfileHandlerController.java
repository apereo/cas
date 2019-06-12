package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    public Saml1ArtifactResolutionProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
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
            if (adaptor.isEmpty()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }
            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, artifactMsg, facade);
            val artifactId = artifactMsg.getArtifact().getArtifact();
            val ticketId = getSamlProfileHandlerConfigurationContext().getArtifactTicketFactory().createTicketIdFor(artifactId);
            val ticket = getSamlProfileHandlerConfigurationContext().getTicketRegistry().getTicket(ticketId, SamlArtifactTicket.class);

            val issuerService = getSamlProfileHandlerConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
            val casAssertion = buildCasAssertion(ticket.getTicketGrantingTicket().getAuthentication(),
                issuerService, service,
                CollectionUtils.wrap("artifact", ticket));
            getSamlProfileHandlerConfigurationContext().getResponseBuilder().build(artifactMsg, request, response, casAssertion,
                service, facade, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, e.getMessage());
            getSamlProfileHandlerConfigurationContext().getSamlFaultResponseBuilder().build(artifactMsg, request, response,
                null, null, null, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        }
    }
}
