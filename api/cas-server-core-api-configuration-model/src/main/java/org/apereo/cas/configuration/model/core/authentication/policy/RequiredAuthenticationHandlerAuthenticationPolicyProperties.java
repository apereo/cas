package org.apereo.cas.configuration.model.core.authentication.policy;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link RequiredAuthenticationHandlerAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class RequiredAuthenticationHandlerAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {

    @Serial
    private static final long serialVersionUID = -4206244023952305821L;

    /**
     * Ensure number of provided credentials does not match the sum of authentication successes and failures.
     */
    private boolean tryAll;

    /**
     * The handler name which must have successfully executed and validated credentials.
     */
    @RequiredProperty
    private String handlerName = "handlerName";
}
