package org.apereo.cas.configuration.model.core.authentication.policy;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RequiredAttributesAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class RequiredAttributesAuthenticationPolicyProperties extends BaseAuthenticationPolicyProperties {

    @Serial
    private static final long serialVersionUID = -4216244023952315821L;

    /**
     * Map of attributes that are required to found either as authentication
     * attributes or principal attributes.
     */
    @RequiredProperty
    @RegularExpressionCapable
    private Map<String, String> attributes = new HashMap<>();
}
