package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalFactoryUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class PrincipalFactoryUtilsTests {

    @Test
    public void verifyOperation() {
        val input = PrincipalFactoryUtils.newRestfulPrincipalFactory("https://google.com");
        assertNotNull(input);
    }

}
