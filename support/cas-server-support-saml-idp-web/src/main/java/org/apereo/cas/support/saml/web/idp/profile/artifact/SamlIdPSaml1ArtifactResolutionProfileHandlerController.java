package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlIdPSaml1ArtifactResolutionProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Tag(name = "SAML2")
public class SamlIdPSaml1ArtifactResolutionProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public SamlIdPSaml1ArtifactResolutionProfileHandlerController(final SamlProfileHandlerConfigurationContext ctx) {
        super(ctx);
    }

    /**
     * Handle post request.
     *
     * @param response the response
     * @param request  the request
     * @throws Throwable the throwable
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION)
    @Operation(summary = "Handle SAML1 SOAP Artifact Resolution Request")
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) throws Throwable {
        val ctx = decodeSoapRequest(request);
        val artifactMsg = (ArtifactResolve) ctx.getMessage();
        try {
            val issuer = Objects.requireNonNull(artifactMsg).getIssuer().getValue();
            val registeredService = verifySamlRegisteredService(issuer, request);
            val adaptor = getSamlMetadataFacadeFor(registeredService, artifactMsg);
            if (adaptor.isEmpty()) {
                throw UnauthorizedServiceException.denied("Cannot find metadata linked to %s".formatted(issuer));
            }
            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, artifactMsg, facade, registeredService);
            val artifactId = artifactMsg.getArtifact().getValue();

            val factory = (SamlArtifactTicketFactory) getConfigurationContext().getTicketFactory().get(SamlArtifactTicket.class);
            val ticketId = factory.createTicketIdFor(artifactId);
            val ticket = getConfigurationContext().getTicketRegistry().getTicket(ticketId, SamlArtifactTicket.class);
            if (ticket == null) {
                throw new InvalidTicketException(ticketId);
            }
            val issuerService = getConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
            val authentication = ((AuthenticationAwareTicket) ticket.getTicketGrantingTicket()).getAuthentication();
            val casAssertion = buildCasAssertion(authentication,
                issuerService, registeredService,
                CollectionUtils.wrap("artifact", ticket));

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(artifactMsg)
                .httpRequest(request)
                .httpResponse(response)
                .authenticatedAssertion(Optional.of(casAssertion))
                .registeredService(registeredService)
                .adaptor(facade)
                .binding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)
                .messageContext(ctx)
                .build();
            getConfigurationContext().getResponseBuilder().build(buildContext);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR,
                "Unable to build SOAP response: " + StringUtils.defaultString(e.getMessage()));

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(artifactMsg)
                .httpRequest(request)
                .httpResponse(response)
                .binding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)
                .messageContext(ctx)
                .build();
            getConfigurationContext().getSamlFaultResponseBuilder().build(buildContext);
        }
    }
}
