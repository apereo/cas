package org.apereo.cas.adaptors.radius;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RadiusProtocolTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Radius")
class RadiusProtocolTests {
    @Test
    void verifyOperation() {
        Arrays.stream(RadiusProtocol.values()).forEach(protocol -> assertNotNull(protocol.getName()));
    }
}
