package org.apereo.cas.support.saml.web.idp.profile.builders.response.soap;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;

import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultActor;
import org.opensaml.soap.soap11.FaultCode;
import org.opensaml.soap.soap11.FaultString;
import org.opensaml.soap.soap11.Header;

import java.util.Objects;

/**
 * The {@link SamlProfileSamlSoap11FaultResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlProfileSamlSoap11FaultResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {

    public SamlProfileSamlSoap11FaultResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public Envelope build(final SamlProfileBuilderContext context) throws Exception {

        val body = SamlUtils.newSoapObject(Body.class);
        val fault = SamlUtils.newSoapObject(Fault.class);

        val faultCode = SamlUtils.newSoapObject(FaultCode.class);
        faultCode.setValue(FaultCode.SERVER);
        fault.setCode(faultCode);

        val faultActor = SamlUtils.newSoapObject(FaultActor.class);
        faultActor.setURI(SamlIdPUtils.getIssuerFromSamlObject(context.getSamlRequest()));
        fault.setActor(faultActor);

        val faultString = SamlUtils.newSoapObject(FaultString.class);
        val error = context.getHttpRequest().getAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR);
        if (error != null) {
            faultString.setValue(error.toString());
        } else {
            faultString.setValue("SOAP failure");
        }
        fault.setMessage(faultString);
        body.getUnknownXMLObjects().add(fault);
        
        val envelope = SamlUtils.newSoapObject(Envelope.class);
        val header = SamlUtils.newSoapObject(Header.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        val ctx = context.getMessageContext().ensureSubcontext(SOAP11Context.class);
        Objects.requireNonNull(ctx).setHTTPResponseStatus(HttpStatus.SC_OK);
        encodeFinalResponse(context, envelope);
        context.getHttpRequest().setAttribute(FaultString.class.getSimpleName(), error);
        return envelope;
    }
}
