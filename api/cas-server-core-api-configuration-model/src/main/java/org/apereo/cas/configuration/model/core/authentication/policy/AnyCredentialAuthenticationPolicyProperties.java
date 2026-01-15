package org.apereo.cas.configuration.model.core.authentication.policy;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AnyCredentialAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class AnyCredentialAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {

    @Serial
    private static final long serialVersionUID = 4600357071276768175L;

    /**
     * Avoid short circuiting and try every handler even if one prior succeeded.
     * Ensure number of provided credentials does not match the sum of authentication successes and failures
     */
    private boolean tryAll;

    public AnyCredentialAuthenticationPolicyProperties() {
        setEnabled(true);
    }
}
