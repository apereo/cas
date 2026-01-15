package org.apereo.cas.adaptors.radius;

import module java.base;
import net.jradius.packet.attribute.RadiusAttribute;

/**
 * Acts as a DTO, to carry the response returned by the
 * Radius authenticator in the event of a successful authentication,
 * and provides access to the response code as well as attributes
 * which may be used as authentication attributes.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public record CasRadiusResponse(int code, int identifier, List<RadiusAttribute> attributes) {

}
