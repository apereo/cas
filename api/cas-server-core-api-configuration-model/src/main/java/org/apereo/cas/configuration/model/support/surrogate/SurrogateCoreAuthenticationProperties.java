package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SurrogateCoreAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class SurrogateCoreAuthenticationProperties implements Serializable, CasFeatureModule {
    @Serial
    private static final long serialVersionUID = 16938920863432222L;
    
    /**
     * The separator character used to distinguish between the surrogate account and the admin/primary account.
     * For example, if you are {@code casuser} and you need to switch to {@code jsmith} as the
     * surrogate (impersonated) user, the username provided to CAS would be {@code jsmith+casuser}.
     */
    private String separator = "+";

    /**
     * Impersonation can be authorized for all primary users/subjects carrying specific attributes with a predefined matching value
     * specified via {@link #principalAttributeValues}.
     * Needless to say, the attributes need to have been resolved for the primary principal prior to this step.
     * Matching and comparison operations are case insensitive.
     */
    private List<String> principalAttributeNames = new ArrayList<>();

    /**
     * The regular expression that is cross matched against the principal attribute to determine
     * if the account is authorized for impersonation.
     * Matching and comparison operations are case insensitive.
     */
    @RegularExpressionCapable
    private List<String> principalAttributeValues = new ArrayList<>();

}
