package org.apereo.cas.support.saml.web.idp.profile.builders.response.soap;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.BaseSamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@link SamlProfileSamlSoap11ResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class SamlProfileSamlSoap11ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Envelope> {
    
    public SamlProfileSamlSoap11ResponseBuilder(
        final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }

    @Override
    protected Envelope buildResponse(final Optional<Assertion> assertion,
                                     final SamlProfileBuilderContext context) throws Exception {
        LOGGER.debug("Locating the assertion consumer service url for binding [{}]", context.getBinding());
        val acs = context.getAdaptor().getAssertionConsumerService(context.getBinding());
        LOGGER.debug("Located assertion consumer service url [{}]", acs);
        val ecpResponse = newEcpResponse(acs.getLocation());
        val header = SamlUtils.newSoapObject(Header.class);
        header.getUnknownXMLObjects().add(ecpResponse);
        val body = SamlUtils.newSoapObject(Body.class);
        val saml2Response = buildSaml2Response(context);
        body.getUnknownXMLObjects().add(saml2Response);
        val envelope = SamlUtils.newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        openSamlConfigBean.logObject(envelope);
        return envelope;
    }

    /**
     * Build saml2 response.
     *
     * @param context the context
     * @return the org . opensaml . saml . saml 2 . core . response
     * @throws Exception the exception
     */
    protected Response buildSaml2Response(final SamlProfileBuilderContext context) throws Exception {
        return (Response) getConfigurationContext().getSamlSoapResponseBuilder().build(context);
    }

    @Override
    protected Envelope encode(final SamlProfileBuilderContext context,
                              final Envelope envelope,
                              final String relayState) throws Exception {
        val ctx = context.getMessageContext().ensureSubcontext(SOAP11Context.class);
        Objects.requireNonNull(ctx).setEnvelope(envelope);
        val encoder = new HTTPSOAP11Encoder();
        encoder.setHttpServletResponseSupplier(context::getHttpResponse);
        encoder.setMessageContext(context.getMessageContext());
        encoder.initialize();
        encoder.encode();
        return envelope;
    }
}
