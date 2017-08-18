package org.apereo.cas.support.saml.web.idp.profile.builders.response.soap;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultActor;
import org.opensaml.soap.soap11.FaultCode;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlProfileSamlSoap11FaultResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlProfileSamlSoap11FaultResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -1875903354216171261L;

    public SamlProfileSamlSoap11FaultResponseBuilder(final OpenSamlConfigBean openSamlConfigBean,
                                                     final BaseSamlObjectSigner samlObjectSigner,
                                                     final VelocityEngineFactory velocityEngineFactory,
                                                     final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
                                                     final SamlProfileObjectBuilder<? extends SAMLObject> saml2ResponseBuilder,
                                                     final SamlObjectEncrypter samlObjectEncrypter) {
        super(openSamlConfigBean, samlObjectSigner, velocityEngineFactory,
                samlProfileSamlAssertionBuilder, saml2ResponseBuilder, samlObjectEncrypter);
    }


    @Override
    public Envelope build(final RequestAbstractType authnRequest,
                          final HttpServletRequest request,
                          final HttpServletResponse response,
                          final Object casAssertion,
                          final SamlRegisteredService service,
                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                          final String binding) throws SamlException {
        final Header header = newSoapObject(Header.class);

        final Body body = newSoapObject(Body.class);
        final Fault fault = newSoapObject(Fault.class);

        final FaultCode faultCode = newSoapObject(FaultCode.class);
        faultCode.setValue(FaultCode.SERVER);
        fault.setCode(faultCode);

        final FaultActor faultActor = newSoapObject(FaultActor.class);
        faultActor.setValue(SamlIdPUtils.getIssuerFromSamlRequest(authnRequest));
        fault.setActor(faultActor);

        final FaultString faultString = newSoapObject(FaultString.class);
        faultString.setValue(request.getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR).toString());
        fault.setMessage(faultString);

        body.getUnknownXMLObjects().add(fault);

        final Envelope envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        encodeFinalResponse(request, response, service, adaptor, envelope, binding, authnRequest, casAssertion);
        return envelope;
    }
}
