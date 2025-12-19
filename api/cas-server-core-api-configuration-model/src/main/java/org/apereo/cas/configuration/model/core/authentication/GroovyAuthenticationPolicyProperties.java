package org.apereo.cas.configuration.model.core.authentication;

import module java.base;
import org.apereo.cas.configuration.model.core.authentication.policy.BaseAuthenticationPolicyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GroovyAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {

    @Serial
    private static final long serialVersionUID = 8713917167124116270L;

    /**
     * Path to the groovy script to execute.
     */
    @RequiredProperty
    private String script;
}
