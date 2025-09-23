package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {

    @Serial
    private static final long serialVersionUID = -3068390754996358337L;

    private Set<String> multifactorAuthenticationProviders = new LinkedHashSet<>();

    private MultifactorAuthenticationProviderFailureModes failureMode = MultifactorAuthenticationProviderFailureModes.UNDEFINED;

    private String principalAttributeNameTrigger;

    private String principalAttributeValueToMatch;

    private boolean bypassEnabled;

    private boolean forceExecution;

    private boolean bypassTrustedDeviceEnabled;

    private String bypassPrincipalAttributeName;

    private String bypassPrincipalAttributeValue;

    private String script;

    private boolean bypassIfMissingPrincipalAttribute;
}
