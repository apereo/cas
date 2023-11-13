package org.apereo.cas.configuration.model.core.authentication.policy;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link BaseAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public abstract class BaseAuthenticationPolicyProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -1830217018850738715L;

    /**
     * Enables the policy.
     */
    private boolean enabled;

    /**
     * The name of the authentication policy.
     */
    private String name;

    /**
     * The execution order of this policy.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;
}
