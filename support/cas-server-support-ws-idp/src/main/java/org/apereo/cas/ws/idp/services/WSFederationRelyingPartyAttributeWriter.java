package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;

import javax.xml.stream.XMLStreamWriter;

/**
 * This is {@link WSFederationRelyingPartyAttributeWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface WSFederationRelyingPartyAttributeWriter {

    void write(XMLStreamWriter writer, Principal principal, WSFederationRegisteredService service);

}
