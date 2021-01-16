package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link JaasSecurityActuatorEndpointsMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@JsonFilter("JaasSecurityActuatorEndpointsMonitorProperties")
@Accessors(chain = true)
public class JaasSecurityActuatorEndpointsMonitorProperties implements Serializable {

    private static final long serialVersionUID = -3024678577827371641L;

    /**
     * JAAS login resource file.
     */
    private transient Resource loginConfig;

    /**
     * If set, a call to {@code Configuration#refresh()}
     * will be made by {@code #configureJaas(Resource)} method.
     */
    private boolean refreshConfigurationOnStartup = true;

    /**
     * The login context name should coincide with a given index in the login config specified.
     * This name is used as the index to the configuration specified in the login config property.
     *
     * &lt;pre&gt;
     * JAASTest {
     * org.springframework.security.authentication.jaas.TestLoginModule required;
     * };
     * &lt;/pre&gt;
     * In the above example, {@code JAASTest} should be set as the context name.
     */
    private String loginContextName;
}
