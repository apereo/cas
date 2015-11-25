package org.jasig.cas.authentication.principal;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGeneratorTests {

    @Test
    public void verifyGenerator() {
        final ShibbolethCompatiblePersistentIdGenerator generator =
                new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("testuser");
        final String value = generator.generate(p, org.jasig.cas.services.TestUtils.getService());

        assertNotNull(value);
    }

}
