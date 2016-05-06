package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.TestUtils;
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
        final String value = generator.generate(p, TestUtils.getService());

        assertNotNull(value);
    }

}
