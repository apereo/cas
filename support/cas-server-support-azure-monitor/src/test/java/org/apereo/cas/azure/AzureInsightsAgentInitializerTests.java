package org.apereo.cas.azure;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.app.ApplicationEntrypointInitializer;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureInsightsAgentInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
class AzureInsightsAgentInitializerTests {
    static {
        System.setProperty(AzureInsightsAgentInitializer.AZURE_MONITOR_AGENT_ENABLED, "yes");
    }

    @Test
    void verifyOperation() {
        val agent = ServiceLoader.load(ApplicationEntrypointInitializer.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList()
            .getFirst();
        assertDoesNotThrow(() -> agent.initialize(ArrayUtils.EMPTY_STRING_ARRAY));
    }
}
