package org.apereo.cas.validation;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolValidationSpecificationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class CasProtocolValidationSpecificationTests {

    @Test
    public void verifyOperation() {
        val spec = (CasProtocolValidationSpecification) (assertion, request) -> false;
        assertEquals(0, spec.getOrder());
        assertDoesNotThrow(spec::reset);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                spec.setRenew(false);
            }
        });
    }
}
