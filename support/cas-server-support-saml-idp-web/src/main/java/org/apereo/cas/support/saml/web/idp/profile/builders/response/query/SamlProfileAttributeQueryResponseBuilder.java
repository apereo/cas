package org.apereo.cas.support.saml.web.idp.profile.builders.response.query;

import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;

import lombok.val;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SamlProfileAttributeQueryResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileAttributeQueryResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {

    public SamlProfileAttributeQueryResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public Envelope build(final SamlProfileBuilderContext context) throws Exception {

        context.getHttpResponse().setContentType(MediaType.APPLICATION_XML_VALUE);
        val header = SamlUtils.newSoapObject(Header.class);
        val body = SamlUtils.newSoapObject(Body.class);
        val query = (AttributeQuery) context.getSamlRequest();

        val scratch = context.getMessageContext().ensureSubcontext(ScratchContext.class);
        val map = (Map) Objects.requireNonNull(scratch).getMap();
        map.put(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.FALSE);

        val buildContext = context.transferTo(query, SAMLConstants.SAML2_POST_BINDING_URI);
        val saml2Response = buildSaml2Response(buildContext);
        body.getUnknownXMLObjects().add(saml2Response);

        val envelope = SamlUtils.newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        openSamlConfigBean.logObject(envelope);

        map.remove(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE);
        return encodeFinalResponse(context, envelope);
    }
}
