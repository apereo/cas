package org.apereo.cas.apm;

import module java.base;
import org.apereo.cas.util.app.ApplicationEntrypointInitializer;
import co.elastic.apm.attach.ElasticApmAttacher;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ElasticApmAgentInitializer}.
 * This callback runs prior to the initialization of the application context
 * just as CAS is about to start up.
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class ElasticApmAgentInitializer implements ApplicationEntrypointInitializer {
    static final String SETTING_ELASTIC_APM_AGENT_ENABLED = "ELASTIC_APM_AGENT_ENABLED";

    @Override
    public ApplicationEntrypointInitializer initialize(final String[] mainArguments) {
        val apmEnabled = StringUtils.defaultIfBlank(System.getProperty(SETTING_ELASTIC_APM_AGENT_ENABLED,
            System.getenv(SETTING_ELASTIC_APM_AGENT_ENABLED)), "true");
        if (BooleanUtils.toBoolean(apmEnabled)) {
            //CHECKSTYLE:OFF
            IO.println("Attaching Elastic APM Agent...");
            //CHECKSTYLE:ON
            ElasticApmAttacher.attach();
        }
        return this;
    }
}
