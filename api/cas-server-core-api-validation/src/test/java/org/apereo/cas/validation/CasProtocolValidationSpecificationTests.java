package org.apereo.cas.validation;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolValidationSpecificationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CAS")
class CasProtocolValidationSpecificationTests {

    @Test
    void verifyOperation() {
        val spec = (CasProtocolValidationSpecification) (assertion, request) -> false;
        assertEquals(0, spec.getOrder());
        assertDoesNotThrow(spec::reset);
        assertDoesNotThrow(() -> spec.setRenew(false));
    }
}
