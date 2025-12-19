package org.apereo.cas.uma.ticket.resource;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ResourceSetTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class ResourceSetTests {

    @Test
    void verifyOperation() {
        val set = new ResourceSet();
        assertThrows(InvalidResourceSetException.class,
            () -> set.validate(new CommonProfile()));
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        assertThrows(InvalidResourceSetException.class,
            () -> set.validate(profile));
    }

}
