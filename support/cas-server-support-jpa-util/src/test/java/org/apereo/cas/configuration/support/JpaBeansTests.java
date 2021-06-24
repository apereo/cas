package org.apereo.cas.configuration.support;

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
@Tag("JDBC")
public class JpaBeansTests {
    @Test
    public void verifyConnectionValidity() throws Exception {
        val ds = mock(CloseableDataSource.class);
        when(ds.getConnection()).thenThrow(new RuntimeException());
        assertFalse(JpaBeans.isValidDataSourceConnection(ds, 1));
    }
}
