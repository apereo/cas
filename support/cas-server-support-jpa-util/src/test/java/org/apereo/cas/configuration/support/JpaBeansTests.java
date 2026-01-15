package org.apereo.cas.configuration.support;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JpaBeansTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Hibernate")
class JpaBeansTests {
    @Test
    void verifyConnectionValidity() throws Throwable {
        val ds = mock(CloseableDataSource.class);
        when(ds.getConnection()).thenThrow(new RuntimeException());
        assertFalse(JpaBeans.isValidDataSourceConnection(ds, 1));
    }
}
