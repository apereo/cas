package org.apereo.cas.configuration.model.core.authentication.policy;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link AllCredentialsAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class AllCredentialsAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {
    @Serial
    private static final long serialVersionUID = 928409456096460793L;
}
