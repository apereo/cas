package org.apereo.cas.support.saml.web.idp.profile.builders.response.soap;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.BaseSamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlProfileSamlSoap11ResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class SamlProfileSamlSoap11ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Envelope> {

    private static final long serialVersionUID = -1875903354216171261L;

    public SamlProfileSamlSoap11ResponseBuilder(
        final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
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

        LOGGER.debug("Locating the assertion consumer service url for binding [{}]", binding);
        val acs = adaptor.getAssertionConsumerService(binding);
        LOGGER.debug("Located assertion consumer service url [{}]", acs);
        val ecpResponse = newEcpResponse(acs.getLocation());
        val header = newSoapObject(Header.class);
        header.getUnknownXMLObjects().add(ecpResponse);
        val body = newSoapObject(Body.class);
        val saml2Response = buildSaml2Response(casAssertion, authnRequest, service, adaptor, request, binding, messageContext);
        body.getUnknownXMLObjects().add(saml2Response);
        val envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.openSamlConfigBean, envelope);
        return envelope;
    }

    /**
     * Build saml2 response.
     *
     * @param casAssertion   the cas assertion
     * @param authnRequest   the authn request
     * @param service        the service
     * @param adaptor        the adaptor
     * @param request        the request
     * @param binding        the binding
     * @param messageContext the message context
     * @return the org . opensaml . saml . saml 2 . core . response
     */
    protected Response buildSaml2Response(final Object casAssertion,
                                          final RequestAbstractType authnRequest,
                                          final SamlRegisteredService service,
                                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                          final HttpServletRequest request,
                                          final String binding,
                                          final MessageContext messageContext) {
        return (Response)
            getSamlResponseBuilderConfigurationContext().getSamlSoapResponseBuilder()
                .build(authnRequest, request, null, casAssertion, service, adaptor, binding, messageContext);
    }

    @Override
    @SneakyThrows
    protected Envelope encode(final SamlRegisteredService service,
                              final Envelope envelope,
                              final HttpServletResponse httpResponse,
                              final HttpServletRequest httpRequest,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState,
                              final String binding,
                              final RequestAbstractType authnRequest,
                              final Object assertion) throws SamlException {
        val result = new MessageContext();
        val ctx = result.getSubcontext(SOAP11Context.class, true);
        ctx.setEnvelope(envelope);
        val encoder = new HTTPSOAP11Encoder();
        encoder.setHttpServletResponse(httpResponse);
        encoder.setMessageContext(result);
        encoder.initialize();
        encoder.encode();
        return envelope;
    }
}
