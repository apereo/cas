package org.apereo.cas.adaptors.radius;

import java.util.List;

import net.jradius.packet.attribute.RadiusAttribute;

/**
 * Acts as a DTO, to carry the response returned by the
 * Radius authenticator in the event of a successful authentication,
 * and provides access to the response code as well as attributes
 * which may be used as authentication attributes.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RadiusResponse {
    
    /** The code. */
    private final int code;
    
    /** The identifier. */
    private final int identifier;
    
    /** The attributes. */
    private final List<RadiusAttribute> attributes;
    
    /**
     * Instantiates a new radius response.
     *
     * @param code the code
     * @param identifier the identifier
     * @param attributes the attributes
     */
    public RadiusResponse(final int code, final int identifier, final List<RadiusAttribute> attributes) {
        this.code = code;
        this.identifier = identifier;
        this.attributes = attributes;
    }

    public int getCode() {
        return this.code;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public List<RadiusAttribute> getAttributes() {
        return this.attributes;
    }
}
