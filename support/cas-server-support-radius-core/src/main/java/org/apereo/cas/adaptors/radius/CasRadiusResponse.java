package org.apereo.cas.adaptors.radius;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.jradius.packet.attribute.RadiusAttribute;

import java.util.List;

/**
 * Acts as a DTO, to carry the response returned by the
 * Radius authenticator in the event of a successful authentication,
 * and provides access to the response code as well as attributes
 * which may be used as authentication attributes.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Getter
@RequiredArgsConstructor
public class CasRadiusResponse {

    /**
     * The code.
     */
    private final int code;

    /**
     * The identifier.
     */
    private final int identifier;

    /**
     * The attributes.
     */
    private final List<RadiusAttribute> attributes;

}
