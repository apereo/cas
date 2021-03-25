package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PrincipalAttributeMultifactorAuthenticationProperties")
public class PrincipalAttributeMultifactorAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 7426521468929733907L;

    /**
     * This is a more generic variant of the {@link #globalPrincipalAttributeNameTriggers}.
     * It may be useful in cases where there
     * is more than one provider configured and available in the application runtime and
     * you need to design a strategy to dynamically decide on the provider that should be activated for the request.
     * The decision is handed off to a Predicate implementation that define in a Groovy script whose location is taught to CAS.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties globalPrincipalAttributePredicate = new SpringResourceProperties();

    /**
     * MFA can be triggered for all users/subjects carrying a specific attribute that matches one of the conditions below.
     * <ul>
     * <li>Trigger MFA based on a principal attribute(s) whose value(s) matches a regex pattern.
     * Note that this behavior is only applicable if there is only a single MFA provider configured,
     * since that would allow CAS to know what provider to next activate.</li>
     * <li>Trigger MFA based on a principal attribute(s) whose value(s) EXACTLY matches an MFA provider.
     * This option is more relevant if you have more than one provider configured or if you have the flexibility
     * of assigning provider ids to attributes as values.</li>
     * </ul>
     * Needless to say, the attributes need to have been resolved for the principal prior to this step.
     */
    private String globalPrincipalAttributeNameTriggers;

    /**
     * The regular expression that is cross matches against the principal attribute to determine
     * if the account is qualified for multifactor authentication.
     */
    private String globalPrincipalAttributeValueRegex;

    /**
     * Force CAS to deny and block the authentication attempt
     * altogether if attribute name/value configuration cannot produce a successful
     * match to trigger multifactor authentication.
     */
    private boolean denyIfUnmatched;
}
