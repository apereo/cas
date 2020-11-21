package org.apereo.cas.uma.web.controllers.discovery;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaWellKnownEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class UmaWellKnownEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOp() throws Exception {
        val responseEntity = umaWellKnownEndpointController.getWellKnownDiscoveryConfiguration();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
