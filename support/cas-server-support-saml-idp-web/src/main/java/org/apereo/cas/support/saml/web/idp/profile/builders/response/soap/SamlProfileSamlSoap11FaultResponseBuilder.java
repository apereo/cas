package org.apereo.cas.support.saml.web.idp.profile.builders.response.soap;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;

import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultActor;
import org.opensaml.soap.soap11.FaultCode;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;

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

    public SamlProfileSamlSoap11FaultResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }

    @Override
    public Envelope build(final RequestAbstractType authnRequest,
                          final HttpServletRequest request,
                          final HttpServletResponse response,
                          final Object casAssertion,
                          final SamlRegisteredService service,
                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                          final String binding,
                          final MessageContext messageContext) throws SamlException {

        val body = newSoapObject(Body.class);
        val fault = newSoapObject(Fault.class);

        val faultCode = newSoapObject(FaultCode.class);
        faultCode.setValue(FaultCode.SERVER);
        fault.setCode(faultCode);

        val faultActor = newSoapObject(FaultActor.class);
        faultActor.setURI(SamlIdPUtils.getIssuerFromSamlObject(authnRequest));
        fault.setActor(faultActor);

        val faultString = newSoapObject(FaultString.class);
        val error = request.getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR);
        if (error != null) {
            faultString.setValue(error.toString());
        } else {
            faultString.setValue("SOAP failure");
        }
        fault.setMessage(faultString);

        body.getUnknownXMLObjects().add(fault);
        
        val envelope = newSoapObject(Envelope.class);
        val header = newSoapObject(Header.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        encodeFinalResponse(request, response, service, adaptor, envelope,
            binding, authnRequest, casAssertion, messageContext);
        return envelope;
    }
}
