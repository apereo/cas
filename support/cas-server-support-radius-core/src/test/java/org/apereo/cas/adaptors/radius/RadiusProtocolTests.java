package org.apereo.cas.adaptors.radius;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RadiusProtocolTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Radius")
public class RadiusProtocolTests {
    @Test
    public void verifyOperation() {
        Arrays.stream(RadiusProtocol.values()).forEach(protocol -> assertNotNull(protocol.getName()));
    }
}
