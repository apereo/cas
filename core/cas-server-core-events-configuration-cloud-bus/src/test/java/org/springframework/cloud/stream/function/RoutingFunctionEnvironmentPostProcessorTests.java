package org.springframework.cloud.stream.function;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RoutingFunctionEnvironmentPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 * @deprecated Since 8.0.0
 */
@Tag("CasConfiguration")
@SuppressWarnings("removal")
@Deprecated(since = "8.0.0", forRemoval = true)
class RoutingFunctionEnvironmentPostProcessorTests {
    @Test
    void verifyOperation() throws Exception {
        val environment = new MockEnvironment();
        val application = new SpringApplication();
        assertDoesNotThrow(() -> new RoutingFunctionEnvironmentPostProcessor()
            .postProcessEnvironment(environment, application));
    }
}
