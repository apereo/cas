package org.apereo.cas.support.saml.web.idp.profile.builders.response.query;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileAttributeQueryResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileAttributeQueryResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileAttributeQueryResponseBuilder(final OpenSamlConfigBean openSamlConfigBean, final BaseSamlObjectSigner samlObjectSigner,
                                                    final VelocityEngineFactory velocityEngineFactory,
                                                    final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
                                                    final SamlProfileObjectBuilder<? extends SAMLObject> saml2ResponseBuilder,
                                                    final SamlObjectEncrypter samlObjectEncrypter) {
        super(openSamlConfigBean, samlObjectSigner, velocityEngineFactory,
                samlProfileSamlAssertionBuilder, saml2ResponseBuilder, samlObjectEncrypter);
    }

    @Override
    public Envelope build(final RequestAbstractType authnRequest, final HttpServletRequest request, 
                          final HttpServletResponse response, final Object casAssertion, final SamlRegisteredService service, 
                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor, final String binding) throws SamlException {
        final AttributeQuery query = (AttributeQuery) authnRequest;
        final Header header = newSoapObject(Header.class);

        final Body body = newSoapObject(Body.class);
        final Response saml2Response = buildSaml2Response(casAssertion, query, service, 
                adaptor, request, SAMLConstants.SAML2_POST_BINDING_URI);
        body.getUnknownXMLObjects().add(saml2Response);

        final Envelope envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.configBean, envelope);
        
        return encodeFinalResponse(request, response, service, adaptor, envelope, binding, authnRequest, casAssertion);
    }
}
