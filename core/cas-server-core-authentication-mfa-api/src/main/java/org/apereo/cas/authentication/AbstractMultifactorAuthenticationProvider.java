package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"order", "id"})
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148134156909L;

    private MultifactorAuthenticationProviderBypassEvaluator bypassEvaluator;

    private MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    private String failureMode = "UNDEFINED";

    private String id;

    private int order;

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public boolean matches(final String identifier) {
        return StringUtils.isNotBlank(getId()) && getId().matches(identifier);
    }

    @Override
    public RegisteredServiceMultifactorPolicyFailureModes getFailureMode() {
        return RegisteredServiceMultifactorPolicyFailureModes.valueOf(failureMode);
    }
}
