package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link AuthenticationAttributeMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuthenticationAttributeMultifactorAuthenticationProperties")
public class AuthenticationAttributeMultifactorAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 6426521468929733907L;

    /**
     * MFA can be triggered for all users/subjects whose authentication event/metadata has resolved a specific attribute that
     * matches one of the below conditions:
     * <ul>
     * <li>Trigger MFA based on a authentication attribute(s) whose value(s) matches a regex pattern.
     * Note that this behavior is only applicable if there is only a single MFA provider configured,
     * since that would allow CAS to know what provider to next activate. </li>
     * <li>Trigger MFA based on a authentication attribute(s) whose value(s) EXACTLY matches an MFA provider.
     * This option is more relevant if you have more than one provider configured or if you have the
     * flexibility of assigning provider ids to attributes as values. </li>
     * </ul>
     * Needless to say, the attributes need to have been resolved for the authentication event prior to this step.
     * This trigger is generally useful when the underlying authentication engine signals
     * CAS to perform additional validation of credentials. This signal may be captured by CAS as
     * an attribute that is part of the authentication event metadata which can then trigger
     * additional multifactor authentication events.
     */
    private String globalAuthenticationAttributeNameTriggers;

    /**
     * The regular expression that is cross matches against the authentication attribute to determine
     * if the account is qualified for multifactor authentication.
     */
    private String globalAuthenticationAttributeValueRegex;

}
