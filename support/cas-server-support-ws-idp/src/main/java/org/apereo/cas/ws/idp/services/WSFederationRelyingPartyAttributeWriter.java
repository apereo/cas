package org.apereo.cas.ws.idp.services;

import module java.base;
import module java.xml;
import org.apereo.cas.authentication.principal.Principal;

/**
 * This is {@link WSFederationRelyingPartyAttributeWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface WSFederationRelyingPartyAttributeWriter {

    /**
     * Write principal attributes for this service.
     *
     * @param writer    the writer
     * @param principal the principal
     * @param service   the service
     */
    void write(XMLStreamWriter writer, Principal principal, WSFederationRegisteredService service);

}
