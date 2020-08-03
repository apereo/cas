package org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;

import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.ArtifactResponseBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileArtifactResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileArtifactResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileArtifactResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }

    @Override
    protected Envelope buildResponse(final Assertion assertion,
                                     final Object casAssertion,
                                     final RequestAbstractType authnRequest,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final String binding,
                                     final MessageContext messageContext) throws SamlException {
        val castedAssertion = org.jasig.cas.client.validation.Assertion.class.cast(casAssertion);
        val ticket = (SamlArtifactTicket) castedAssertion.getAttributes().get("artifact");
        val artifactResponse = new ArtifactResponseBuilder().buildObject();
        artifactResponse.setIssueInstant(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        artifactResponse.setIssuer(newIssuer(ticket.getIssuer()));
        artifactResponse.setInResponseTo(ticket.getRelyingPartyId());
        artifactResponse.setID(ticket.getId());
        artifactResponse.setStatus(newStatus(StatusCode.SUCCESS, "Success"));

        val samlResponse = SamlUtils.transformSamlObject(openSamlConfigBean, ticket.getObject(), SAMLObject.class);
        artifactResponse.setMessage(samlResponse);

        val header = newSoapObject(Header.class);

        val body = newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(artifactResponse);

        val envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.openSamlConfigBean, envelope);
        return envelope;
    }
}
