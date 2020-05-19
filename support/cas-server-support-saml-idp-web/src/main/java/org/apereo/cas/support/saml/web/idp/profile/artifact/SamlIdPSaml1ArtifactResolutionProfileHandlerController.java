package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlIdPSaml1ArtifactResolutionProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlIdPSaml1ArtifactResolutionProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public SamlIdPSaml1ArtifactResolutionProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
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
        val config = getSamlProfileHandlerConfigurationContext();
        try {
            val issuer = artifactMsg.getIssuer().getValue();
            val service = verifySamlRegisteredService(issuer);
            val adaptor = getSamlMetadataFacadeFor(service, artifactMsg);
            if (adaptor.isEmpty()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }
            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, artifactMsg, facade);
            val artifactId = artifactMsg.getArtifact().getValue();
            val ticketId = config.getArtifactTicketFactory().createTicketIdFor(artifactId);
            val ticket = config.getTicketRegistry().getTicket(ticketId, SamlArtifactTicket.class);
            if (ticket == null) {
                throw new InvalidTicketException(ticketId);
            }
            val issuerService = config.getWebApplicationServiceFactory().createService(issuer);
            val casAssertion = buildCasAssertion(ticket.getTicketGrantingTicket().getAuthentication(),
                issuerService, service,
                CollectionUtils.wrap("artifact", ticket));
            config.getResponseBuilder().build(artifactMsg, request, response, casAssertion,
                service, facade, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR,
                "Unable to build SOAP response: " + StringUtils.defaultString(e.getMessage()));
            config.getSamlFaultResponseBuilder().build(artifactMsg, request, response,
                null, null, null, SAMLConstants.SAML2_ARTIFACT_BINDING_URI, ctx);
        }
    }
}
