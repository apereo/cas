package org.apereo.cas.configuration.model.core.authentication.policy;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link UniquePrincipalAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class UniquePrincipalAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {
    @Serial
    private static final long serialVersionUID = -4930217087310738715L;
}
