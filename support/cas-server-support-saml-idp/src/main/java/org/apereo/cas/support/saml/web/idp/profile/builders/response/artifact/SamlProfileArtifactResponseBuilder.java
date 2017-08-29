package org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.ArtifactResponseBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileArtifactResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileArtifactResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileArtifactResponseBuilder(final OpenSamlConfigBean openSamlConfigBean, final BaseSamlObjectSigner samlObjectSigner, 
                                              final VelocityEngineFactory velocityEngineFactory, 
                                              final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder, 
                                              final SamlProfileObjectBuilder<? extends SAMLObject> saml2ResponseBuilder, 
                                              final SamlObjectEncrypter samlObjectEncrypter) {
        super(openSamlConfigBean, samlObjectSigner, velocityEngineFactory, samlProfileSamlAssertionBuilder, saml2ResponseBuilder, samlObjectEncrypter);
    }

    @Override
    protected Envelope buildResponse(final Assertion assertion, final Object casAssertion, final RequestAbstractType authnRequest, 
                                     final SamlRegisteredService service, final SamlRegisteredServiceServiceProviderMetadataFacade adaptor, 
                                     final HttpServletRequest request, final HttpServletResponse response, final String binding) throws SamlException {
        final org.jasig.cas.client.validation.Assertion castedAssertion = org.jasig.cas.client.validation.Assertion.class.cast(casAssertion);
        final SamlArtifactTicket ticket = (SamlArtifactTicket) castedAssertion.getAttributes().get("artifact");
        final ArtifactResponse artifactResponse = new ArtifactResponseBuilder().buildObject();
        artifactResponse.setIssueInstant(DateTime.now());
        artifactResponse.setIssuer(newIssuer(ticket.getIssuer()));
        artifactResponse.setInResponseTo(ticket.getRelyingPartyId());
        artifactResponse.setID(ticket.getId());
        artifactResponse.setStatus(newStatus(StatusCode.SUCCESS, "Success"));
        
        final SAMLObject samlResponse = SamlUtils.transformSamlObject(configBean, ticket.getObject());
        artifactResponse.setMessage(samlResponse);
        
        final Header header = newSoapObject(Header.class);
        
        final Body body = newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(artifactResponse);
        
        final Envelope envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.configBean, envelope);
        return envelope;
    }
}
