package org.apereo.cas.acme;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AcmeAuthorizationExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
public class AcmeAuthorizationExecutorTests extends BaseAcmeTests {

    @Autowired
    @Qualifier("acmeAuthorizationExecutor")
    private AcmeAuthorizationExecutor acmeAuthorizationExecutor;

    @Test
    public void verifyOperation() {
        val auth = mock(Authorization.class);
        assertNull(acmeAuthorizationExecutor.find(auth));
        assertDoesNotThrow(() -> {
            acmeAuthorizationExecutor.execute(mock(Order.class), mock(CSRBuilder.class));
        });
    }
}
