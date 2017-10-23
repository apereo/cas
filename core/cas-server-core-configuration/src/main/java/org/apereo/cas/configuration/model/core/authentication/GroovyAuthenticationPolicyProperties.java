package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link GroovyAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class GroovyAuthenticationPolicyProperties implements Serializable {
    private static final long serialVersionUID = 8713917167124116270L;
    /**
     * Path to the groovy script to execute.
     */
    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }
}
