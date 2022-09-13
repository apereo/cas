package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;

import javax.xml.stream.XMLStreamWriter;
import java.util.Set;

/**
 * This is {@link DefaultWSFederationRelyingPartyAttributeWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultWSFederationRelyingPartyAttributeWriter implements WSFederationRelyingPartyAttributeWriter {
    private final Set<String> customClaims;

    @Override
    public void write(final XMLStreamWriter writer,
                      final Principal principal,
                      final WSFederationRegisteredService service) {
        val attributes = principal.getAttributes();
        LOGGER.debug("Mapping principal attributes [{}] to claims for service [{}]", attributes, service);

        attributes.forEach(Unchecked.biConsumer((name, value) -> {
            val claimName = ProtocolAttributeEncoder.decodeAttribute(name);
            if (WSFederationClaims.contains(claimName)) {
                val uri = WSFederationClaims.valueOf(name).getUri();
                LOGGER.debug("Requested claim [{}] mapped to [{}]", name, uri);
                writeAttributeValue(writer, uri, value, service);
            } else if (WSFederationClaims.containsUri(claimName)) {
                LOGGER.debug("Requested claim [{}] directly mapped to [{}]", name, claimName);
                writeAttributeValue(writer, claimName, value, service);
            } else if (customClaims.contains(claimName)) {
                LOGGER.debug("Requested custom claim [{}]", claimName);
                writeAttributeValue(writer, claimName, value, service);
            } else {
                LOGGER.debug("Requested claim [{}] is not defined/supported by CAS", claimName);
                writeAttributeValue(writer, WSFederationConstants.getClaimInCasNamespace(claimName), value, service);
            }
        }));
    }

    protected void writeAttributeValue(final XMLStreamWriter writer, final String uri,
                                       final Object attributeValue,
                                       final WSFederationRegisteredService service) throws Exception {
        LOGGER.trace("Mapping attribute [{}] with value [{}] for service [{}]", uri, attributeValue, service.getServiceId());
        writer.writeStartElement("ic", "ClaimValue", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
        writer.writeAttribute("Uri", uri);
        writer.writeAttribute("Optional", Boolean.TRUE.toString());

        val values = CollectionUtils.toCollection(attributeValue);
        for (val value : values) {
            writer.writeStartElement("ic", "Value", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
            writer.writeCharacters(value.toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
