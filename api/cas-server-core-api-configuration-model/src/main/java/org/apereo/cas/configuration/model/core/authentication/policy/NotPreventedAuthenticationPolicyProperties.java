package org.apereo.cas.configuration.model.core.authentication.policy;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link NotPreventedAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class NotPreventedAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {
    @Serial
    private static final long serialVersionUID = 8184166804664983317L;
}
