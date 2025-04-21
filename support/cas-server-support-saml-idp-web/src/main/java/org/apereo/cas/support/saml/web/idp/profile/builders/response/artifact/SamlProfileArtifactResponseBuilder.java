package org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;

import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.ArtifactResponseBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * This is {@link SamlProfileArtifactResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileArtifactResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    public SamlProfileArtifactResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext ctx) {
        super(ctx);
    }

    @Override
    protected Envelope buildResponse(final Optional<Assertion> assertion, final SamlProfileBuilderContext context) {
        val ticket = (SamlArtifactTicket) context.getAuthenticatedAssertion().orElseThrow().getAttributes().get("artifact");
        val artifactResponse = new ArtifactResponseBuilder().buildObject();
        artifactResponse.setIssueInstant(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        artifactResponse.setIssuer(newIssuer(ticket.getIssuer()));
        artifactResponse.setInResponseTo(ticket.getRelyingPartyId());
        artifactResponse.setID(ticket.getId());
        artifactResponse.setStatus(newStatus(StatusCode.SUCCESS, "Success"));

        val samlResponse = SamlUtils.transformSamlObject(openSamlConfigBean, ticket.getObject(), SAMLObject.class);
        artifactResponse.setMessage(samlResponse);

        val header = SamlUtils.newSoapObject(Header.class);

        val body = SamlUtils.newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(artifactResponse);

        val envelope = SamlUtils.newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        openSamlConfigBean.logObject(envelope);
        return envelope;
    }
}
