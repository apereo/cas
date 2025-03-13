package org.apereo.cas.azure;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.app.ApplicationEntrypointInitializer;

/**
 * This is {@link AzureInsightsAgentInitializer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class AzureInsightsAgentInitializer implements ApplicationEntrypointInitializer {
    static final String AZURE_MONITOR_AGENT_ENABLED = "AZURE_MONITOR_AGENT_ENABLED";

    @Override
    public ApplicationEntrypointInitializer initialize(final String[] mainArguments) {
        val agentEnabled = StringUtils.defaultIfBlank(System.getProperty(AZURE_MONITOR_AGENT_ENABLED,
            System.getenv(AZURE_MONITOR_AGENT_ENABLED)), "true");
        if (BooleanUtils.toBoolean(agentEnabled)) {
            //CHECKSTYLE:OFF
            System.out.println("Attaching Azure Monitor Application Insights Agent...");
            //CHECKSTYLE:ON
            ApplicationInsights.attach();
        }
        return this;
    }
}
