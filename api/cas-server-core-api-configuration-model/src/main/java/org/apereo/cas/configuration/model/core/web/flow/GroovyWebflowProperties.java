package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link GroovyWebflowProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-webflow", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GroovyWebflowProperties extends SpringResourceProperties {

    @Serial
    private static final long serialVersionUID = 8079027843747126083L;

    /**
     * This setting allows one to provide an alternative implementation
     * to Spring Webflow's actions as implemented in Groovy.
     * See CAS documentation on the outline of the script as well as
     * any inputs and outputs expected.
     * This setting is defined as map, where the key is expected to be the
     * name/identifier of the bean that supplies the Spring Webflow action
     * and the value is a resource path to the Groovy
     * script (i.e. {@code file:/path/to/Script.groovy}) that shall be executed
     * when the action is called upon by CAS and the Spring Webflow execution runtime.
     * You will need to examine the CAS codebase to locate the proper bean identifier
     * for the action in question. Note that Groovy scripts entirely supplant
     * the CAS implementation for Spring Webflow actions and must be designed carefully
     * and in compliance with the rest of the webflow orchestration.
     */
    private Map<String, String> actions = new LinkedHashMap<>();
}
